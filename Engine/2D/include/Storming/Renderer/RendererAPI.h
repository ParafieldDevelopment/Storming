#pragma once

#include <memory>
#include <string>

namespace Storming {

    enum class RendererBackend {
        None = 0,
        OpenGL = 1,
        Vulkan = 2
    };

    class RendererAPI {
    public:
        virtual ~RendererAPI() = default;

        virtual void Init() = 0;
        virtual void SetViewport(uint32_t x, uint32_t y, uint32_t width, uint32_t height) = 0;
        virtual void SetClearColor(float r, float g, float b, float a) = 0;
        virtual void Clear() = 0;

        virtual void DrawIndexed(uint32_t indexCount) = 0;

        static RendererBackend GetBackend() { return s_Backend; }
        static void SetBackend(RendererBackend backend) { s_Backend = backend; }

        static std::unique_ptr<RendererAPI> Create();

    private:
        static RendererBackend s_Backend;
    };

}
