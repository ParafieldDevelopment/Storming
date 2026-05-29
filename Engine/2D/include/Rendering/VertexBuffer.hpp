#pragma once

#include <memory>
#include <cstdint>

namespace Storming {

    /** Abstract Interface for a Vertex Buffer. */
    class VertexBuffer {
    public:
        virtual ~VertexBuffer() = default;
        virtual void Bind() const = 0;
        virtual void Unbind() const = 0;
        virtual void SetData(const void* data, uint32_t size) = 0;
    };

}
