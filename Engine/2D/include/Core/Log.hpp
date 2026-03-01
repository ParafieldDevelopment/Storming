#pragma once

#include <memory>
#include <string>
#include <iostream>

namespace Storming {

    class Log {
    public:
        enum class Level {
            Trace, Info, Warn, Error, Fatal
        };

        static void Init();

        template<typename... Args>
        static void Print(Level level, const std::string& fmt, Args&&... args) {
            // Simple implementation for now, could use spdlog later
            std::string prefix;
            switch (level) {
                case Level::Trace: prefix = "[Trace] "; break;
                case Level::Info:  prefix = "[Engine] "; break;
                case Level::Warn:  prefix = "[Warning] "; break;
                case Level::Error: prefix = "[Error] "; break;
                case Level::Fatal: prefix = "[Fatal] "; break;
            }
            std::cout << prefix << fmt << std::endl;
        }
    };

}

#define ST_TRACE(...) ::Storming::Log::Print(::Storming::Log::Level::Trace, __VA_ARGS__)
#define ST_INFO(...)  ::Storming::Log::Print(::Storming::Log::Level::Info, __VA_ARGS__)
#define ST_WARN(...)  ::Storming::Log::Print(::Storming::Log::Level::Warn, __VA_ARGS__)
#define ST_ERROR(...) ::Storming::Log::Print(::Storming::Log::Level::Error, __VA_ARGS__)
#define ST_FATAL(...) ::Storming::Log::Print(::Storming::Log::Level::Fatal, __VA_ARGS__)
