//
// Created by Batista on 9/11/2025.
//

#ifndef STORMING_LOG_H
#define STORMING_LOG_H

#pragma once
#include <iostream>
#include <glad/glad.h> // Include GLAD for glGetError

namespace Storming {
    class Log {
    public:
        static void Init();
        static void Info(const std::string& message);
        static void Error(const std::string& message);
    };
}

#define GL_CHECK_ERROR() \
    while (GLenum error = glGetError()) { \
        std::cerr << "[OpenGL Error] " << error << " in " << __FILE__ << ":" << __LINE__ << std::endl; \
    }


#endif //STORMING_LOG_H