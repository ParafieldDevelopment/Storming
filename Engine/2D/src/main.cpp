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
        }
    }

    Storming::Application* app = new Storming::Application(config);
    app->Run();
    delete app;

    return 0;
}
