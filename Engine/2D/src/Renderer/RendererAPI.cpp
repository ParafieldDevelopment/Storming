#include "Storming/Renderer/RendererAPI.hpp"
#include "Platform/OpenGL/OpenGLRendererAPI.hpp"
#include "Platform/OpenGL/OpenGLBuffer.hpp"

namespace Storming {

    RendererBackend RendererAPI::s_Backend = RendererBackend::OpenGL;
    std::unique_ptr<RendererAPI> RendererAPI::s_Instance = nullptr;

    std::shared_ptr<VertexBuffer> RendererAPI::CreateVertexBuffer(float* vertices, uint32_t size) {
        switch (s_Backend) {
            case RendererBackend::OpenGL: return std::make_shared<OpenGLVertexBuffer>(vertices, size);
        }
        return nullptr;
    }

    std::shared_ptr<IndexBuffer> RendererAPI::CreateIndexBuffer(uint32_t* indices, uint32_t count) {
        switch (s_Backend) {
            case RendererBackend::OpenGL: return std::make_shared<OpenGLIndexBuffer>(indices, count);
        }
        return nullptr;
    }

    std::shared_ptr<VertexArray> RendererAPI::CreateVertexArray() {
        switch (s_Backend) {
            case RendererBackend::OpenGL: return std::make_shared<OpenGLVertexArray>();
        }
        return nullptr;
    }

    std::unique_ptr<RendererAPI> RendererAPI::Create() {
        switch (s_Backend) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_unique<OpenGLRendererAPI>();
            case RendererBackend::Vulkan:  return nullptr;
        }
        return nullptr;
    }

}
