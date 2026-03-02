#include "Rendering/Texture.hpp"
#include "Storming/Renderer/RendererAPI.hpp"
#include "Platform/OpenGL/OpenGLTexture.hpp"

namespace Storming {

    /**
     * Factory method to create a blank 2D texture.
     * @param width The width in pixels.
     * @param height The height in pixels.
     */
    std::shared_ptr<Texture2D> Texture2D::Create(uint32_t width, uint32_t height) {
        switch (RendererAPI::GetBackend()) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_shared<OpenGLTexture2D>(width, height);
            case RendererBackend::Vulkan:  return nullptr;
        }
        return nullptr;
    }

    /**
     * Factory method to load a 2D texture from an image file.
     * @param path The absolute or relative path to the image file.
     */
    std::shared_ptr<Texture2D> Texture2D::Create(const std::string& path) {
        switch (RendererAPI::GetBackend()) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_shared<OpenGLTexture2D>(path);
            case RendererBackend::Vulkan:  return nullptr;
        }
        return nullptr;
    }

}
