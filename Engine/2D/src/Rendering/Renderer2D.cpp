#include "Rendering/Renderer2D.hpp"
#include "Rendering/Shader.hpp"
#include <glad/glad.h>
#include <memory>
#include <array>
#include <iostream>

namespace Storming {

    struct LineVertex {
        glm::vec3 Position;
        glm::vec4 Color;
    };

    struct Renderer2DData {
        static const uint32_t MaxQuads = 10000;
        static const uint32_t MaxVertices = MaxQuads * 4;
        static const uint32_t MaxIndices = MaxQuads * 6;
        static const uint32_t MaxTextureSlots = 16; 

        static const uint32_t MaxLines = 10000;
        static const uint32_t MaxLineVertices = MaxLines * 2;

        std::shared_ptr<VertexArray> QuadVertexArray;
        std::shared_ptr<VertexBuffer> QuadVertexBuffer;
        std::shared_ptr<IndexBuffer> QuadIndexBuffer;
        
        std::unique_ptr<Shader> QuadShader;
        std::shared_ptr<Texture2D> WhiteTexture;

        uint32_t QuadIndexCount = 0;
        Vertex* QuadVertexBufferBase = nullptr;
        Vertex* QuadVertexBufferPtr = nullptr;

        std::array<std::shared_ptr<Texture2D>, MaxTextureSlots> TextureSlots;
        uint32_t TextureSlotIndex = 1; // 0 = White Texture

        // Lines
        std::shared_ptr<VertexArray> LineVertexArray;
        std::shared_ptr<VertexBuffer> LineVertexBuffer;
        std::unique_ptr<Shader> LineShader;
        uint32_t LineVertexCount = 0;
        LineVertex* LineVertexBufferBase = nullptr;
        LineVertex* LineVertexBufferPtr = nullptr;

        Renderer2D::Statistics Stats;
    };

    static Renderer2DData s_Data;

    static const std::string QuadVertexSrc = R"(
        #version 450 core
        layout(location = 0) in vec3 a_Position;
        layout(location = 1) in vec4 a_Color;
        layout(location = 2) in vec2 a_TexCoord;
        layout(location = 3) in float a_TexIndex;

        uniform mat4 u_ViewProjection;

        out vec4 v_Color;
        out vec2 v_TexCoord;
        out float v_TexIndex;

