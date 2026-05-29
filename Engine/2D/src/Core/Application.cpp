#include "Storming/Core/Application.hpp"
#include "Core/Log.hpp"
#include "Rendering/Renderer2D.hpp"
#include "Rendering/FrameBuffer.hpp"
#include "Rendering/OrthographicCamera.hpp"
#include "ECS/Scene.hpp"
#include "ECS/Entity.hpp"
#include "ECS/Component.hpp"
#include "Platform/OpenGL/OpenGLRendererAPI.hpp"
#include "Physics/PhysicsBridge.hpp"
#include "Core/AssetRegistry.hpp"
#include <SDL3/SDL.h>
#include <glad/glad.h>
#include <nlohmann/json.hpp>
#include <iostream>
#include <fcntl.h>
#include <vector>
#include <csignal>

#ifdef _WIN32
#include <io.h>
#include <windows.h>
#else
#include <unistd.h>
#include <execinfo.h>
#endif

namespace Storming {

    using json = nlohmann::json;

    static Scene* s_ActiveScene = nullptr;
    static OrthographicCamera* s_Camera = nullptr;
    static std::string s_CommandBuffer = "";

    void SignalHandler(int signal) {
        std::cerr << "\n[CRITICAL] Engine caught signal: " << signal << std::endl;
        #ifndef _WIN32
        void* array[10];
        size_t size = backtrace(array, 10);
        std::cerr << "[CRITICAL] Stack Trace:" << std::endl;
        backtrace_symbols_fd(array, size, STDERR_FILENO);
        #endif
        std::cerr.flush();
        exit(signal);
    }

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
        std::signal(SIGSEGV, SignalHandler);
        std::signal(SIGILL, SignalHandler);
        std::signal(SIGFPE, SignalHandler);
        std::signal(SIGABRT, SignalHandler);

        #ifndef _WIN32
        int flags = fcntl(STDIN_FILENO, F_GETFL, 0);
        fcntl(STDIN_FILENO, F_SETFL, flags | O_NONBLOCK);
        #endif

