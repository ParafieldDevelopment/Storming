#pragma once

#include <string>
#include <memory>
#include <glm/glm.hpp>

namespace Storming {

    /**
     * Interface for GPU shader programs.
     * Provides an abstraction for binding and setting uniform variables.
     * Actual implementation is backend-specific (e.g., OpenGLShader).
     */
    class Shader {
    public:
        virtual ~Shader() = default;

        /** Activates the shader program on the GPU. */
        virtual void Bind() const = 0;
        /** Deactivates the shader program. */
        virtual void Unbind() const = 0;

        // --- Uniform Setters ---
        virtual void SetInt(const std::string& name, int value) = 0;
        virtual void SetIntArray(const std::string& name, int* values, uint32_t count) = 0;
        virtual void SetFloat(const std::string& name, float value) = 0;
        virtual void SetFloat3(const std::string& name, const glm::vec3& value) = 0;
        virtual void SetFloat4(const std::string& name, const glm::vec4& value) = 0;
        virtual void SetMat4(const std::string& name, const glm::mat4& value) = 0;

        /**
         * Factory method to create a shader from source code.
         * @param vertexSrc GLSL/HLSL source for the vertex stage.
         * @param fragmentSrc GLSL/HLSL source for the fragment stage.
         */
        static std::unique_ptr<Shader> Create(const std::string& vertexSrc, const std::string& fragmentSrc);
    };

}
