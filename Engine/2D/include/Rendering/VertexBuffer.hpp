#pragma once

#include <memory>
#include <cstdint>

#include "Rendering/Buffer.hpp"

namespace Storming {

    /** Abstract Interface for a Vertex Buffer. */
    class VertexBuffer {
    public:
        virtual ~VertexBuffer() = default;
        virtual void Bind() const = 0;
        virtual void Unbind() const = 0;
        virtual void SetData(const void* data, uint32_t size) = 0;

        virtual void SetLayout(const BufferLayout& layout) = 0;
        virtual const BufferLayout& GetLayout() const = 0;
    };

}