        void main() {
            v_Color = a_Color;
            v_TexCoord = a_TexCoord;
            v_TexIndex = a_TexIndex;
            gl_Position = u_ViewProjection * vec4(a_Position, 1.0);
        }
    )";

    static const std::string QuadFragmentSrc = R"(
        #version 450 core
        layout(location = 0) out vec4 color;

        in vec4 v_Color;
        in vec2 v_TexCoord;
        in float v_TexIndex;

        uniform sampler2D u_Textures[16];

        void main() {
            int index = int(v_TexIndex);
            color = v_Color * texture(u_Textures[index], v_TexCoord);
        }
    )";

    static const std::string LineVertexSrc = R"(
        #version 450 core
        layout(location = 0) in vec3 a_Position;
        layout(location = 1) in vec4 a_Color;

        uniform mat4 u_ViewProjection;
        out vec4 v_Color;

        void main() {
            v_Color = a_Color;
            gl_Position = u_ViewProjection * vec4(a_Position, 1.0);
        }
    )";

    static const std::string LineFragmentSrc = R"(
        #version 450 core
        layout(location = 0) out vec4 color;
        in vec4 v_Color;

        void main() {
            color = v_Color;
        }
    )";

    void Renderer2D::Init() {
        s_Data.QuadVertexBufferBase = new Vertex[s_Data.MaxVertices];

        s_Data.QuadVertexArray = RendererAPI::CreateVertexArray();
        
        s_Data.QuadVertexBuffer = RendererAPI::CreateVertexBuffer(nullptr, s_Data.MaxVertices * sizeof(Vertex));
        
        BufferLayout layout = {
            { ShaderDataType::Float3, "a_Position" },
            { ShaderDataType::Float4, "a_Color" },
            { ShaderDataType::Float2, "a_TexCoord" },
            { ShaderDataType::Float,  "a_TexIndex" }
        };
        s_Data.QuadVertexBuffer->SetLayout(layout);
        s_Data.QuadVertexArray->AddVertexBuffer(s_Data.QuadVertexBuffer);

        uint32_t* quadIndices = new uint32_t[s_Data.MaxIndices];
        uint32_t offset = 0;
        for (uint32_t i = 0; i < s_Data.MaxIndices; i += 6) {
            quadIndices[i + 0] = offset + 0; quadIndices[i + 1] = offset + 1; quadIndices[i + 2] = offset + 2;
            quadIndices[i + 3] = offset + 2; quadIndices[i + 4] = offset + 3; quadIndices[i + 5] = offset + 0;
            offset += 4;
        }
        s_Data.QuadIndexBuffer = RendererAPI::CreateIndexBuffer(quadIndices, s_Data.MaxIndices);
        s_Data.QuadVertexArray->SetIndexBuffer(s_Data.QuadIndexBuffer);
        delete[] quadIndices;

        // Lines
        s_Data.LineVertexBufferBase = new LineVertex[s_Data.MaxLineVertices];
        s_Data.LineVertexArray = RendererAPI::CreateVertexArray();
        s_Data.LineVertexBuffer = RendererAPI::CreateVertexBuffer(nullptr, s_Data.MaxLineVertices * sizeof(LineVertex));
        
        BufferLayout lineLayout = {
            { ShaderDataType::Float3, "a_Position" },
            { ShaderDataType::Float4, "a_Color" }
        };
        s_Data.LineVertexBuffer->SetLayout(lineLayout);
        s_Data.LineVertexArray->AddVertexBuffer(s_Data.LineVertexBuffer);

        s_Data.WhiteTexture = Texture2D::Create(1, 1);
        uint32_t whiteTextureData = 0xffffffff;
        s_Data.WhiteTexture->SetData(&whiteTextureData, sizeof(uint32_t));

        s_Data.QuadShader = Shader::Create(QuadVertexSrc, QuadFragmentSrc);
        s_Data.LineShader = Shader::Create(LineVertexSrc, LineFragmentSrc);

        if (s_Data.QuadShader) {
            int32_t samplers[s_Data.MaxTextureSlots];
            for (uint32_t i = 0; i < s_Data.MaxTextureSlots; i++) samplers[i] = i;
            s_Data.QuadShader->Bind();
            s_Data.QuadShader->SetIntArray("u_Textures", samplers, s_Data.MaxTextureSlots);
        }

        s_Data.TextureSlots[0] = s_Data.WhiteTexture;
    }

    void Renderer2D::Shutdown() {
        glDeleteVertexArrays(1, &s_Data.QuadVAO);
        glDeleteBuffers(1, &s_Data.QuadVBO);
        glDeleteBuffers(1, &s_Data.QuadIBO);
        delete[] s_Data.QuadVertexBufferBase;
        delete[] s_Data.LineVertexBufferBase;
    }

    void Renderer2D::BeginScene(const OrthographicCamera& camera) {
        if (s_Data.QuadShader) {
            s_Data.QuadShader->Bind();
            s_Data.QuadShader->SetMat4("u_ViewProjection", camera.GetViewProjectionMatrix());
        }
        
        if (s_Data.LineShader) {
            s_Data.LineShader->Bind();
            s_Data.LineShader->SetMat4("u_ViewProjection", camera.GetViewProjectionMatrix());
        }

        StartBatch();
    }

    void Renderer2D::EndScene() { Flush(); }

    void Renderer2D::StartBatch() {
        s_Data.QuadIndexCount = 0;
        s_Data.QuadVertexBufferPtr = s_Data.QuadVertexBufferBase;
        s_Data.TextureSlotIndex = 1;

        s_Data.LineVertexCount = 0;
        s_Data.LineVertexBufferPtr = s_Data.LineVertexBufferBase;
    }

    void Renderer2D::Flush() {
        if (s_Data.QuadIndexCount > 0 && s_Data.QuadShader) {
            uint32_t dataSize = (uint32_t)((uint8_t*)s_Data.QuadVertexBufferPtr - (uint8_t*)s_Data.QuadVertexBufferBase);
            glBindBuffer(GL_ARRAY_BUFFER, s_Data.QuadVBO);
            glBufferSubData(GL_ARRAY_BUFFER, 0, dataSize, s_Data.QuadVertexBufferBase);
            
            for (uint32_t i = 0; i < s_Data.TextureSlotIndex; i++) {
                if (s_Data.TextureSlots[i])
                    s_Data.TextureSlots[i]->Bind(i);
            }
            
            s_Data.QuadShader->Bind();
            glBindVertexArray(s_Data.QuadVAO);
            glDrawElements(GL_TRIANGLES, s_Data.QuadIndexCount, GL_UNSIGNED_INT, nullptr);
            s_Data.Stats.DrawCalls++;
        }

        if (s_Data.LineVertexCount > 0 && s_Data.LineShader) {
            uint32_t dataSize = (uint32_t)((uint8_t*)s_Data.LineVertexBufferPtr - (uint8_t*)s_Data.LineVertexBufferBase);
            glBindBuffer(GL_ARRAY_BUFFER, s_Data.LineVBO);
            glBufferSubData(GL_ARRAY_BUFFER, 0, dataSize, s_Data.LineVertexBufferBase);
            s_Data.LineShader->Bind();
            glBindVertexArray(s_Data.LineVAO);
            glDrawArrays(GL_LINES, 0, s_Data.LineVertexCount);
            s_Data.Stats.DrawCalls++;
        }
    }

    void Renderer2D::DrawQuad(const glm::vec3& position, const glm::vec2& size, const glm::vec4& color) {
        DrawQuad(position, size, s_Data.WhiteTexture, color);
    }

    void Renderer2D::DrawQuad(const glm::vec3& position, const glm::vec2& size, const std::shared_ptr<Texture2D>& texture, const glm::vec4& tintColor) {
        if (s_Data.QuadIndexCount >= s_Data.MaxIndices) NextBatch();
        
        float textureIndex = 0.0f;
        for (uint32_t i = 1; i < s_Data.TextureSlotIndex; i++) {
            if (s_Data.TextureSlots[i] && s_Data.TextureSlots[i]->GetRendererID() == texture->GetRendererID()) {
                textureIndex = (float)i;
                break;
            }
        }
        if (textureIndex == 0.0f) {
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

    void Renderer2D::DrawLine(const glm::vec3& p0, const glm::vec3& p1, const glm::vec4& color) {
        if (s_Data.LineVertexCount >= s_Data.MaxLineVertices) {
            Flush(); 
            s_Data.LineVertexCount = 0;
            s_Data.LineVertexBufferPtr = s_Data.LineVertexBufferBase;
        }
        s_Data.LineVertexBufferPtr->Position = p0;
        s_Data.LineVertexBufferPtr->Color = color;
        s_Data.LineVertexBufferPtr++;
        s_Data.LineVertexBufferPtr->Position = p1;
        s_Data.LineVertexBufferPtr->Color = color;
        s_Data.LineVertexBufferPtr++;
        s_Data.LineVertexCount += 2;
    }

    void Renderer2D::NextBatch() { Flush(); StartBatch(); }
    Renderer2D::Statistics Renderer2D::GetStats() { return s_Data.Stats; }
    void Renderer2D::ResetStats() { s_Data.Stats = {}; }
}
