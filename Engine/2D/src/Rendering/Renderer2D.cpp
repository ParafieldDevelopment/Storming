#include "Rendering/Renderer2D.hpp"
#include "Rendering/Shader.hpp"
#include <glad/glad.h>
#include <memory>
#include <array>
#include <iostream>

namespace Storming {

    struct Renderer2DData {
        static const uint32_t MaxQuads = 10000;
        static const uint32_t MaxVertices = MaxQuads * 4;
        static const uint32_t MaxIndices = MaxQuads * 6;
        static const uint32_t MaxTextureSlots = 16;

        GLuint QuadVAO = 0;
        GLuint QuadVBO = 0;
        GLuint QuadIBO = 0;
        std::unique_ptr<Shader> QuadShader;
        std::shared_ptr<Texture2D> WhiteTexture;

        uint32_t QuadIndexCount = 0;
        Vertex* QuadVertexBufferBase = nullptr;
        Vertex* QuadVertexBufferPtr = nullptr;

        std::array<std::shared_ptr<Texture2D>, MaxTextureSlots> TextureSlots;
        uint32_t TextureSlotIndex = 1;

        Renderer2D::Statistics Stats;
    };

    static Renderer2DData s_Data;

    void Renderer2D::Init() {
        s_Data.QuadVertexBufferBase = new Vertex[s_Data.MaxVertices];

        glGenVertexArrays(1, &s_Data.QuadVAO);
        glBindVertexArray(s_Data.QuadVAO);

        glGenBuffers(1, &s_Data.QuadVBO);
        glBindBuffer(GL_ARRAY_BUFFER, s_Data.QuadVBO);
        glBufferData(GL_ARRAY_BUFFER, s_Data.MaxVertices * sizeof(Vertex), nullptr, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (const void*)0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, GL_FALSE, sizeof(Vertex), (const void*)(sizeof(float) * 3));
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, GL_FALSE, sizeof(Vertex), (const void*)(sizeof(float) * 7));
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 1, GL_FLOAT, GL_FALSE, sizeof(Vertex), (const void*)(sizeof(float) * 9));

        uint32_t* quadIndices = new uint32_t[s_Data.MaxIndices];
        uint32_t offset = 0;
        for (uint32_t i = 0; i < s_Data.MaxIndices; i += 6) {
            quadIndices[i + 0] = offset + 0; quadIndices[i + 1] = offset + 1; quadIndices[i + 2] = offset + 2;
            quadIndices[i + 3] = offset + 2; quadIndices[i + 4] = offset + 3; quadIndices[i + 5] = offset + 0;
            offset += 4;
        }
        glGenBuffers(1, &s_Data.QuadIBO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, s_Data.QuadIBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, s_Data.MaxIndices * sizeof(uint32_t), quadIndices, GL_STATIC_DRAW);
        delete[] quadIndices;

        s_Data.WhiteTexture = Texture2D::Create(1, 1);
        uint32_t whiteTextureData = 0xffffffff;
        s_Data.WhiteTexture->SetData(&whiteTextureData, sizeof(uint32_t));

        const std::string vertexSrc = R"(
            #version 450 core
            layout (location = 0) in vec3 a_Pos;
            layout (location = 1) in vec4 a_Color;
            layout (location = 2) in vec2 a_TexCoord;
            layout (location = 3) in float a_TexIndex;
            uniform mat4 u_ViewProjection;
            out vec4 v_Color;
            out vec2 v_TexCoord;
            flat out int v_TexIndex;
            void main() {
                v_Color = a_Color;
                v_TexCoord = a_TexCoord;
                v_TexIndex = int(a_TexIndex + 0.1);
                gl_Position = u_ViewProjection * vec4(a_Pos, 1.0);
            }
        )";

        const std::string fragmentSrc = R"(
            #version 450 core
            layout (location = 0) out vec4 color;
            in vec4 v_Color;
            in vec2 v_TexCoord;
            flat in int v_TexIndex;
            uniform sampler2D u_Textures[16];
            void main() {
                color = texture(u_Textures[v_TexIndex], v_TexCoord) * v_Color;
            }
        )";

        s_Data.QuadShader = Shader::Create(vertexSrc, fragmentSrc);
        s_Data.QuadShader->Bind();
        
        int32_t samplers[16];
        for (int i = 0; i < 16; i++) samplers[i] = i;
        s_Data.QuadShader->SetIntArray("u_Textures[0]", samplers, 16);
        
        s_Data.TextureSlots[0] = s_Data.WhiteTexture;
    }

    void Renderer2D::Shutdown() {
        glDeleteVertexArrays(1, &s_Data.QuadVAO);
        glDeleteBuffers(1, &s_Data.QuadVBO);
        glDeleteBuffers(1, &s_Data.QuadIBO);
        delete[] s_Data.QuadVertexBufferBase;
    }

    void Renderer2D::BeginScene(const OrthographicCamera& camera) {
        s_Data.QuadShader->Bind();
        s_Data.QuadShader->SetMat4("u_ViewProjection", camera.GetViewProjectionMatrix());
        StartBatch();
    }

    void Renderer2D::EndScene() { Flush(); }

    void Renderer2D::StartBatch() {
        s_Data.QuadIndexCount = 0;
        s_Data.QuadVertexBufferPtr = s_Data.QuadVertexBufferBase;
        s_Data.TextureSlotIndex = 1;
    }

    void Renderer2D::Flush() {
        if (s_Data.QuadIndexCount == 0) return;
        uint32_t dataSize = (uint32_t)((uint8_t*)s_Data.QuadVertexBufferPtr - (uint8_t*)s_Data.QuadVertexBufferBase);
        glBindBuffer(GL_ARRAY_BUFFER, s_Data.QuadVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0, dataSize, s_Data.QuadVertexBufferBase);
        for (uint32_t i = 0; i < s_Data.TextureSlotIndex; i++) s_Data.TextureSlots[i]->Bind(i);
        s_Data.QuadShader->Bind();
        glBindVertexArray(s_Data.QuadVAO);
        glDrawElements(GL_TRIANGLES, s_Data.QuadIndexCount, GL_UNSIGNED_INT, nullptr);
        s_Data.Stats.DrawCalls++;
    }

    void Renderer2D::DrawQuad(const glm::vec3& position, const glm::vec2& size, const glm::vec4& color) {
        DrawQuad(position, size, s_Data.WhiteTexture, color);
    }

    void Renderer2D::DrawQuad(const glm::vec3& position, const glm::vec2& size, const std::shared_ptr<Texture2D>& texture, const glm::vec4& tintColor) {
        if (s_Data.QuadIndexCount >= s_Data.MaxIndices) NextBatch();
        float textureIndex = -1.0f;
        for (uint32_t i = 0; i < s_Data.TextureSlotIndex; i++) {
            if (s_Data.TextureSlots[i]->GetRendererID() == texture->GetRendererID()) {
                textureIndex = (float)i;
                break;
            }
        }
        if (textureIndex == -1.0f) {
            if (s_Data.TextureSlotIndex >= s_Data.MaxTextureSlots) NextBatch();
            textureIndex = (float)s_Data.TextureSlotIndex;
            s_Data.TextureSlots[s_Data.TextureSlotIndex] = texture;
            s_Data.TextureSlotIndex++;
        }
        s_Data.QuadVertexBufferPtr->Position = position;
        s_Data.QuadVertexBufferPtr->Color = tintColor;
        s_Data.QuadVertexBufferPtr->TexCoord = { 0.0f, 0.0f };
        s_Data.QuadVertexBufferPtr->TexIndex = textureIndex;
        s_Data.QuadVertexBufferPtr++;
        s_Data.QuadVertexBufferPtr->Position = { position.x + size.x, position.y, position.z };
        s_Data.QuadVertexBufferPtr->Color = tintColor;
        s_Data.QuadVertexBufferPtr->TexCoord = { 1.0f, 0.0f };
        s_Data.QuadVertexBufferPtr->TexIndex = textureIndex;
        s_Data.QuadVertexBufferPtr++;
        s_Data.QuadVertexBufferPtr->Position = { position.x + size.x, position.y + size.y, position.z };
        s_Data.QuadVertexBufferPtr->Color = tintColor;
        s_Data.QuadVertexBufferPtr->TexCoord = { 1.0f, 1.0f };
        s_Data.QuadVertexBufferPtr->TexIndex = textureIndex;
        s_Data.QuadVertexBufferPtr++;
        s_Data.QuadVertexBufferPtr->Position = { position.x, position.y + size.y, position.z };
        s_Data.QuadVertexBufferPtr->Color = tintColor;
        s_Data.QuadVertexBufferPtr->TexCoord = { 0.0f, 1.0f };
        s_Data.QuadVertexBufferPtr->TexIndex = textureIndex;
        s_Data.QuadVertexBufferPtr++;
        s_Data.QuadIndexCount += 6;
        s_Data.Stats.QuadCount++;
    }

    void Renderer2D::NextBatch() { Flush(); StartBatch(); }
    Renderer2D::Statistics Renderer2D::GetStats() { return s_Data.Stats; }
    void Renderer2D::ResetStats() { s_Data.Stats = {}; }
}
