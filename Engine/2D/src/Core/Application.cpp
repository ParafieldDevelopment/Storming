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
#include <vector>

#ifdef _WIN32
#include <io.h>
#else
#include <unistd.h>
#endif

namespace Storming {

    using json = nlohmann::json;

    static Scene* s_ActiveScene = nullptr;
    static OrthographicCamera* s_Camera = nullptr;
    static std::string s_CommandBuffer = "";

    Application::Application(const ApplicationConfig& config)
        : m_Config(config)
    {
        m_IsEditor = config.IsEditor;
        m_SelectedEntityID = 0xFFFFFFFF;
        Init();
    }

    Application::~Application() {
        Shutdown();
    }

    void Application::Init() {
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
            }

            // --- Robust Line-Buffered Command Reader ---
            char readBuf[1024];
            #ifndef _WIN32
            ssize_t bytes = read(STDIN_FILENO, readBuf, sizeof(readBuf) - 1);
            if (bytes > 0) {
                readBuf[bytes] = '\0';
                s_CommandBuffer += readBuf;
                
                size_t pos;
                while ((pos = s_CommandBuffer.find('\n')) != std::string::npos) {
                    std::string line = s_CommandBuffer.substr(0, pos);
                    s_CommandBuffer.erase(0, pos + 1);
                    
                    try {
                        auto cmd = json::parse(line);
                        if (cmd["type"] == "command") {
                            std::string action = cmd["action"];
                            if (action == "request_scene_tree") {
                                if (s_ActiveScene) s_ActiveScene->BroadcastSceneTree();
                            } else if (action == "request_save") {
                                if (s_ActiveScene) {
                                    json dump;
                                    dump["type"] = "scene_data_dump";
                                    dump["data"] = s_ActiveScene->Serialize();
                                    std::cout << "[TELEMETRY]" << dump.dump() << std::endl;
                                    std::cout.flush();
                                }
                            } else if (action == "load_scene") {
                                if (s_ActiveScene) s_ActiveScene->LoadFromFile(cmd["path"]);
                            } else if (action == "select_entity") {
                                m_SelectedEntityID = cmd["id"];
                                if (s_ActiveScene) s_ActiveScene->BroadcastEntityComponents(m_SelectedEntityID);
                            } else if (action == "translate_selected") {
                                entt::entity handle = (entt::entity)m_SelectedEntityID;
                                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                                    auto& tc = s_ActiveScene->GetRegistry().get<TransformComponent>(handle);
                                    tc.Translation.x += (float)cmd["dx"];
                                    tc.Translation.y += (float)cmd["dy"];
                                }
                            } else if (action == "rotate_selected") {
                                entt::entity handle = (entt::entity)m_SelectedEntityID;
                                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                                    auto& tc = s_ActiveScene->GetRegistry().get<TransformComponent>(handle);
                                    tc.Rotation.z += (float)cmd["da"];
                                }
                            } else if (action == "scale_selected") {
                                entt::entity handle = (entt::entity)m_SelectedEntityID;
                                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                                    auto& tc = s_ActiveScene->GetRegistry().get<TransformComponent>(handle);
                                    float ds = (float)cmd["ds"];
                                    tc.Scale.x += ds; tc.Scale.y += ds;
                                }
                            } else if (action == "request_picking") {
                                if (s_ActiveScene) m_SelectedEntityID = s_ActiveScene->PickEntity(cmd["x"], cmd["y"]);
                            } else if (action == "update_component") {
                                entt::entity handle = (entt::entity)((uint32_t)cmd["id"]);
                                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                                    auto& reg = s_ActiveScene->GetRegistry();
                                    std::string comp = cmd["component"];
                                    std::string field = cmd["field"];
                                    if (comp == "transform") {
                                        auto& tc = reg.get<TransformComponent>(handle);
                                        if (field == "translation") tc.Translation[cmd["index"]] = cmd["value"];
                                        else if (field == "rotation") tc.Rotation[cmd["index"]] = cmd["value"];
                                        else if (field == "scale") tc.Scale[cmd["index"]] = cmd["value"];
                                    } else if (comp == "spriterenderer" && field == "color") {
                                        auto& src = reg.get<SpriteRendererComponent>(handle);
                                        src.Color = { (float)cmd["r"], (float)cmd["g"], (float)cmd["b"], (float)cmd["a"] };
                                    }
                                }
                            } else if (action == "create_entity") {
                                if (s_ActiveScene) {
                                    auto e = s_ActiveScene->CreateEntity(cmd.value("name", "New Entity"));
                                    if (cmd.contains("sprite")) e.AddComponent<SpriteRendererComponent>();
                                }
                            } else if (action == "delete_entity") {
                                entt::entity handle = (entt::entity)((uint32_t)cmd["id"]);
                                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle))
                                    s_ActiveScene->DestroyEntity({handle, s_ActiveScene});
                            }
                        }
                    } catch (...) {}
                }
            }
            #endif

            if (m_FrameBuffer) m_FrameBuffer->Bind();

            m_RendererAPI->SetClearColor(0.1f, 0.1f, 0.12f, 1.0f);
            m_RendererAPI->Clear();

            Renderer2D::ResetStats();
            Renderer2D::BeginScene(*s_Camera);
            
            if (m_IsEditor) {
                // Grid
                glm::vec4 gridColor = { 0.2f, 0.2f, 0.2f, 1.0f };
                for (float i = -10.0f; i <= 10.0f; i += 1.0f) {
                    Renderer2D::DrawLine({i, -10.0f, 0.0f}, {i, 10.0f, 0.0f}, gridColor);
                    Renderer2D::DrawLine({-10.0f, i, 0.0f}, {10.0f, i, 0.0f}, gridColor);
                }
                Renderer2D::DrawLine({-1.0f, 0.0f, 0.01f}, {1.0f, 0.0f, 0.01f}, {1.0f, 0.0f, 0.0f, 1.0f});
                Renderer2D::DrawLine({0.0f, -1.0f, 0.01f}, {0.0f, 1.0f, 0.01f}, {0.0f, 1.0f, 0.0f, 1.0f});
            }

            if (s_ActiveScene) s_ActiveScene->OnUpdate(deltaTime);

            // Selection Highlighting (Yellow Box)
            if (m_IsEditor && m_SelectedEntityID != 0xFFFFFFFF) {
                entt::entity handle = (entt::entity)m_SelectedEntityID;
                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                    auto& tc = s_ActiveScene->GetRegistry().get<TransformComponent>(handle);
                    glm::vec3 p = tc.Translation;
                    glm::vec2 s = { tc.Scale.x, tc.Scale.y };
                    glm::vec4 c = { 1.0f, 1.0f, 0.0f, 1.0f };
                    Renderer2D::DrawLine({p.x - s.x/2, p.y - s.y/2, 0.1f}, {p.x + s.x/2, p.y - s.y/2, 0.1f}, c);
                    Renderer2D::DrawLine({p.x + s.x/2, p.y - s.y/2, 0.1f}, {p.x + s.x/2, p.y + s.y/2, 0.1f}, c);
                    Renderer2D::DrawLine({p.x + s.x/2, p.y + s.y/2, 0.1f}, {p.x - s.x/2, p.y + s.y/2, 0.1f}, c);
                    Renderer2D::DrawLine({p.x - s.x/2, p.y + s.y/2, 0.1f}, {p.x - s.x/2, p.y - s.y/2, 0.1f}, c);
                }
            }

            Renderer2D::EndScene();

            if (m_FrameBuffer) {
                glFinish(); 
                m_FrameBuffer->CopyToSharedMemory();
                m_FrameBuffer->Unbind();
            }
            SDL_GL_SwapWindow(m_Window);

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
