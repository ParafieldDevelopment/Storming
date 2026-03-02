#pragma once

#include <string>
#include <memory>
#include "Storming/Renderer/RendererAPI.hpp"

struct SDL_Window;

namespace Storming {

    /**
     * Configuration settings for launching the engine.
     */
    struct ApplicationConfig {
        std::string Name = "Storming Engine";
        uint32_t Width = 1280;
        uint32_t Height = 720;
        RendererBackend Backend = RendererBackend::OpenGL;
        uint64_t ParentWindowID = 0; // For future docking/embedding support
        std::string ShmName = "";    // Non-empty triggers Editor Mode (render to shared memory)
        bool IsEditor = false;       // Enables grid and editor overlays
    };

    /**
     * The heart of the engine core.
     * Orchestrates the startup, main update loop, and shutdown of all systems.
     * Manages the IPC bridge between the C++ core and the Java Editor.
     */
    class Application {
    public:
        /** Initializes the application with the provided configuration. */
        Application(const ApplicationConfig& config);
        virtual ~Application();

        /** Starts the infinite engine loop. This method blocks until the engine closes. */
        void Run();
        /** Triggers a graceful exit of the engine loop. */
        void Close();

    private:
        void Init();
        void Shutdown();

    private:
        ApplicationConfig m_Config;
        bool m_Running = true;
        bool m_IsEditor = false;
        SDL_Window* m_Window = nullptr;
        std::unique_ptr<RendererAPI> m_RendererAPI;
        class FrameBuffer* m_FrameBuffer = nullptr;
        uint32_t m_SelectedEntityID = 0xFFFFFFFF;
    };

}
