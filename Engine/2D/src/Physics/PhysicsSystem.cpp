#include "Physics/PhysicsSystem.hpp"
#include "ECS/Component.hpp"
#include "ECS/Entity.hpp"
#include <iostream>

namespace Storming {

    PhysicsSystem::PhysicsSystem() {
        m_PhysicsWorld = st_physics_world_create();
        std::cout << "[PhysicsSystem] World Created" << std::endl;
    }

    PhysicsSystem::~PhysicsSystem() {
        if (m_PhysicsWorld) {
            st_physics_world_destroy(m_PhysicsWorld);
            std::cout << "[PhysicsSystem] World Destroyed" << std::endl;
        }
    }

    void PhysicsSystem::OnUpdate(Scene* scene, float ts) {
        auto& registry = scene->GetRegistry();

        // 1. Sync C++ -> Rust (Initialization of RigidBodies)
        {
            auto view = registry.view<TransformComponent, RigidBody2DComponent>();
            for (auto entity : view) {
                auto& tc = view.get<TransformComponent>(entity);
                auto& rb2d = view.get<RigidBody2DComponent>(entity);

                if (!rb2d.RuntimeBody) {
                    rb2d.RuntimeBody = (void*)st_rigid_body_create(m_PhysicsWorld, (int)rb2d.Type, tc.Translation.x, tc.Translation.y, tc.Rotation.z);
                    
                    if (registry.all_of<BoxCollider2DComponent>(entity)) {
                        auto& bc2d = registry.get<BoxCollider2DComponent>(entity);
                        // Rapier cuboid uses half-extents
                        st_collider_box_create(m_PhysicsWorld, (st_rigid_body_handle)rb2d.RuntimeBody, bc2d.Size.x * tc.Scale.x * 0.5f, bc2d.Size.y * tc.Scale.y * 0.5f);
                    }
                }
            }
        }

        // 2. Step Physics
        // For now, we step every frame. We might want a fixed time step later.
        st_physics_world_step(m_PhysicsWorld);

        // 3. Sync Rust -> C++ (Update transforms for dynamic bodies)
        {
            auto view = registry.view<TransformComponent, RigidBody2DComponent>();
            for (auto entity : view) {
                auto& tc = view.get<TransformComponent>(entity);
                auto& rb2d = view.get<RigidBody2DComponent>(entity);

                if (rb2d.RuntimeBody && rb2d.Type == RigidBody2DComponent::BodyType::Dynamic) {
                    float x, y;
                    st_rigid_body_get_position(m_PhysicsWorld, (st_rigid_body_handle)rb2d.RuntimeBody, &x, &y);
                    tc.Translation.x = x;
                    tc.Translation.y = y;
                    tc.Rotation.z = st_rigid_body_get_rotation(m_PhysicsWorld, (st_rigid_body_handle)rb2d.RuntimeBody);
                }
            }
        }
    }

}
