//
// Created by Batista on 9/11/2025.
//

#ifndef STORMING_LOG_H
#define STORMING_LOG_H

#pragma once
#include <iostream>

namespace Storming {
    class Log {
    public:
        static void Init();
        static void Info(const std::string& message);
        static void Error(const std::string& message);
    };
}


#endif //STORMING_LOG_H