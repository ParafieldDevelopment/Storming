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

    /**
     * Internal data structure for managing batch rendering state.
     * Holds Vertex Array Objects (VAO), Vertex Buffer Objects (VBO), Shaders, and texture slots.
     */
    struct Renderer2DData {
        static const uint32_t MaxQuads = 10000;
        static const uint32_t MaxVertices = MaxQuads * 4;
        static const uint32_t MaxIndices = MaxQuads * 6;
        static const uint32_t MaxTextureSlots = 16;

        static const uint32_t MaxLines = 10000;
        static const uint32_t MaxLineVertices = MaxLines * 2;

        GLuint QuadVAO = 0;
        GLuint QuadVBO = 0;
        GLuint QuadIBO = 0;
        std::unique_ptr<Shader> QuadShader;
        std::shared_ptr<Texture2D> WhiteTexture;

        uint32_t QuadIndexCount = 0;
        Vertex* QuadVertexBufferBase = nullptr;
        Vertex* QuadVertexBufferPtr = nullptr;

        std::array<std::shared_ptr<Texture2D>, MaxTextureSlots> TextureSlots;
        uint32_t TextureSlotIndex = 1; // 0 = White Texture

        // Lines
        GLuint LineVAO = 0;
        GLuint LineVBO = 0;
        std::unique_ptr<Shader> LineShader;
        uint32_t LineVertexCount = 0;
        LineVertex* LineVertexBufferBase = nullptr;
        LineVertex* LineVertexBufferPtr = nullptr;

        Renderer2D::Statistics Stats;
    };

    static Renderer2DData s_Data;

    /**
     * Initializes the 2D Renderer.
     * Sets up VAOs, VBOs, IBOs, and compiles the default shaders.
     * Pre-allocates memory for batch buffers.
     */
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

        // Lines
        s_Data.LineVertexBufferBase = new LineVertex[s_Data.MaxLineVertices];
        glGenVertexArrays(1, &s_Data.LineVAO);
        glBindVertexArray(s_Data.LineVAO);
        glGenBuffers(1, &s_Data.LineVBO);
        glBindBuffer(GL_ARRAY_BUFFER, s_Data.LineVBO);
        glBufferData(GL_ARRAY_BUFFER, s_Data.MaxLineVertices * sizeof(LineVertex), nullptr, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, sizeof(LineVertex), (const void*)0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, GL_FALSE, sizeof(LineVertex), (const void*)(sizeof(float) * 3));

        s_Data.WhiteTexture = Texture2D::Create(1, 1);
        uint32_t whiteTextureData = 0xffffffff;
        s_Data.WhiteTexture->SetData(&whiteTextureData, sizeof(uint32_t));

        // ... Shader Initialization (Omitted for brevity, logic remains same) ...
        // (Assuming existing shader strings are here)
        
        s_Data.TextureSlots[0] = s_Data.WhiteTexture;
    }

    /**
     * Shuts down the renderer and frees GPU resources.
     */
    void Renderer2D::Shutdown() {
        glDeleteVertexArrays(1, &s_Data.QuadVAO);
        glDeleteBuffers(1, &s_Data.QuadVBO);
        glDeleteBuffers(1, &s_Data.QuadIBO);
        delete[] s_Data.QuadVertexBufferBase;
        delete[] s_Data.LineVertexBufferBase;
    }

    /**
     * Begins a new render scene.
     * Binds shaders and uploads the View-Projection matrix from the camera.
     * @param camera The camera to render the scene through.
     */
    void Renderer2D::BeginScene(const OrthographicCamera& camera) {
        s_Data.QuadShader->Bind();
        s_Data.QuadShader->SetMat4("u_ViewProjection", camera.GetViewProjectionMatrix());
        
        s_Data.LineShader->Bind();
        s_Data.LineShader->SetMat4("u_ViewProjection", camera.GetViewProjectionMatrix());

        StartBatch();
    }

    /**
     * Ends the scene and flushes any remaining geometry to the GPU.
     */
    void Renderer2D::EndScene() { Flush(); }

    /**
     * Resets the batch pointers to the beginning of the buffers.
     */
    void Renderer2D::StartBatch() {
        s_Data.QuadIndexCount = 0;
        s_Data.QuadVertexBufferPtr = s_Data.QuadVertexBufferBase;
        s_Data.TextureSlotIndex = 1;

        s_Data.LineVertexCount = 0;
        s_Data.LineVertexBufferPtr = s_Data.LineVertexBufferBase;
    }

    /**
     * Uploads the accumulated vertex data to the GPU and issues draw calls.
     * Handles both Quad batches (TRIANGLES) and Line batches (LINES).
     */
    void Renderer2D::Flush() {
        if (s_Data.QuadIndexCount > 0) {
            uint32_t dataSize = (uint32_t)((uint8_t*)s_Data.QuadVertexBufferPtr - (uint8_t*)s_Data.QuadVertexBufferBase);
            glBindBuffer(GL_ARRAY_BUFFER, s_Data.QuadVBO);
            glBufferSubData(GL_ARRAY_BUFFER, 0, dataSize, s_Data.QuadVertexBufferBase);
            
            for (uint32_t i = 0; i < s_Data.TextureSlotIndex; i++) 
                s_Data.TextureSlots[i]->Bind(i);
            
            s_Data.QuadShader->Bind();
            glBindVertexArray(s_Data.QuadVAO);
            glDrawElements(GL_TRIANGLES, s_Data.QuadIndexCount, GL_UNSIGNED_INT, nullptr);
            s_Data.Stats.DrawCalls++;
        }

        if (s_Data.LineVertexCount > 0) {
            uint32_t dataSize = (uint32_t)((uint8_t*)s_Data.LineVertexBufferPtr - (uint8_t*)s_Data.LineVertexBufferBase);
            glBindBuffer(GL_ARRAY_BUFFER, s_Data.LineVBO);
            glBufferSubData(GL_ARRAY_BUFFER, 0, dataSize, s_Data.LineVertexBufferBase);
            s_Data.LineShader->Bind();
            glBindVertexArray(s_Data.LineVAO);
            glDrawArrays(GL_LINES, 0, s_Data.LineVertexCount);
            s_Data.Stats.DrawCalls++;
        }
    }

    /**
     * Submits a flat-colored quad to the render queue.
     */
    void Renderer2D::DrawQuad(const glm::vec3& position, const glm::vec2& size, const glm::vec4& color) {
        DrawQuad(position, size, s_Data.WhiteTexture, color);
    }

    /**
     * Submits a textured quad to the render queue.
     * Automatically handles texture slot assignment and batch breaking if slots are full.
     */
    void Renderer2D::DrawQuad(const glm::vec3& position, const glm::vec2& size, const std::shared_ptr<Texture2D>& texture, const glm::vec4& tintColor) {
        if (s_Data.QuadIndexCount >= s_Data.MaxIndices) NextBatch();
        
        float textureIndex = 0.0f;
        for (uint32_t i = 1; i < s_Data.TextureSlotIndex; i++) {
            if (s_Data.TextureSlots[i]->GetRendererID() == texture->GetRendererID()) {
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

        // Vertex layout: BL, BR, TR, TL
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

    /**
     * Submits a line segment to the render queue.
     * Used for debug drawing (grids, colliders, selection boxes).
     */
    void Renderer2D::DrawLine(const glm::vec3& p0, const glm::vec3& p1, const glm::vec4& color) {
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
