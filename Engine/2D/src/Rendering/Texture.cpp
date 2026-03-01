#include "Rendering/Texture.hpp"
#include "Storming/Renderer/RendererAPI.hpp"
#include "Platform/OpenGL/OpenGLTexture.hpp"

namespace Storming {

    std::shared_ptr<Texture2D> Texture2D::Create(uint32_t width, uint32_t height) {
        switch (RendererAPI::GetBackend()) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_shared<OpenGLTexture2D>(width, height);
            case RendererBackend::Vulkan:  return nullptr;
        }
        return nullptr;
    }

    std::shared_ptr<Texture2D> Texture2D::Create(const std::string& path) {
        switch (RendererAPI::GetBackend()) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_shared<OpenGLTexture2D>(path);
            case RendererBackend::Vulkan:  return nullptr;
        }
        return nullptr;
    }

}
