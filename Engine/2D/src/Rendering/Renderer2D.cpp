#include "Rendering/Renderer2D.hpp"
#include <glad/glad.h>
#include <iostream>
#include "Core/Log.hpp" // Include Log.hpp for GL_CHECK_ERROR
#include <vector> // Required for std::vector in shader error logging

namespace Storming {

    struct Renderer2DData {
        static const uint32_t MaxQuads = 10000;
        static const uint32_t MaxVertices = MaxQuads * 4;
        static const uint32_t MaxIndices = MaxQuads * 6;

        GLuint QuadVAO = 0;
        GLuint QuadVBO = 0;
        GLuint QuadIBO = 0;
        GLuint WhiteShader = 0;

        uint32_t QuadIndexCount = 0;
        Vertex* QuadVertexBufferBase = nullptr;
        Vertex* QuadVertexBufferPtr = nullptr;

        Renderer2D::Statistics Stats;
    };

    static Renderer2DData s_Data;

    void Renderer2D::Init() {
        // Load GL functions
        if (!gladLoadGL()) {
            std::cerr << "[Renderer] Failed to initialize GLAD!" << std::endl;
            return;
        }
        GL_CHECK_ERROR();

        s_Data.QuadVertexBufferBase = new Vertex[s_Data.MaxVertices];

        glCreateVertexArrays(1, &s_Data.QuadVAO);
        glBindVertexArray(s_Data.QuadVAO);
        GL_CHECK_ERROR();

        glCreateBuffers(1, &s_Data.QuadVBO);
        glBindBuffer(GL_ARRAY_BUFFER, s_Data.QuadVBO);
        glBufferData(GL_ARRAY_BUFFER, s_Data.MaxVertices * sizeof(Vertex), nullptr, GL_DYNAMIC_DRAW);
        GL_CHECK_ERROR();

        glEnableVertexArrayAttrib(s_Data.QuadVAO, 0);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (const void*)offsetof(Vertex, Position));
        GL_CHECK_ERROR();

        glEnableVertexArrayAttrib(s_Data.QuadVAO, 1);
        glVertexAttribPointer(1, 4, GL_FLOAT, GL_FALSE, sizeof(Vertex), (const void*)offsetof(Vertex, Color));
        GL_CHECK_ERROR();

        uint32_t* quadIndices = new uint32_t[s_Data.MaxIndices];
        uint32_t offset = 0;
        for (uint32_t i = 0; i < s_Data.MaxIndices; i += 6) {
            quadIndices[i + 0] = offset + 0;
            quadIndices[i + 1] = offset + 1;
            quadIndices[i + 2] = offset + 2;
            quadIndices[i + 3] = offset + 2;
            quadIndices[i + 4] = offset + 3;
            quadIndices[i + 5] = offset + 0;
            offset += 4;
        }

        glCreateBuffers(1, &s_Data.QuadIBO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, s_Data.QuadIBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, s_Data.MaxIndices * sizeof(uint32_t), quadIndices, GL_STATIC_DRAW);
        GL_CHECK_ERROR();
        delete[] quadIndices;

