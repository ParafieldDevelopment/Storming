#include "Storming/Renderer/RendererAPI.h"

namespace Storming {

    RendererBackend RendererAPI::s_Backend = RendererBackend::OpenGL;

    // This factory will eventually return OpenGLRendererAPI or VulkanRendererAPI
    std::unique_ptr<RendererAPI> RendererAPI::Create() {
        switch (s_Backend) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  /* return std::make_unique<OpenGLRendererAPI>(); */ return nullptr;
            case RendererBackend::Vulkan:  /* return std::make_unique<VulkanRendererAPI>(); */ return nullptr;
        }
        return nullptr;
    }

}
