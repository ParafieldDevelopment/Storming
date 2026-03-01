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
#include <nlohmann/json.hpp>
#include <iostream>

namespace Storming {

    using json = nlohmann::json;

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

        SDL_PropertiesID props = SDL_CreateProperties();
        SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, m_Config.Name.c_str());
        
        if (!m_Config.ShmName.empty()) {
            // Tiling WM Fix: Use a strictly hidden window with no decorations or management hint
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, 1);
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, 1);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_BORDERLESS_BOOLEAN, true);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_UTILITY_BOOLEAN, true);
        } else {
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, m_Config.Width);
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, m_Config.Height);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, true);
        }
        
        SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true);

        m_Window = SDL_CreateWindowWithProperties(props);
        SDL_DestroyProperties(props);

        if (!m_Window) {
            std::cerr << "[Engine] Window Creation Error: " << SDL_GetError() << std::endl;
            return;
        }

        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 4);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 5);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);

        SDL_GLContext glContext = SDL_GL_CreateContext(m_Window);
        if (!glContext) {
            std::cerr << "[Engine] GL Context Error: " << SDL_GetError() << std::endl;
            return;
        }
        SDL_GL_MakeCurrent(m_Window, glContext);

        if (!gladLoadGLLoader((GLADloadproc)SDL_GL_GetProcAddress)) {
            std::cerr << "[Engine] GLAD Error: Failed to load OpenGL" << std::endl;
            return;
        }

        std::cout << "[Engine] OpenGL Version: " << glGetString(GL_VERSION) << std::endl;
        std::cout << "[Engine] OpenGL Renderer: " << glGetString(GL_RENDERER) << std::endl;

        m_RendererAPI = RendererAPI::Create();
        m_RendererAPI->Init();

        if (!m_Config.ShmName.empty()) {
            m_FrameBuffer = new FrameBuffer(m_Config.Width, m_Config.Height, m_Config.ShmName);
        }

        Renderer2D::Init();

        float aspectRatio = (float)m_Config.Width / (float)m_Config.Height;
        s_Camera = new OrthographicCamera(-aspectRatio, aspectRatio, -1.0f, 1.0f);

        s_ActiveScene = new Scene();
        
        // Z-Ordering: Squares at 0.0, Logo at 0.5 (Higher Z = In Front)
        auto redSquare = s_ActiveScene->CreateEntity("Red Square");
        redSquare.AddComponent<SpriteRendererComponent>(glm::vec4{ 0.9f, 0.2f, 0.3f, 1.0f });
        redSquare.GetComponent<TransformComponent>().Translation = { -0.5f, -0.5f, 0.0f };
        redSquare.GetComponent<TransformComponent>().Scale = { 0.4f, 0.4f, 1.0f };

        auto greenSquare = s_ActiveScene->CreateEntity("Green Square");
        greenSquare.AddComponent<SpriteRendererComponent>(glm::vec4{ 0.2f, 0.8f, 0.3f, 1.0f });
        greenSquare.GetComponent<TransformComponent>().Translation = { 0.1f, 0.1f, 0.0f };
        greenSquare.GetComponent<TransformComponent>().Scale = { 0.3f, 0.5f, 1.0f };

        auto logo = s_ActiveScene->CreateEntity("Logo");
        auto texture = Texture2D::Create("src/main/resources/com/parafield/storming/icons/png/icon.png");
        logo.AddComponent<SpriteRendererComponent>().Texture = texture;
        logo.GetComponent<TransformComponent>().Translation = { 0.0f, 0.0f, 0.5f }; // Move to front
        logo.GetComponent<TransformComponent>().Scale = { 0.8f, 0.8f, 1.0f };

        ST_INFO("Storming Engine Initialized Successfully");
    }

    void Application::Run() {
        uint64_t lastTime = SDL_GetTicks();
        uint64_t lastTelemetryTime = lastTime;
        uint32_t frames = 0;

        while (m_Running) {
            uint64_t currentTime = SDL_GetTicks();
            float deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;

            SDL_Event event;
            while (SDL_PollEvent(&event)) {
                if (event.type == SDL_EVENT_QUIT) m_Running = false;
                if (event.type == SDL_EVENT_WINDOW_CLOSE_REQUESTED) m_Running = false;
            }

            if (m_FrameBuffer) m_FrameBuffer->Bind();

            m_RendererAPI->SetClearColor(0.1f, 0.1f, 0.12f, 1.0f);
            m_RendererAPI->Clear();

            Renderer2D::ResetStats();
            Renderer2D::BeginScene(*s_Camera);
            if (s_ActiveScene) s_ActiveScene->OnUpdate(deltaTime);
            Renderer2D::EndScene();

            if (m_FrameBuffer) {
                m_FrameBuffer->CopyToSharedMemory();
                m_FrameBuffer->Unbind();
            } else {
                SDL_GL_SwapWindow(m_Window);
            }

            frames++;
            if (currentTime - lastTelemetryTime >= 500) {
                float fps = frames / ((currentTime - lastTelemetryTime) / 1000.0f);
                auto stats = Renderer2D::GetStats();
                json telemetry;
                telemetry["type"] = "telemetry";
                telemetry["fps"] = fps;
                telemetry["frameTime"] = (fps > 0) ? 1000.0f / fps : 0;
                telemetry["drawCalls"] = stats.DrawCalls;
                telemetry["quads"] = stats.QuadCount;
                std::cout << "[TELEMETRY]" << telemetry.dump() << std::endl;
                frames = 0;
                lastTelemetryTime = currentTime;
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
        if (m_FrameBuffer) delete m_FrameBuffer;
        if (m_Window) SDL_DestroyWindow(m_Window);
        SDL_Quit();
    }

}
