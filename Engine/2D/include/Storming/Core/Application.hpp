#pragma once

#include <string>
#include <memory>
#include "Storming/Renderer/RendererAPI.hpp"

struct SDL_Window;

namespace Storming {

    struct ApplicationConfig {
        std::string Name = "Storming Engine";
        uint32_t Width = 1280;
        uint32_t Height = 720;
        RendererBackend Backend = RendererBackend::OpenGL;
    };

    class Application {
    public:
        Application(const ApplicationConfig& config);
        virtual ~Application();

        void Run();
        void Close();

    private:
        void Init();
        void Shutdown();

    private:
        ApplicationConfig m_Config;
        bool m_Running = true;
        SDL_Window* m_Window = nullptr;
        std::unique_ptr<RendererAPI> m_RendererAPI;
    };

}
