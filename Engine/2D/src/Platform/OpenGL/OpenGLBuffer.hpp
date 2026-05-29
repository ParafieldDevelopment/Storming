#pragma once

#include "Rendering/VertexBuffer.hpp"
#include "Rendering/IndexBuffer.hpp"
#include "Rendering/VertexArray.hpp"
#include <glad/glad.h>

namespace Storming {

    class OpenGLVertexBuffer : public VertexBuffer {
    public:
        OpenGLVertexBuffer(float* vertices, uint32_t size);
        ~OpenGLVertexBuffer();
        void Bind() const override;
        void Unbind() const override;
        void SetData(const void* data, uint32_t size) override;
    private:
        GLuint m_RendererID;
    };

    class OpenGLIndexBuffer : public IndexBuffer {
    public:
        OpenGLIndexBuffer(uint32_t* indices, uint32_t count);
        ~OpenGLIndexBuffer();
        void Bind() const override;
        void Unbind() const override;
        uint32_t GetCount() const override { return m_Count; }
    private:
        GLuint m_RendererID;
        uint32_t m_Count;
    };

    class OpenGLVertexArray : public VertexArray {
    public:
        OpenGLVertexArray();
        ~OpenGLVertexArray();
        void Bind() const override;
        void Unbind() const override;
        void AddVertexBuffer(const std::shared_ptr<VertexBuffer>& vertexBuffer) override;
        void SetIndexBuffer(const std::shared_ptr<IndexBuffer>& indexBuffer) override;
        const std::vector<std::shared_ptr<VertexBuffer>>& GetVertexBuffers() const override { return m_VertexBuffers; }
        const std::shared_ptr<IndexBuffer>& GetIndexBuffer() const override { return m_IndexBuffer; }
    private:
        GLuint m_RendererID;
        std::vector<std::shared_ptr<VertexBuffer>> m_VertexBuffers;
        std::shared_ptr<IndexBuffer> m_IndexBuffer;
    };
}
