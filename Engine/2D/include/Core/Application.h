//
// Created by Batista on 9/11/2025.
//

#ifndef STORMING_APPLICATION_H
#define STORMING_APPLICATION_H

#pragma once

#include "Window.h"

namespace Storming {
    class Application {
    public:
        Application();
        virtual ~Application();

        void Run();

    private:
        bool m_Running = true;
        Window* m_Window;
    };
}


#endif //STORMING_APPLICATION_H