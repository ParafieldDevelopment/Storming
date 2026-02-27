#include "Storming/Core/Application.h"

int main(int argc, char** argv) {
    Storming::ApplicationConfig config;
    config.Name = "Storming Engine Runtime";
    
    // Check for renderer flag (Editor will pass this)
    for (int i = 1; i < argc; ++i) {
        std::string arg = argv[i];
        if (arg == "--vulkan") {
            config.Backend = Storming::RendererBackend::Vulkan;
        }
    }

    Storming::Application app(config);
    app.Run();

    return 0;
}
