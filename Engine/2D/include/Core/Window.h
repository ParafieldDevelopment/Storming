//
// Created by Batista on 9/11/2025.
//

#ifndef STORMING_WINDOW_H
#define STORMING_WINDOW_H

#pragma once
#include <string>

namespace Storming {
    class Window {
    public:
        Window(const std::string& title, unsigned int width, unsigned int height);
        ~Window();

        void OnUpdate();

    private:
        std::string m_Title;
        unsigned int m_Width, m_Height;
    };
}


#endif //STORMING_WINDOW_H