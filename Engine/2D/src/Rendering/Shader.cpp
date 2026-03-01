#include "Rendering/Shader.hpp"
#include "Storming/Renderer/RendererAPI.hpp"
#include "Platform/OpenGL/OpenGLShader.hpp"

namespace Storming {

    std::unique_ptr<Shader> Shader::Create(const std::string& vertexSrc, const std::string& fragmentSrc) {
        switch (RendererAPI::GetBackend()) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_unique<OpenGLShader>(vertexSrc, fragmentSrc);
            case RendererBackend::Vulkan:  return nullptr;
        }
        return nullptr;
    }

}
