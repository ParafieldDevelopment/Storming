#pragma once

#include <entt/entt.hpp>
#include <string>

namespace Storming {

    class Entity;

    class Scene {
    public:
        Scene();
        ~Scene();

        Entity CreateEntity(const std::string& name = "Entity");
        void DestroyEntity(Entity entity);

        void OnUpdate(float ts);
        void OnViewportResize(uint32_t width, uint32_t height);

        void BroadcastSceneTree();

    private:
        entt::registry m_Registry;
        uint32_t m_ViewportWidth = 0, m_ViewportHeight = 0;

        friend class Entity;
    };

}
