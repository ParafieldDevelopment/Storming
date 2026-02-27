#pragma once

#include <string>
#include <memory>
#include "Storming/Renderer/RendererAPI.hpp"
#include <glm/vec4.hpp>
#include <thread> // Added for std::thread

struct SDL_Window;

namespace Storming {

    struct ApplicationConfig {
        std::string Name = "Storming Engine";
        uint32_t Width = 1280;
        uint32_t Height = 720;
        RendererBackend Backend = RendererBackend::OpenGL;
        uint64_t ParentWindowID = 0;
        std::string ShmName = "";
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
        void CommandThread(); // Declared here

    private:
        ApplicationConfig m_Config;
        bool m_Running = true;
        SDL_Window* m_Window = nullptr;
        std::unique_ptr<RendererAPI> m_RendererAPI;
        class FrameBuffer* m_FrameBuffer = nullptr;

        glm::vec4 m_ClearColor = { 0.12f, 0.12f, 0.18f, 1.0f }; // Default clear color

        std::thread m_CommandThread;
        bool m_CommandThreadRunning = false;
    };

}
