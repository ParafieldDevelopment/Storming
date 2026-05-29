#pragma once

#include <memory>
#include <string>
#include "Rendering/VertexBuffer.hpp"
#include "Rendering/IndexBuffer.hpp"
#include "Rendering/VertexArray.hpp"

namespace Storming {

    /** Supported rendering backends for the engine. */
    enum class RendererBackend {
        None = 0,
        OpenGL = 1,
        Vulkan = 2
    };

    /**
     * Interface for low-level GPU operations.
     * Abstructs backend-specific commands (OpenGL/Vulkan) for viewport management,
     * clearing buffers, and issuing indexed draw calls.
     */
    class RendererAPI {
    public:
        virtual ~RendererAPI() = default;

        /** Performs global initialization for the rendering backend. */
        virtual void Init() = 0;
        virtual void SetViewport(uint32_t x, uint32_t y, uint32_t width, uint32_t height) = 0;
        virtual void SetClearColor(float r, float g, float b, float a) = 0;
        virtual void Clear() = 0;

        /** Issues a single indexed draw call to the GPU. */
        virtual void DrawIndexed(uint32_t indexCount) = 0;

        // Factory methods
        static std::shared_ptr<VertexBuffer> CreateVertexBuffer(float* vertices, uint32_t size);
        static std::shared_ptr<IndexBuffer> CreateIndexBuffer(uint32_t* indices, uint32_t count);
        static std::shared_ptr<VertexArray> CreateVertexArray();

        static RendererBackend GetBackend() { return s_Backend; }
        static void SetBackend(RendererBackend backend) { s_Backend = backend; }

        /** Factory method to create an API instance based on the active backend. */
        static std::unique_ptr<RendererAPI> Create();

    private:
        static RendererBackend s_Backend;
    };

}
