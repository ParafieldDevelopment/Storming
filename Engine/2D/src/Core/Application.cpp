#include "Storming/Core/Application.hpp"
#include "Core/Log.hpp"
#include "Rendering/Renderer2D.hpp"
#include "Rendering/FrameBuffer.hpp"
#include "Rendering/OrthographicCamera.hpp"
#include "ECS/Scene.hpp"
#include "ECS/Entity.hpp"
#include "ECS/Component.hpp"
#include "Platform/OpenGL/OpenGLRendererAPI.hpp"
#include <SDL3/SDL.h>
#include <glad/glad.h>
#include <iostream>

namespace Storming {

    static Scene* s_ActiveScene = nullptr;
    static OrthographicCamera* s_Camera = nullptr;

    Application::Application(const ApplicationConfig& config)
        : m_Config(config)
    {
        Init();
    }

    Application::~Application() {
        Shutdown();
    }

    void Application::Init() {
        if (!SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS)) {
            std::cerr << "[Engine] SDL Init Error: " << SDL_GetError() << std::endl;
            return;
        }

        // --- Window Setup ---
        SDL_PropertiesID props = SDL_CreateProperties();
        SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, m_Config.Name.c_str());
        SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, m_Config.Width);
        SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, m_Config.Height);
        SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true);
        SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, true);
        
        if (!m_Config.ShmName.empty()) {
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true);
        }

        m_Window = SDL_CreateWindowWithProperties(props);
        SDL_DestroyProperties(props);

        if (!m_Window) {
            std::cerr << "[Engine] Window Creation Error: " << SDL_GetError() << std::endl;
            return;
        }

        // --- Graphics Context ---
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 4);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 5);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);

        SDL_GLContext glContext = SDL_GL_CreateContext(m_Window);
        if (!glContext) {
            std::cerr << "[Engine] GL Context Error: " << SDL_GetError() << std::endl;
            return;
        }
        SDL_GL_MakeCurrent(m_Window, glContext);

        // Load GLAD
        if (!gladLoadGLLoader((GLADloadproc)SDL_GL_GetProcAddress)) {
            std::cerr << "[Engine] GLAD Error: Failed to load OpenGL" << std::endl;
            return;
        }

        // --- Renderer API ---
        m_RendererAPI = RendererAPI::Create();
        m_RendererAPI->Init();

        // --- Initialize FrameBuffer if using SHM ---
        if (!m_Config.ShmName.empty()) {
            m_FrameBuffer = new FrameBuffer(m_Config.Width, m_Config.Height, m_Config.ShmName);
        }

        // --- Initialize High-Level Systems ---
        Renderer2D::Init();

        // --- Camera Setup ---
        float aspectRatio = (float)m_Config.Width / (float)m_Config.Height;
        s_Camera = new OrthographicCamera(-aspectRatio, aspectRatio, -1.0f, 1.0f);

        // --- Scene Setup ---
        s_ActiveScene = new Scene();
        
        auto redSquare = s_ActiveScene->CreateEntity("Red Square");
        redSquare.AddComponent<SpriteRendererComponent>(glm::vec4{ 0.9f, 0.2f, 0.3f, 1.0f });
        redSquare.GetComponent<TransformComponent>().Translation = { -0.5f, -0.5f, 0.0f };
        redSquare.GetComponent<TransformComponent>().Scale = { 0.4f, 0.4f, 1.0f };

        auto greenSquare = s_ActiveScene->CreateEntity("Green Square");
        greenSquare.AddComponent<SpriteRendererComponent>(glm::vec4{ 0.2f, 0.8f, 0.3f, 1.0f });
        greenSquare.GetComponent<TransformComponent>().Translation = { 0.1f, 0.1f, 0.0f };
        greenSquare.GetComponent<TransformComponent>().Scale = { 0.3f, 0.5f, 1.0f };

        ST_INFO("Storming Engine Initialized Successfully");
    }

    void Application::Run() {
        while (m_Running) {
            SDL_Event event;
            while (SDL_PollEvent(&event)) {
                if (event.type == SDL_EVENT_QUIT) m_Running = false;
                if (event.type == SDL_EVENT_WINDOW_CLOSE_REQUESTED) m_Running = false;
            }

            if (m_FrameBuffer) m_FrameBuffer->Bind();

            m_RendererAPI->SetClearColor(0.1f, 0.1f, 0.12f, 1.0f);
            m_RendererAPI->Clear();

            Renderer2D::BeginScene(*s_Camera);
            if (s_ActiveScene) s_ActiveScene->OnUpdate(0.016f); // Dummy ts for now
            Renderer2D::EndScene();

            if (m_FrameBuffer) {
                m_FrameBuffer->CopyToSharedMemory();
                m_FrameBuffer->Unbind();
            } else {
                SDL_GL_SwapWindow(m_Window);
            }
        }
    }

    void Application::Close() {
        m_Running = false;
    }

    void Application::Shutdown() {
        if (s_ActiveScene) delete s_ActiveScene;
        if (s_Camera) delete s_Camera;
        
        Renderer2D::Shutdown();
        
        if (m_FrameBuffer) {
            delete m_FrameBuffer;
            m_FrameBuffer = nullptr;
        }

        if (m_Window) {
            SDL_DestroyWindow(m_Window);
            m_Window = nullptr;
        }
        SDL_Quit();
    }

}
