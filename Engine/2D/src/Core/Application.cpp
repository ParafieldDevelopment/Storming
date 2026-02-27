#include "Storming/Core/Application.hpp"
#include "Rendering/Renderer2D.hpp"
#include <SDL3/SDL.h>
#include <glad/glad.h>
#include <iostream>

namespace Storming {

    Application::Application(const ApplicationConfig& config)
        : m_Config(config)
    {
        Init();
    }

    Application::~Application() {
        Shutdown();
    }

    void Application::Init() {
        // Initialize SDL3 Video Subsystem
        if (!SDL_Init(SDL_INIT_VIDEO)) {
            std::cerr << "[Engine] SDL Init Error: " << SDL_GetError() << std::endl;
            return;
        }

        // Create Window with Properties
        SDL_PropertiesID props = SDL_CreateProperties();
        SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, m_Config.Name.c_str());
        SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, m_Config.Width);
        SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, m_Config.Height);
        SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true);
        SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, true);

        if (m_Config.ParentWindowID != 0) {
            // Embed into Java's Canvas (X11 Specific for now)
            SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_X11_WINDOW_NUMBER, m_Config.ParentWindowID);
            SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_BORDERLESS_BOOLEAN, true);
            std::cout << "[Engine] Embedding into X11 Window ID: " << m_Config.ParentWindowID << std::endl;
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
        if (!glContext) {
            std::cerr << "[Engine] OpenGL Context Error: " << SDL_GetError() << std::endl;
            return;
        }

        SDL_GL_MakeCurrent(m_Window, glContext);

        // --- Initialize Renderer ---
        Renderer2D::Init();

        std::cout << "[Engine] Initialized OpenGL 4.5: " << m_Config.Name << std::endl;
    }

    void Application::Run() {
        while (m_Running) {
            SDL_Event event;
            while (SDL_PollEvent(&event)) {
                if (event.type == SDL_EVENT_QUIT) {
                    m_Running = false;
                }
            }

            // --- Render Loop ---
            // Clear screen to "Storming Blue" (Hex: 0x1e1e2e -> ~0.12, 0.12, 0.18)
            glClearColor(0.12f, 0.12f, 0.18f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            Renderer2D::BeginScene();

            // Draw a few test quads
            Renderer2D::DrawQuad({ -0.5f, -0.5f }, { 0.4f, 0.4f }, { 0.9f, 0.2f, 0.3f, 1.0f }); // Reddish
            Renderer2D::DrawQuad({ 0.1f, 0.1f }, { 0.3f, 0.5f }, { 0.2f, 0.8f, 0.3f, 1.0f });  // Greenish
            Renderer2D::DrawQuad({ -0.8f, 0.6f }, { 0.2f, 0.2f }, { 0.2f, 0.4f, 0.9f, 1.0f }); // Blueish

            Renderer2D::EndScene();
            
            SDL_GL_SwapWindow(m_Window);
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
