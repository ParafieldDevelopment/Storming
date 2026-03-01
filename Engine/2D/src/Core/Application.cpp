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
#include <fcntl.h>

#ifdef _WIN32
#include <io.h>
#else
#include <unistd.h>
#endif

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
        // Set stdin to non-blocking for command protocol
        #ifndef _WIN32
        int flags = fcntl(STDIN_FILENO, F_GETFL, 0);
        fcntl(STDIN_FILENO, F_SETFL, flags | O_NONBLOCK);
        #endif

        if (!SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS)) {
            std::cerr << "[Engine] SDL Init Error: " << SDL_GetError() << std::endl;
            return;
        }

        SDL_PropertiesID props = SDL_CreateProperties();
        SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, m_Config.Name.c_str());
        
        if (!m_Config.ShmName.empty()) {
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_BORDERLESS_BOOLEAN, true);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_UTILITY_BOOLEAN, true);
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, 16);
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, 16);
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
        SDL_GL_MakeCurrent(m_Window, glContext);

        gladLoadGLLoader((GLADloadproc)SDL_GL_GetProcAddress);

        m_RendererAPI = RendererAPI::Create();
        m_RendererAPI->Init();
        glDisable(GL_DEPTH_TEST); 

        if (!m_Config.ShmName.empty()) {
            m_FrameBuffer = new FrameBuffer(m_Config.Width, m_Config.Height, m_Config.ShmName);
        }

        Renderer2D::Init();

        float aspectRatio = (float)m_Config.Width / (float)m_Config.Height;
        s_Camera = new OrthographicCamera(-aspectRatio, aspectRatio, -1.0f, 1.0f);

        s_ActiveScene = new Scene();
        
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
        logo.AddComponent<SpriteRendererComponent>(glm::vec4{ 1.0f, 1.0f, 1.0f, 1.0f }).Texture = texture;
        logo.GetComponent<TransformComponent>().Translation = { 0.0f, 0.0f, 0.0f };
        logo.GetComponent<TransformComponent>().Scale = { 0.8f, 0.8f, 1.0f };

        s_ActiveScene->BroadcastSceneTree();

        ST_INFO("Storming Engine Initialized Successfully");
    }

    void Application::Run() {
        uint64_t lastTime = SDL_GetTicks();
        uint64_t lastTelemetryTime = lastTime;
        uint64_t lastHierarchySyncTime = lastTime;
        uint32_t frames = 0;

        while (m_Running) {
            uint64_t currentTime = SDL_GetTicks();
            float deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;

            // --- 1. Handle Input & Events ---
            SDL_Event event;
            while (SDL_PollEvent(&event)) {
                if (event.type == SDL_EVENT_QUIT) m_Running = false;
            }

            // --- 2. Handle Editor Commands (JSON via Stdin) ---
            char buffer[1024];
            #ifndef _WIN32
            ssize_t bytes = read(STDIN_FILENO, buffer, sizeof(buffer) - 1);
            if (bytes > 0) {
                buffer[bytes] = '\0';
                try {
                    auto cmd = json::parse(buffer);
                    if (cmd["type"] == "command") {
                        if (cmd["action"] == "request_scene_tree") {
                            if (s_ActiveScene) s_ActiveScene->BroadcastSceneTree();
                        } else if (cmd["action"] == "select_entity") {
                            if (s_ActiveScene) s_ActiveScene->BroadcastEntityComponents(cmd["id"]);
                        } else if (cmd["action"] == "update_component") {
                            uint32_t id = cmd["id"];
                            entt::entity handle = (entt::entity)id;
                            std::string component = cmd["component"];
                            std::string field = cmd["field"];
                            int index = cmd["index"];
                            float value = cmd["value"];

                            if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                                auto& reg = s_ActiveScene->GetRegistry();
                                if (component == "transform") {
                                    auto& tc = reg.get<TransformComponent>(handle);
                                    if (field == "translation") tc.Translation[index] = value;
                                    else if (field == "rotation") tc.Rotation[index] = value;
                                    else if (field == "scale") tc.Scale[index] = value;
                                }
                            }
                        }
                    }
                } catch (...) {}
            }
            #endif

            // --- 3. Render ---
            if (m_FrameBuffer) m_FrameBuffer->Bind();

            m_RendererAPI->SetClearColor(0.1f, 0.4f, 0.4f, 1.0f);
            m_RendererAPI->Clear();

            Renderer2D::ResetStats();
            Renderer2D::BeginScene(*s_Camera);
            if (s_ActiveScene) s_ActiveScene->OnUpdate(deltaTime);
            Renderer2D::EndScene();

            if (m_FrameBuffer) {
                glFinish(); 
                m_FrameBuffer->CopyToSharedMemory();
                m_FrameBuffer->Unbind();
            }
            
            SDL_GL_SwapWindow(m_Window);

            // --- 4. Periodic Broadcasts ---
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
                std::cout.flush();
                frames = 0;
                lastTelemetryTime = currentTime;
            }

            if (currentTime - lastHierarchySyncTime >= 2000) {
                if (s_ActiveScene) s_ActiveScene->BroadcastSceneTree();
                lastHierarchySyncTime = currentTime;
            }
        }
    }

    void Application::Close() { m_Running = false; }

    void Application::Shutdown() {
        if (s_ActiveScene) delete s_ActiveScene;
        if (s_Camera) delete s_Camera;
        Renderer2D::Shutdown();
        if (m_FrameBuffer) delete m_FrameBuffer;
        if (m_Window) SDL_DestroyWindow(m_Window);
        SDL_Quit();
    }

}
