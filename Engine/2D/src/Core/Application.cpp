#include "Core/Application.h"
#include "Core/Log.h"

namespace Storming {
    Application::Application() {
        Log::Init();
        m_Window = new Window("Storming Engine 2D", 1280, 720);
    }

    Application::~Application() {
        delete m_Window;
    }

    void Application::Run() {
        while (m_Running) {
            m_Window->OnUpdate();
            // TODO: update ECS, render, handle events
        }
    }
}
