#pragma once

#include <glad/glad.h>
#include <string>

namespace Storming {

    class FrameBuffer {
    public:
        FrameBuffer(uint32_t width, uint32_t height, const std::string& shmName);
        ~FrameBuffer();

        void Bind();
        void Unbind();
        void CopyToSharedMemory();

        uint32_t GetWidth() const { return m_Width; }
        uint32_t GetHeight() const { return m_Height; }

    private:
        uint32_t m_FBO = 0;
        uint32_t m_ColorAttachment = 0;
        uint32_t m_Width, m_Height;
        
        std::string m_ShmName;
        int m_ShmFd = -1;
        void* m_ShmPtr = nullptr;
    };

}
