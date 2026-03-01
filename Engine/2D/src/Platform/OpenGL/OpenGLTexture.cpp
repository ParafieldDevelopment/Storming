#include "Platform/OpenGL/OpenGLTexture.hpp"
#include <stb_image.h>
#include <iostream>
#include <filesystem>

namespace Storming {

    OpenGLTexture2D::OpenGLTexture2D(uint32_t width, uint32_t height)
        : m_Width(width), m_Height(height)
    {
        m_InternalFormat = GL_RGBA8;
        m_DataFormat = GL_RGBA;

        glGenTextures(1, &m_RendererID);
        glBindTexture(GL_TEXTURE_2D, m_RendererID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexImage2D(GL_TEXTURE_2D, 0, m_InternalFormat, m_Width, m_Height, 0, m_DataFormat, GL_UNSIGNED_BYTE, nullptr);
    }

    OpenGLTexture2D::OpenGLTexture2D(const std::string& path)
        : m_Path(path)
    {
        int width, height, channels;
        stbi_set_flip_vertically_on_load(1);
        
        std::string absPath = std::filesystem::absolute(path).string();
        std::cout << "[Texture] Loading: " << path << " (Abs: " << absPath << ")" << std::endl;

        stbi_uc* data = stbi_load(path.c_str(), &width, &height, &channels, 0);

        if (data) {
            m_Width = width;
            m_Height = height;

            GLenum internalFormat = 0, dataFormat = 0;
            if (channels == 4) {
                internalFormat = GL_RGBA8;
                dataFormat = GL_RGBA;
            } else if (channels == 3) {
                internalFormat = GL_RGB8;
                dataFormat = GL_RGB;
            }

            m_InternalFormat = internalFormat;
            m_DataFormat = dataFormat;

            if (internalFormat == 0 || dataFormat == 0) {
                std::cerr << "[Texture] Unsupported texture format! Channels: " << channels << std::endl;
                stbi_image_free(data);
                return;
            }

            glGenTextures(1, &m_RendererID);
            glBindTexture(GL_TEXTURE_2D, m_RendererID);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glTexImage2D(GL_TEXTURE_2D, 0, m_InternalFormat, m_Width, m_Height, 0, m_DataFormat, GL_UNSIGNED_BYTE, data);

            stbi_image_free(data);
            std::cout << "[Texture] Successfully loaded: " << path << " (" << width << "x" << height << ")" << std::endl;
        } else {
            std::cerr << "[Texture] Failed to load image: " << path << " | Error: " << stbi_failure_reason() << std::endl;
        }
    }

    OpenGLTexture2D::~OpenGLTexture2D() {
        glDeleteTextures(1, &m_RendererID);
    }

    void OpenGLTexture2D::SetData(void* data, uint32_t size) {
        uint32_t bpp = m_DataFormat == GL_RGBA ? 4 : 3;
        if (size != m_Width * m_Height * bpp) {
            std::cerr << "[Texture] Data must be entire texture!" << std::endl;
            return;
        }
        glBindTexture(GL_TEXTURE_2D, m_RendererID);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, m_Width, m_Height, m_DataFormat, GL_UNSIGNED_BYTE, data);
    }

    void OpenGLTexture2D::Bind(uint32_t slot) const {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, m_RendererID);
    }

}
