#pragma once

#include "Physics/PhysicsBridge.hpp"
#include "ECS/Scene.hpp"

namespace Storming {

    class PhysicsSystem {
    public:
        PhysicsSystem();
        ~PhysicsSystem();

        void OnUpdate(Scene* scene, float ts);

    private:
        st_physics_world_ptr m_PhysicsWorld = nullptr;
    };

}
