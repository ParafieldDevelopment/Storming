#pragma once

#include <glad/glad.h>
#include <string>

namespace Storming {

    /**
     * Manages an off-screen render target.
     * In Editor Mode, this class handles the high-speed transfer of pixels 
     * from the GPU to a Shared Memory (SHM) segment accessible by the Java Editor.
     */
    class FrameBuffer {
    public:
        /**
         * Creates a FrameBuffer and initializes a named shared memory segment.
         * @param shmName The unique name for the SHM segment (must match Editor's key).
         */
        FrameBuffer(uint32_t width, uint32_t height, const std::string& shmName);
        ~FrameBuffer();

        /** Directs all subsequent draw calls to this buffer instead of the screen. */
        void Bind();
        /** Switches rendering back to the default screen buffer. */
        void Unbind();

        /**
         * Reads pixels back from the GPU and copies them into the SHM segment.
         * This is typically called once per frame in Editor Mode.
         */
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
