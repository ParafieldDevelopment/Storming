#pragma once

#include <glm/glm.hpp>
#include "Rendering/OrthographicCamera.hpp"
#include "Rendering/Texture.hpp"

namespace Storming {

    struct Vertex {
        glm::vec3 Position;
        glm::vec4 Color;
        glm::vec2 TexCoord;
        float TexIndex;
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
        static void DrawQuad(const glm::vec3& position, const glm::vec2& size, const std::shared_ptr<Texture2D>& texture, const glm::vec4& tintColor = glm::vec4(1.0f));

        static void DrawLine(const glm::vec3& p0, const glm::vec3& p1, const glm::vec4& color);

        static Statistics GetStats();
        static void ResetStats();

    private:
        static void StartBatch();
        static void NextBatch();
    };

}