        if (!SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS)) {
            std::cerr << "[Engine] SDL Init Error: " << SDL_GetError() << std::endl;
            return;
        }

        SDL_PropertiesID props = SDL_CreateProperties();
        std::string title = m_Config.Name;
        if (!m_Config.ShmName.empty()) {
            title = "StormingEngine_Background_" + m_Config.ShmName;
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_BORDERLESS_BOOLEAN, true);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_UTILITY_BOOLEAN, true);
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, 1); // 1x1 is safer than 16x16
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, 1);
        } else {
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, m_Config.Width);
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, m_Config.Height);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, true);
        }
        SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, title.c_str());
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

        RendererAPI::SetInstance(RendererAPI::Create());
        RendererAPI::Get().Init();
        
        glDisable(GL_DEPTH_TEST); 

        if (!m_Config.ShmName.empty()) {
            m_FrameBuffer = new FrameBuffer(m_Config.Width, m_Config.Height, m_Config.ShmName);
        }

        Renderer2D::Init();

        float aspectRatio = (float)m_Config.Width / (float)m_Config.Height;
        s_Camera = new OrthographicCamera(-aspectRatio, aspectRatio, -1.0f, 1.0f);

        s_ActiveScene = new Scene();

        st_physics_init_test();

        ST_INFO("Storming Engine Initialized Successfully");
    }

    void Application::Run() {
        uint64_t lastTime = SDL_GetTicks();
        uint64_t lastTelemetryTime = lastTime;
        uint32_t frames = 0;
        uint32_t totalFrames = 0;

        while (m_Running) {
            uint64_t currentTime = SDL_GetTicks();
            float deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;

            SDL_Event event;
            while (SDL_PollEvent(&event)) {
                if (event.type == SDL_EVENT_QUIT) m_Running = false;
            }

            // --- Cross-Platform Non-Blocking Command Reader ---
            bool hasData = false;
            char readBuf[1024];
            memset(readBuf, 0, sizeof(readBuf));
            int bytesRead = 0;

            #ifdef _WIN32
            HANDLE hStdin = GetStdHandle(STD_INPUT_HANDLE);
            DWORD dwAvail = 0;
            if (PeekNamedPipe(hStdin, NULL, 0, NULL, &dwAvail, NULL) && dwAvail > 0) {
                DWORD readBytes;
                if (ReadFile(hStdin, readBuf, sizeof(readBuf) - 1, &readBytes, NULL)) {
                    bytesRead = (int)readBytes;
                    hasData = (bytesRead > 0);
                }
            }
            #else
            ssize_t bytes = read(STDIN_FILENO, readBuf, sizeof(readBuf) - 1);
            if (bytes > 0) {
                bytesRead = (int)bytes;
                hasData = true;
            }
            #endif

            if (hasData && bytesRead > 0) {
                readBuf[bytesRead] = '\0';
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
                            } else if (action == "resize") {
                                uint32_t w = cmd["width"];
                                uint32_t h = cmd["height"];
                                if (w > 0 && h > 0 && (w != m_Config.Width || h != m_Config.Height)) {
                                    m_Config.Width = w;
                                    m_Config.Height = h;
                                    
                                    if (m_FrameBuffer) m_FrameBuffer->Resize(w, h);
                                    
                                    float aspectRatio = (float)w / (float)h;
                                    if (s_Camera) s_Camera->SetProjection(-aspectRatio, aspectRatio, -1.0f, 1.0f);
                                    
                                    if (s_ActiveScene) s_ActiveScene->OnViewportResize(w, h);

                                    // Send Handshake Confirmation
                                    json event;
                                    event["type"] = "event";
                                    event["action"] = "viewport_resized";
                                    event["width"] = w;
                                    event["height"] = h;
                                    std::cout << "[TELEMETRY]" << event.dump() << std::endl;
                                    std::cout.flush();
                                }
                            } else if (action == "request_asset_load") {
                                std::string path = cmd["path"];
                                AssetRegistry::Get().LoadTextureAsync(path);
                                ST_INFO("Started loading asset: {0}", path);
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
                            } else if (action == "update_component") {
                                entt::entity handle = (entt::entity)((uint32_t)cmd["id"]);
                                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                                    auto& reg = s_ActiveScene->GetRegistry();
                                    std::string comp = cmd["component"];
                                    std::string field = cmd["field"];

                                    if (field == "init") {
                                        if (comp == "rigidbody2d") reg.get_or_emplace<RigidBody2DComponent>(handle);
                                        else if (comp == "boxcollider2d") reg.get_or_emplace<BoxCollider2DComponent>(handle);
                                        else if (comp == "spriterenderer") reg.get_or_emplace<SpriteRendererComponent>(handle);
                                        return;
                                    }

                                    if (comp == "transform") {
                                        auto& tc = reg.get<TransformComponent>(handle);
                                        if (field == "translation") {
                                            tc.Translation[cmd["index"]] = cmd["value"];
                                        } else if (field == "rotation") {
                                            tc.Rotation[cmd["index"]] = cmd["value"];
                                        } else if (field == "scale") {
                                            tc.Scale[cmd["index"]] = cmd["value"];
                                        }
                                    } else if (comp == "spriterenderer") {
                                        auto& src = reg.get<SpriteRendererComponent>(handle);
                                        if (field == "color") {
                                            src.Color = { (float)cmd["r"], (float)cmd["g"], (float)cmd["b"], (float)cmd["a"] };
                                        } else if (field == "texture") {
                                            std::string path = cmd["path"];
                                            src.Texture = AssetRegistry::Get().GetTexture(path);
                                            if (!src.Texture) src.Texture = Texture2D::Create(path); // Fallback
                                            src.TexturePath = path;
                                        }
                                    } else if (comp == "rigidbody2d") {
                                        auto& rb = reg.get_or_emplace<RigidBody2DComponent>(handle);
                                        if (field == "type") rb.Type = (RigidBody2DComponent::BodyType)cmd["value"];
                                        else if (field == "fixed_rotation") rb.FixedRotation = cmd["value"];
                                        rb.RuntimeBody = nullptr;
                                    } else if (comp == "boxcollider2d") {
                                        auto& bc = reg.get_or_emplace<BoxCollider2DComponent>(handle);
                                        if (field == "size") bc.Size[cmd["index"]] = cmd["value"];
                                        else if (field == "offset") bc.Offset[cmd["index"]] = cmd["value"];
                                        else if (field == "density") bc.Density = cmd["value"];
                                        else if (field == "friction") bc.Friction = cmd["value"];
                                        else if (field == "restitution") bc.Restitution = cmd["value"];
                                        
                                        if (reg.all_of<RigidBody2DComponent>(handle))
                                            reg.get<RigidBody2DComponent>(handle).RuntimeBody = nullptr;
                                    }
                                }
                            } else if (action == "create_entity") {
                                if (s_ActiveScene) {
                                    auto e = s_ActiveScene->CreateEntity(cmd.value("name", "New Entity"));
                                    if (cmd.contains("sprite")) e.AddComponent<SpriteRendererComponent>();
                                    s_ActiveScene->BroadcastSceneTree();
                                }
                            } else if (action == "delete_entity") {
                                entt::entity handle = (entt::entity)((uint32_t)cmd["id"]);
                                if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                                    s_ActiveScene->DestroyEntity({handle, s_ActiveScene});
                                    s_ActiveScene->BroadcastSceneTree();
                                }
                            }
                        }
                    } catch (...) {}
                }
            }

            if (m_FrameBuffer) m_FrameBuffer->Bind();

            m_RendererAPI->SetClearColor(0.1f, 0.1f, 0.12f, 1.0f);
            m_RendererAPI->Clear();

            Renderer2D::ResetStats();
            if (s_Camera) {
                Renderer2D::BeginScene(*s_Camera);
                if (m_IsEditor) {
                    glm::vec4 gridColor = { 0.2f, 0.2f, 0.2f, 1.0f };
                    for (float i = -10.0f; i <= 10.0f; i += 1.0f) {
                        Renderer2D::DrawLine({i, -10.0f, 0.0f}, {i, 10.0f, 0.0f}, gridColor);
                        Renderer2D::DrawLine({-10.0f, i, 0.0f}, {10.0f, i, 0.0f}, gridColor);
                    }
                    Renderer2D::DrawLine({-1.0f, 0.0f, 0.01f}, {1.0f, 0.0f, 0.01f}, {1.0f, 0.0f, 0.0f, 1.0f});
                    Renderer2D::DrawLine({0.0f, -1.0f, 0.01f}, {0.0f, 1.0f, 0.01f}, {0.0f, 1.0f, 0.0f, 1.0f});
                }
                if (s_ActiveScene) s_ActiveScene->OnUpdate(deltaTime);
                if (m_IsEditor && m_SelectedEntityID != 0xFFFFFFFF) {
                    entt::entity handle = (entt::entity)m_SelectedEntityID;
                    if (s_ActiveScene && s_ActiveScene->GetRegistry().valid(handle)) {
                        auto& tc = s_ActiveScene->GetRegistry().get<TransformComponent>(handle);
                        glm::vec3 p = tc.Translation; glm::vec2 s = { tc.Scale.x, tc.Scale.y };
                        glm::vec4 c = { 1.0f, 1.0f, 0.0f, 1.0f };
                        Renderer2D::DrawLine({p.x - s.x/2, p.y - s.y/2, 0.1f}, {p.x + s.x/2, p.y - s.y/2, 0.1f}, c);
                        Renderer2D::DrawLine({p.x + s.x/2, p.y - s.y/2, 0.1f}, {p.x + s.x/2, p.y + s.y/2, 0.1f}, c);
                        Renderer2D::DrawLine({p.x + s.x/2, p.y + s.y/2, 0.1f}, {p.x - s.x/2, p.y + s.y/2, 0.1f}, c);
                        Renderer2D::DrawLine({p.x - s.x/2, p.y + s.y/2, 0.1f}, {p.x - s.x/2, p.y - s.y/2, 0.1f}, c);
                    }
                }
                Renderer2D::EndScene();
            }

            if (m_FrameBuffer) {
                glFinish(); 
                m_FrameBuffer->CopyToSharedMemory();
                m_FrameBuffer->Unbind();
            }
            
            SDL_GL_SwapWindow(m_Window);
            
            frames++;
            totalFrames++;
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
