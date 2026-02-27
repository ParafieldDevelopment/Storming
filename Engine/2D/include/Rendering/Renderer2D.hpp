#pragma once

#include <glm/glm.hpp>
#include <vector>

namespace Storming {

    struct Vertex {
        glm::vec3 Position;
        glm::vec4 Color;
        // glm::vec2 TexCoord;
    };

    class Renderer2D {
    public:
        struct Statistics {
            uint32_t DrawCalls = 0;
            uint32_t QuadCount = 0;
        };

        static void Init();
        static void Shutdown();

        static void BeginScene();
        static void EndScene();
        static void Flush();

        // High Performance Batching Draw
        static void DrawQuad(const glm::vec2& position, const glm::vec2& size, const glm::vec4& color);

        static Statistics GetStats();

    private:
        static void StartBatch();
        static void NextBatch();
    };

}
