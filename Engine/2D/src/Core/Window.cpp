#include "Core/Window.h"
#include <iostream>

namespace Storming {
    Window::Window(const std::string& title, unsigned int width, unsigned int height)
        : m_Title(title), m_Width(width), m_Height(height) {
        std::cout << "Created window: " << m_Title
                  << " (" << m_Width << "x" << m_Height << ")\n";
    }

    Window::~Window() {}

    void Window::OnUpdate() {
        // TODO: poll input, refresh screen
    }
}
