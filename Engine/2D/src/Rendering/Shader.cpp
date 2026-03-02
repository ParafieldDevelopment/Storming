#include "Rendering/Shader.hpp"
#include "Storming/Renderer/RendererAPI.hpp"
#include "Platform/OpenGL/OpenGLShader.hpp"

namespace Storming {

    /**
     * Factory method to create a shader instance.
     * Decouples the generic Shader interface from specific implementations like OpenGLShader.
     * 
     * @param vertexSrc The source code for the vertex shader.
     * @param fragmentSrc The source code for the fragment shader.
     * @return A unique pointer to the created shader instance.
     */
    std::unique_ptr<Shader> Shader::Create(const std::string& vertexSrc, const std::string& fragmentSrc) {
        switch (RendererAPI::GetBackend()) {
            case RendererBackend::None:    return nullptr;
            case RendererBackend::OpenGL:  return std::make_unique<OpenGLShader>(vertexSrc, fragmentSrc);
            case RendererBackend::Vulkan:  return nullptr; // Future: add VulkanShader
        }
        return nullptr;
    }

}
