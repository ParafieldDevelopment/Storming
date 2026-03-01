#include "Storming/Renderer/RendererAPI.hpp"
#include "Platform/OpenGL/OpenGLRendererAPI.hpp"

namespace Storming {

    RendererBackend RendererAPI::s_Backend = RendererBackend::OpenGL;

    std::unique_ptr<RendererAPI> RendererAPI::Create() {
        switch (s_Backend) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_unique<OpenGLRendererAPI>();
            case RendererBackend::Vulkan:  return nullptr;
        }
        return nullptr;
    }

}
