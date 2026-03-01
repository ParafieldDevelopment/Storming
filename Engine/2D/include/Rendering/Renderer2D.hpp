#pragma once

#include <glm/glm.hpp>
#include "Rendering/OrthographicCamera.hpp"

namespace Storming {

    struct Vertex {
        glm::vec3 Position;
        glm::vec4 Color;
    };

    class Renderer2D {
    public:
        struct Statistics {
            uint32_t DrawCalls = 0;
            uint32_t QuadCount = 0;
        };

        static void Init();
        static void Shutdown();

        static void BeginScene(const OrthographicCamera& camera);
        static void EndScene();
        static void Flush();

        static void DrawQuad(const glm::vec3& position, const glm::vec2& size, const glm::vec4& color);

        static Statistics GetStats();
        static void ResetStats();

    private:
        static void StartBatch();
        static void NextBatch();
    };

}
