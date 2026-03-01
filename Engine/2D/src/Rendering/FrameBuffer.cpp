#include "Rendering/FrameBuffer.hpp"
#include <iostream>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>

namespace Storming {

    FrameBuffer::FrameBuffer(uint32_t width, uint32_t height, const std::string& shmName)
        : m_Width(width), m_Height(height), m_ShmName(shmName) 
    {
        glGenFramebuffers(1, &m_FBO);
        glBindFramebuffer(GL_FRAMEBUFFER, m_FBO);

        glGenTextures(1, &m_ColorAttachment);
        glBindTexture(GL_TEXTURE_2D, m_ColorAttachment);
        
        // CRITICAL: Set texture parameters or the FBO will be incomplete
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, m_Width, m_Height, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, m_ColorAttachment, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            std::cerr << "[Engine] ERROR: FrameBuffer is incomplete!" << std::endl;
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        size_t size = m_Width * m_Height * 4;
        m_ShmFd = shm_open(m_ShmName.c_str(), O_CREAT | O_RDWR, 0666);
        if (m_ShmFd < 0) {
            std::cerr << "[Engine] ERROR: Failed to open SHM: " << m_ShmName << std::endl;
            return;
        }
        ftruncate(m_ShmFd, size);
        m_ShmPtr = mmap(0, size, PROT_WRITE, MAP_SHARED, m_ShmFd, 0);
        
        std::cout << "[Engine] Shared Memory initialized: " << m_ShmName << std::endl;
    }

    FrameBuffer::~FrameBuffer() {
        glDeleteFramebuffers(1, &m_FBO);
        glDeleteTextures(1, &m_ColorAttachment);
        size_t size = m_Width * m_Height * 4;
        if (m_ShmPtr) munmap(m_ShmPtr, size);
        if (m_ShmFd >= 0) close(m_ShmFd);
        shm_unlink(m_ShmName.c_str());
    }

    void FrameBuffer::Bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, m_FBO);
        glViewport(0, 0, m_Width, m_Height);
    }

    void FrameBuffer::Unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    void FrameBuffer::CopyToSharedMemory() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, m_FBO);
        glReadPixels(0, 0, m_Width, m_Height, GL_RGBA, GL_UNSIGNED_BYTE, m_ShmPtr);
    }

}
