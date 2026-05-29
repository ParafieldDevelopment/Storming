#pragma once

#include <memory>
#include <cstdint>

namespace Storming {

    /** Abstract Interface for an Index Buffer. */
    class IndexBuffer {
    public:
        virtual ~IndexBuffer() = default;
        virtual void Bind() const = 0;
        virtual void Unbind() const = 0;
        virtual uint32_t GetCount() const = 0;
    };

}
