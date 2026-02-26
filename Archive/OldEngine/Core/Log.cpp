#include "Core/Log.h"

namespace Storming {
    void Log::Init() {
        std::cout << "[Log] Logger initialized.\n";
    }

    void Log::Info(const std::string& message) {
        std::cout << "[Info] " << message << std::endl;
    }

    void Log::Error(const std::string& message) {
        std::cerr << "[Error] " << message << std::endl;
    }
}
