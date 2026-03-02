#pragma once

#include <string>
#include <memory>

namespace Storming {

    /**
     * Base interface for GPU texture resources.
     * Manages dimensions, backend-specific IDs, and data uploads.
     */
    class Texture {
    public:
        virtual ~Texture() = default;

        virtual uint32_t GetWidth() const = 0;
        virtual uint32_t GetHeight() const = 0;
        virtual uint32_t GetRendererID() const = 0;

        /** Uploads raw pixel data to the texture on the GPU. */
        virtual void SetData(void* data, uint32_t size) = 0;

        /** Binds the texture to a specific GPU sampling slot. */
        virtual void Bind(uint32_t slot = 0) const = 0;

        virtual bool operator==(const Texture& other) const = 0;
    };

    /**
     * Specialized interface for 2D textures.
     * Provides factory methods for creating blank textures or loading from files.
     */
    class Texture2D : public Texture {
    public:
        /** Creates a blank texture with the specified dimensions. */
        static std::shared_ptr<Texture2D> Create(uint32_t width, uint32_t height);
        /** Loads an image from disk (PNG, JPG, etc.) and creates a GPU texture. */
        static std::shared_ptr<Texture2D> Create(const std::string& path);
    };

}
