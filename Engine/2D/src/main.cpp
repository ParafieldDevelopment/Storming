#include "Storming/Core/Application.hpp"
#include <iostream>

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

    Storming::Application* app = new Storming::Application(config);
    app->Run();
    delete app;

    return 0;
}
