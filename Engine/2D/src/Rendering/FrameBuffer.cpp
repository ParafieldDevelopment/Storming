#include "Rendering/FrameBuffer.hpp"
#include <iostream>
#include "Core/Log.hpp" // Include for GL_CHECK_ERROR

#ifdef _WIN32
    #include <windows.h>
    #include <memoryapi.h>
#else
    #include <fcntl.h>
    #include <sys/mman.h>
    #include <unistd.h>
#endif

namespace Storming {

    FrameBuffer::FrameBuffer(uint32_t width, uint32_t height, const std::string& shmName)
        : m_Width(width), m_Height(height), m_ShmName(shmName) 
    {
        // 1. Create OpenGL FBO
        glCreateFramebuffers(1, &m_FBO);
        glBindFramebuffer(GL_FRAMEBUFFER, m_FBO);
        GL_CHECK_ERROR();

        glCreateTextures(GL_TEXTURE_2D, 1, &m_ColorAttachment);
        glBindTexture(GL_TEXTURE_2D, m_ColorAttachment);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, m_Width, m_Height, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, m_ColorAttachment, 0);
        GL_CHECK_ERROR();

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            std::cerr << "[Engine] FrameBuffer is incomplete!" << std::endl;
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL_CHECK_ERROR();

        // 2. Setup Shared Memory (Platform-specific)
        size_t size = m_Width * m_Height * 4; // RGBA

    #ifdef _WIN32
        m_ShmHandle = CreateFileMappingA(
            INVALID_HANDLE_VALUE,    // Use paging file
            NULL,                    // Default security
            PAGE_READWRITE,          // Read/write access
            0,                       // Maximum object size (high-order DWORD)
            static_cast<DWORD>(size),// Maximum object size (low-order DWORD)
            m_ShmName.c_str());      // Name of mapping object

        if (m_ShmHandle == NULL) {
            std::cerr << "[Engine] CreateFileMappingA failed: " << GetLastError() << std::endl;
            return;
        }

        m_ShmPtr = MapViewOfFile(
            m_ShmHandle,             // Handle to map object
            FILE_MAP_ALL_ACCESS,     // Read/write permission
            0,
            0,
            size);

        if (m_ShmPtr == NULL) {
            std::cerr << "[Engine] MapViewOfFile failed: " << GetLastError() << std::endl;
            CloseHandle(m_ShmHandle);
            m_ShmHandle = NULL;
            return;
        }
    #else // POSIX
        m_ShmFd = shm_open(m_ShmName.c_str(), O_CREAT | O_RDWR, 0666);
        if (m_ShmFd < 0) {
            std::cerr << "[Engine] shm_open failed: " << errno << std::endl;
            return;
        }
        ftruncate(m_ShmFd, size);
        m_ShmPtr = mmap(0, size, PROT_WRITE, MAP_SHARED, m_ShmFd, 0);
        if (m_ShmPtr == MAP_FAILED) {
            std::cerr << "[Engine] mmap failed: " << errno << std::endl;
            close(m_ShmFd);
            m_ShmFd = -1;
            return;
        }
    #endif
        std::cout << "[Engine] Shared Memory initialized: " << m_ShmName << std::endl;
    }

    FrameBuffer::~FrameBuffer() {
        glDeleteFramebuffers(1, &m_FBO);
        glDeleteTextures(1, &m_ColorAttachment);
        
        size_t size = m_Width * m_Height * 4;

    #ifdef _WIN32
        if (m_ShmPtr) {
            UnmapViewOfFile(m_ShmPtr);
        }
        if (m_ShmHandle) {
            CloseHandle(m_ShmHandle);
        }
    #else // POSIX
        if (m_ShmPtr != MAP_FAILED && m_ShmPtr != nullptr) {
            munmap(m_ShmPtr, size);
        }
        if (m_ShmFd != -1) {
            close(m_ShmFd);
            shm_unlink(m_ShmName.c_str());
        }
    #endif
    }

    void FrameBuffer::Bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, m_FBO);
        glViewport(0, 0, m_Width, m_Height);
        GL_CHECK_ERROR();
    }

    void FrameBuffer::Unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GL_CHECK_ERROR();
    }

    void FrameBuffer::CopyToSharedMemory() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, m_FBO);
        glReadPixels(0, 0, m_Width, m_Height, GL_RGBA, GL_UNSIGNED_BYTE, m_ShmPtr);
        GL_CHECK_ERROR();
    }

}
