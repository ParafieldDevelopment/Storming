#include "Rendering/FrameBuffer.hpp"
#include <iostream>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>

namespace Storming {

    /**
     * Constructs a FrameBuffer and sets up off-screen rendering.
     * Also initializes a Linux Shared Memory (POSIX) segment for IPC.
     * 
     * @param width The width of the viewport.
     * @param height The height of the viewport.
     * @param shmName The string identifier for the shared memory block.
     */
    FrameBuffer::FrameBuffer(uint32_t width, uint32_t height, const std::string& shmName)
        : m_Width(width), m_Height(height), m_ShmName(shmName) 
    {
        // 1. Create Framebuffer Object (FBO)
        glGenFramebuffers(1, &m_FBO);
        glBindFramebuffer(GL_FRAMEBUFFER, m_FBO);

        // 2. Create and attach color texture
        glGenTextures(1, &m_ColorAttachment);
        glBindTexture(GL_TEXTURE_2D, m_ColorAttachment);
        
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

        // 3. Initialize POSIX Shared Memory
        size_t size = m_Width * m_Height * 4; // RGBA8 = 4 bytes per pixel
        m_ShmFd = shm_open(m_ShmName.c_str(), O_CREAT | O_RDWR, 0666);
        if (m_ShmFd < 0) {
            std::cerr << "[Engine] ERROR: Failed to open SHM: " << m_ShmName << std::endl;
            return;
        }
        ftruncate(m_ShmFd, size);
        m_ShmPtr = mmap(0, size, PROT_WRITE, MAP_SHARED, m_ShmFd, 0);
        
        std::cout << "[Engine] Shared Memory initialized: " << m_ShmName << std::endl;
    }

    /**
     * Cleans up GPU resources and unlinks the shared memory segment.
     */
    FrameBuffer::~FrameBuffer() {
        glDeleteFramebuffers(1, &m_FBO);
        glDeleteTextures(1, &m_ColorAttachment);
        size_t size = m_Width * m_Height * 4;
        if (m_ShmPtr) munmap(m_ShmPtr, size);
        if (m_ShmFd >= 0) close(m_ShmFd);
        shm_unlink(m_ShmName.c_str());
    }

    /** Binds the framebuffer for drawing. Updates the viewport size. */
    void FrameBuffer::Bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, m_FBO);
        glViewport(0, 0, m_Width, m_Height);
    }

    /** Unbinds the framebuffer, returning focus to the default screen buffer. */
    void FrameBuffer::Unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Reads the GPU pixel data from the framebuffer into the shared memory pointer.
     * Uses GL_RGBA format for compatibility with Java's BufferedImage.
     */
    void FrameBuffer::CopyToSharedMemory() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, m_FBO);
        glReadPixels(0, 0, m_Width, m_Height, GL_RGBA, GL_UNSIGNED_BYTE, m_ShmPtr);
    }

}
