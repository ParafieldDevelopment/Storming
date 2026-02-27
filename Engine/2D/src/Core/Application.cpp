#include "Storming/Core/Application.hpp"
#include "Rendering/Renderer2D.hpp"
#include "Rendering/FrameBuffer.hpp"
#include <SDL3/SDL.h>
#include <glad/glad.h>
#include <iostream>
#include <thread>
#include <nlohmann/json.hpp>
#include "Core/Log.hpp" // Include for GL_CHECK_ERROR

namespace Storming {

    Application::Application(const ApplicationConfig& config)
        : m_Config(config)
    {
        Init();
        m_CommandThreadRunning = true;
        m_CommandThread = std::thread(&Application::CommandThread, this);
    }

    Application::~Application() {
        m_CommandThreadRunning = false;
        if (m_CommandThread.joinable()) {
            m_CommandThread.join();
        }

        if (m_FrameBuffer) delete m_FrameBuffer;
        Shutdown();
    }

    void Application::Init() {
        // Initialize SDL3 Video Subsystem
        if (!SDL_Init(SDL_INIT_VIDEO)) {
            std::cerr << "[Engine] SDL Init Error: " << SDL_GetError() << std::endl;
            return;
        }

        if (m_Config.ParentWindowID != 0) {
            // Force X11 driver for embedding support on Wayland/X11
            SDL_SetHint(SDL_HINT_VIDEO_DRIVER, "x11");
        }

        // Create Window with Properties
        SDL_PropertiesID props = SDL_CreateProperties();
        SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, m_Config.Name.c_str());
        SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, m_Config.Width);
        SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, m_Config.Height);
        SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true);
        
        if (!m_Config.ShmName.empty()) {
            // Hide the window if we're streaming to shared memory
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true);
        }

        m_Window = SDL_CreateWindowWithProperties(props);
        SDL_DestroyProperties(props);

        if (!m_Window) {
            std::cerr << "[Engine] Window Creation Error: " << SDL_GetError() << std::endl;
            return;
        }

        // Create OpenGL Context
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 4);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 5);
        SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);

        SDL_GLContext glContext = SDL_GL_CreateContext(m_Window);
        SDL_GL_MakeCurrent(m_Window, glContext);
        GL_CHECK_ERROR();

        std::cout << "[Engine] OpenGL Version: " << glGetString(GL_VERSION) << std::endl;
        std::cout << "[Engine] OpenGL Vendor: " << glGetString(GL_VENDOR) << std::endl;
        std::cout << "[Engine] OpenGL Renderer: " << glGetString(GL_RENDERER) << std::endl;

        // --- Initialize Renderer ---
        Renderer2D::Init();
        GL_CHECK_ERROR();

        // --- Initialize FrameBuffer if using SHM ---
        if (!m_Config.ShmName.empty()) {
            m_FrameBuffer = new FrameBuffer(m_Config.Width, m_Config.Height, m_Config.ShmName);
            GL_CHECK_ERROR();
        }

        std::cout << "[Engine] Initialized: " << m_Config.Name << std::endl;
    }

    void Application::CommandThread() {
        std::string line;
        while (m_CommandThreadRunning && std::getline(std::cin, line)) {
            try {
                auto json = nlohmann::json::parse(line);
                std::string command = json.at("command").get<std::string>();

                if (command == "set_clear_color") {
                    m_ClearColor.r = json.at("r").get<float>();
                    m_ClearColor.g = json.at("g").get<float>();
                    m_ClearColor.b = json.at("b").get<float>();
                    m_ClearColor.a = json.value("a", 1.0f); // Optional alpha
                    std::cout << "[Engine] Set Clear Color to: " << m_ClearColor.r << ", " << m_ClearColor.g << ", " << m_ClearColor.b << std::endl;
                } else if (command == "exit") {
                    m_Running = false;
                    std::cout << "[Engine] Received exit command." << std::endl;
                } else {
                    std::cerr << "[Engine] Unknown command: " << command << std::endl;
                }
            } catch (const nlohmann::json::exception& e) {
                std::cerr << "[Engine] JSON parse error: " << e.what() << " from line: " << line << std::endl;
            } catch (const std::exception& e) {
                std::cerr << "[Engine] Command handling error: " << e.what() << " from line: " << line << std::endl;
            }
        }
        std::cout << "[Engine] Command thread shutting down." << std::endl;
    }

    void Application::Run() {
        while (m_Running) {
            SDL_Event event;
            while (SDL_PollEvent(&event)) {
                if (event.type == SDL_EVENT_QUIT) m_Running = false;
            }

            if (m_FrameBuffer) m_FrameBuffer->Bind();
            GL_CHECK_ERROR();

            // --- Render ---
            glClearColor(m_ClearColor.r, m_ClearColor.g, m_ClearColor.b, m_ClearColor.a);
            glClear(GL_COLOR_BUFFER_BIT);
            GL_CHECK_ERROR();

            Renderer2D::BeginScene();
            GL_CHECK_ERROR();
            Renderer2D::DrawQuad({ -0.5f, -0.5f }, { 0.4f, 0.4f }, { 0.9f, 0.2f, 0.3f, 1.0f });
            Renderer2D::DrawQuad({ 0.1f, 0.1f }, { 0.3f, 0.5f }, { 0.2f, 0.8f, 0.3f, 1.0f });
            Renderer2D::DrawQuad({ -0.8f, 0.6f }, { 0.2f, 0.2f }, { 0.2f, 0.4f, 0.9f, 1.0f });
            Renderer2D::EndScene();
            GL_CHECK_ERROR();

            if (m_FrameBuffer) {
                m_FrameBuffer->CopyToSharedMemory();
                m_FrameBuffer->Unbind();
            } else {
                SDL_GL_SwapWindow(m_Window);
            }
            GL_CHECK_ERROR();
        }
    }

    void Application::Close() {
        m_Running = false;
    }

    void Application::Shutdown() {
        if (m_Window) {
            SDL_DestroyWindow(m_Window);
            m_Window = nullptr;
        }
        SDL_Quit();
    }

}

