#include "Storming/Core/Application.h"
#include <SDL3/SDL.h>
#include <iostream>

namespace Storming {

    Application::Application(const ApplicationConfig& config)
        : m_Config(config) {
        Init();
    }

    Application::~Application() {
        Shutdown();
    }

    void Application::Init() {
        if (!SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS)) {
            std::cerr << "SDL_Init Error: " << SDL_GetError() << std::endl;
            return;
        }

        uint32_t windowFlags = SDL_WINDOW_RESIZABLE;
        if (m_Config.Backend == RendererBackend::OpenGL) {
            windowFlags |= SDL_WINDOW_OPENGL;
        } else if (m_Config.Backend == RendererBackend::Vulkan) {
            windowFlags |= SDL_WINDOW_VULKAN;
        }

        m_Window = SDL_CreateWindow(
            m_Config.Name.c_str(),
            m_Config.Width,
            m_Config.Height,
            windowFlags
        );

        if (!m_Window) {
            std::cerr << "Window creation failed: " << SDL_GetError() << std::endl;
            return;
        }

        std::cout << "Storming Engine Initialized with " 
                  << (m_Config.Backend == RendererBackend::OpenGL ? "OpenGL" : "Vulkan") 
                  << " backend." << std::endl;
    }

    void Application::Run() {
        while (m_Running) {
            SDL_Event event;
            while (SDL_PollEvent(&event)) {
                if (event.type == SDL_EVENT_QUIT) {
                    m_Running = false;
                }
            }

            // TODO: Render call
        }
    }

    void Application::Close() {
        m_Running = false;
    }

    void Application::Shutdown() {
        if (m_Window) {
            SDL_DestroyWindow(m_Window);
        }
        SDL_Quit();
    }

}