        // Simple Shader
        const char* vertexShaderSrc = R"(
            #version 450 core
            layout (location = 0) in vec3 a_Pos;
            layout (location = 1) in vec4 a_Color;
            out vec4 v_Color;
            void main() {
                v_Color = a_Color;
                gl_Position = vec4(a_Pos, 1.0);
            }
        )";

        const char* fragmentShaderSrc = R"(
            #version 450 core
            layout (location = 0) out vec4 color;
            in vec4 v_Color;
            void main() {
                color = v_Color;
            }
        )";

        GLuint vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, 1, &vertexShaderSrc, NULL);
        glCompileShader(vs);
        GL_CHECK_ERROR();
        GLint isCompiled = 0;
        glGetShaderiv(vs, GL_COMPILE_STATUS, &isCompiled);
        if(isCompiled == GL_FALSE) {
            GLint maxLength = 0;
            glGetShaderiv(vs, GL_INFO_LOG_LENGTH, &maxLength);
            std::vector<GLchar> errorLog(maxLength);
            glGetShaderInfoLog(vs, maxLength, &maxLength, &errorLog[0]);
            glDeleteShader(vs);
            std::cerr << "[Renderer] Vertex Shader compilation failed:\n" << errorLog.data() << std::endl;
            return;
        }

        GLuint fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, 1, &fragmentShaderSrc, NULL);
        glCompileShader(fs);
        GL_CHECK_ERROR();
        glGetShaderiv(fs, GL_COMPILE_STATUS, &isCompiled);
        if(isCompiled == GL_FALSE) {
            GLint maxLength = 0;
            glGetShaderiv(fs, GL_INFO_LOG_LENGTH, &maxLength);
            std::vector<GLchar> errorLog(maxLength);
            glGetShaderInfoLog(fs, maxLength, &maxLength, &errorLog[0]);
            glDeleteShader(fs);
            glDeleteShader(vs);
            std::cerr << "[Renderer] Fragment Shader compilation failed:\n" << errorLog.data() << std::endl;
            return;
        }

        s_Data.WhiteShader = glCreateProgram();
        glAttachShader(s_Data.WhiteShader, vs);
        glAttachShader(s_Data.WhiteShader, fs);
        glLinkProgram(s_Data.WhiteShader);
        GL_CHECK_ERROR();
        GLint isLinked = 0;
        glGetProgramiv(s_Data.WhiteShader, GL_LINK_STATUS, (int*)&isLinked);
        if(isLinked == GL_FALSE) {
            GLint maxLength = 0;
            glGetProgramiv(s_Data.WhiteShader, GL_INFO_LOG_LENGTH, &maxLength);
            std::vector<GLchar> infoLog(maxLength);
            glGetProgramInfoLog(s_Data.WhiteShader, maxLength, &maxLength, &infoLog[0]);
            glDeleteProgram(s_Data.WhiteShader);
            glDeleteShader(vs);
            glDeleteShader(fs);
            std::cerr << "[Renderer] Shader Program linking failed:\n" << infoLog.data() << std::endl;
            return;
        }
        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    void Renderer2D::BeginScene() {
        StartBatch();
        GL_CHECK_ERROR();
    }

    void Renderer2D::EndScene() {
        Flush();
        GL_CHECK_ERROR();
    }

    void Renderer2D::StartBatch() {
        s_Data.QuadIndexCount = 0;
        s_Data.QuadVertexBufferPtr = s_Data.QuadVertexBufferBase;
    }

    void Renderer2D::Flush() {
        if (s_Data.QuadIndexCount == 0) return;

        uint32_t dataSize = (uint32_t)((uint8_t*)s_Data.QuadVertexBufferPtr - (uint8_t*)s_Data.QuadVertexBufferBase);
        glBindBuffer(GL_ARRAY_BUFFER, s_Data.QuadVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0, dataSize, s_Data.QuadVertexBufferBase);
        GL_CHECK_ERROR();

        glUseProgram(s_Data.WhiteShader);
        glBindVertexArray(s_Data.QuadVAO);
        std::cout << "[Renderer] Drawing " << s_Data.QuadIndexCount << " indices." << std::endl;
        glDrawElements(GL_TRIANGLES, s_Data.QuadIndexCount, GL_UNSIGNED_INT, nullptr);
        GL_CHECK_ERROR();
        s_Data.Stats.DrawCalls++;
    }

    void Renderer2D::DrawQuad(const glm::vec2& position, const glm::vec2& size, const glm::vec4& color) {
        if (s_Data.QuadIndexCount >= s_Data.MaxIndices) NextBatch();

        s_Data.QuadVertexBufferPtr->Position = { position.x, position.y, 0.0f };
        s_Data.QuadVertexBufferPtr->Color = color;
        s_Data.QuadVertexBufferPtr++;

        s_Data.QuadVertexBufferPtr->Position = { position.x + size.x, position.y, 0.0f };
        s_Data.QuadVertexBufferPtr->Color = color;
        s_Data.QuadVertexBufferPtr++;

        s_Data.QuadVertexBufferPtr->Position = { position.x + size.x, position.y + size.y, 0.0f };
        s_Data.QuadVertexBufferPtr->Color = color;
        s_Data.QuadVertexBufferPtr++;

        s_Data.QuadVertexBufferPtr->Position = { position.x, position.y + size.y, 0.0f };
        s_Data.QuadVertexBufferPtr->Color = color;
        s_Data.QuadVertexBufferPtr++;

        s_Data.QuadIndexCount += 6;
        s_Data.Stats.QuadCount++;
    }

    void Renderer2D::NextBatch() {
        Flush();
        StartBatch();
    }

    void Renderer2D::Shutdown() {
        delete[] s_Data.QuadVertexBufferBase;
    }

}
