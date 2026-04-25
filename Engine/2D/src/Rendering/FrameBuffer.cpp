#include "Rendering/FrameBuffer.hpp"
#include <iostream>
#include <cstring>

#ifdef _WIN32
#include <windows.h>
#else
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#endif

namespace Storming {

    /**
     * Constructs a FrameBuffer and sets up off-screen rendering.
     * Also initializes a Cross-Platform Shared Memory segment for IPC.
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

        // 2b. Initialize Pixel Buffer Object (PBO) for async readback
        glGenBuffers(1, &m_PBO);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, m_PBO);
        glBufferData(GL_PIXEL_PACK_BUFFER, (size_t)m_Width * m_Height * 4, nullptr, GL_STREAM_READ);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

        // 3. Initialize Shared Memory
        if (!m_ShmName.empty()) {
            size_t size = (size_t)m_Width * m_Height * 4;
            
#ifdef _WIN32
            m_ShmHandle = CreateFileMappingA(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, (DWORD)size, m_ShmName.c_str());
            if (!m_ShmHandle) {
                std::cerr << "[Engine] ERROR: Failed to create SHM '" << m_ShmName << "': " << GetLastError() << std::endl;
                return;
            }

            m_ShmPtr = MapViewOfFile(m_ShmHandle, FILE_MAP_ALL_ACCESS, 0, 0, size);
            if (!m_ShmPtr) {
                std::cerr << "[Engine] ERROR: Failed to map SHM: " << GetLastError() << std::endl;
                CloseHandle(m_ShmHandle);
                m_ShmHandle = nullptr;
                return;
            }
#else
            // Try to open existing first to avoid permission issues if zombie exists
            m_ShmFd = shm_open(m_ShmName.c_str(), O_RDWR | O_CREAT, 0666);
            if (m_ShmFd < 0) {
                std::cerr << "[Engine] ERROR: Failed to open SHM '" << m_ShmName << "': " << strerror(errno) << std::endl;
                return;
            }

            if (ftruncate(m_ShmFd, size) == -1) {
                std::cerr << "[Engine] ERROR: Failed to truncate SHM: " << strerror(errno) << std::endl;
                close(m_ShmFd);
                m_ShmFd = -1;
                return;
            }

            m_ShmPtr = mmap(0, size, PROT_WRITE, MAP_SHARED, m_ShmFd, 0);
            if (m_ShmPtr == MAP_FAILED) {
                std::cerr << "[Engine] ERROR: Failed to map SHM: " << strerror(errno) << std::endl;
                m_ShmPtr = nullptr;
                close(m_ShmFd);
                m_ShmFd = -1;
                return;
            }
#endif
            
            std::cout << "[Engine] Shared Memory initialized: " << m_ShmName << " (" << size << " bytes)" << std::endl;
        }
    }

    FrameBuffer::~FrameBuffer() {
        glDeleteFramebuffers(1, &m_FBO);
        glDeleteTextures(1, &m_ColorAttachment);
        glDeleteBuffers(1, &m_PBO);
        
#ifdef _WIN32
        if (m_ShmPtr) UnmapViewOfFile(m_ShmPtr);
        if (m_ShmHandle) CloseHandle(m_ShmHandle);
#else
        if (m_ShmPtr && m_ShmPtr != MAP_FAILED) {
            size_t size = (size_t)m_Width * m_Height * 4;
            munmap(m_ShmPtr, size);
        }
        if (m_ShmFd >= 0) {
            close(m_ShmFd);
        }
        if (!m_ShmName.empty()) {
            shm_unlink(m_ShmName.c_str());
        }
#endif
    }

    void FrameBuffer::Bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, m_FBO);
        glViewport(0, 0, m_Width, m_Height);
    }

    void FrameBuffer::Unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    void FrameBuffer::Resize(uint32_t width, uint32_t height) {
        if (width == 0 || height == 0) return;
        m_Width = width;
        m_Height = height;

        // 1. Resize GPU Texture
        glBindTexture(GL_TEXTURE_2D, m_ColorAttachment);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, m_Width, m_Height, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
        glBindTexture(GL_TEXTURE_2D, 0);

        // 1b. Resize PBO
        glBindBuffer(GL_PIXEL_PACK_BUFFER, m_PBO);
        glBufferData(GL_PIXEL_PACK_BUFFER, (size_t)m_Width * m_Height * 4, nullptr, GL_STREAM_READ);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

        // 2. Re-map SHM
        if (!m_ShmName.empty()) {
            size_t size = (size_t)m_Width * m_Height * 4;
            
#ifdef _WIN32
            if (m_ShmPtr) UnmapViewOfFile(m_ShmPtr);
            if (m_ShmHandle) CloseHandle(m_ShmHandle);

            m_ShmHandle = CreateFileMappingA(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, (DWORD)size, m_ShmName.c_str());
            m_ShmPtr = MapViewOfFile(m_ShmHandle, FILE_MAP_ALL_ACCESS, 0, 0, size);
#else
            // Clean up old mapping
            if (m_ShmPtr && m_ShmPtr != MAP_FAILED) munmap(m_ShmPtr, size);
            
            // Note: We keep the same m_ShmFd if possible, but ftruncate it
            if (m_ShmFd >= 0) {
                if (ftruncate(m_ShmFd, size) == -1) {
                    std::cerr << "[Engine] ERROR: Failed to truncate SHM on resize: " << strerror(errno) << std::endl;
                }
                m_ShmPtr = mmap(0, size, PROT_WRITE, MAP_SHARED, m_ShmFd, 0);
                if (m_ShmPtr == MAP_FAILED) {
                    std::cerr << "[Engine] ERROR: Failed to map SHM on resize: " << strerror(errno) << std::endl;
                    m_ShmPtr = nullptr;
                }
            }
#endif
            std::cout << "[Engine] Shared Memory Resized: " << m_ShmName << " (" << size << " bytes)" << std::endl;
        }
    }

    void FrameBuffer::CopyToSharedMemory() {
#ifdef _WIN32
        if (!m_ShmPtr) return;
#else
        if (!m_ShmPtr || m_ShmPtr == MAP_FAILED) return;
#endif

        // 1. Trigger Async Read from FBO to PBO
        glBindFramebuffer(GL_READ_FRAMEBUFFER, m_FBO);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, m_PBO);
        
        // This call is non-blocking when reading into a PBO
        glReadPixels(0, 0, m_Width, m_Height, GL_BGRA, GL_UNSIGNED_BYTE, 0);

        // 2. Map the PBO to CPU memory and copy to SHM
        // Note: In a fully optimized dual-PBO setup, we would map the PBO from the PREVIOUS frame
        // to avoid any stall. For now, even a single PBO is faster than direct glReadPixels.
        void* pboPtr = glMapBuffer(GL_PIXEL_PACK_BUFFER, GL_READ_ONLY);
        if (pboPtr) {
            memcpy(m_ShmPtr, pboPtr, (size_t)m_Width * m_Height * 4);
            glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
        }

        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    }

}
