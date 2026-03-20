#include "Storming/Core/Application.hpp"
#include <iostream>

/**
 * Entry point for the Storming Engine C++ Core.
 * Parses command-line arguments provided by the Java Editor to configure the runtime.
 * 
 * Supported Flags:
 *   --vulkan      : Switch to Vulkan rendering backend (Experimental).
 *   --shm <name>  : Enable Shared Memory streaming with the specified identifier.
 *   --editor      : Enable Editor Mode (Renders grid, origin cross, and selection boxes).
 */
int main(int argc, char** argv) {
    Storming::ApplicationConfig config;
    config.Name = "Storming Engine Runtime";
    
    // Parse command line arguments from the Editor
    for (int i = 1; i < argc; ++i) {
        std::string arg = argv[i];
        if (arg == "--vulkan") {
            config.Backend = Storming::RendererBackend::Vulkan;
        } else if (arg == "--parent-id" && i + 1 < argc) {
            config.ParentWindowID = std::stoull(argv[++i]);
        } else if (arg == "--shm" && i + 1 < argc) {
            config.ShmName = argv[++i];
        } else if (arg == "--editor") {
            config.IsEditor = true;
        }
    }

    // Initialize and start the application loop
    std::cout << "[Debug] Creating Application..." << std::endl;
    Storming::Application* app = new Storming::Application(config);
    
    std::cout << "[Debug] Calling app->Run()..." << std::endl;
    app->Run();
    
    std::cout << "[Debug] Application loop finished." << std::endl;
    delete app;

    return 0;
}
