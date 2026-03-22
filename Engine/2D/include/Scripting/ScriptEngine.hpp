#pragma once

#include "ECS/Scene.hpp"
#include <string>

namespace Storming {

    class ScriptEngine {
    public:
        static void Init();
        static void Shutdown();

        static void OnRuntimeStart(Scene* scene);
        static void OnRuntimeStop();

        static void OnUpdate(float dt);

        static void RegisterComponents();
    };

}
