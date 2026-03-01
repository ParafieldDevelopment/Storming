#include "ECS/Scene.hpp"
#include "ECS/Entity.hpp"
#include "ECS/Component.hpp"
#include "Rendering/Renderer2D.hpp"
#include <nlohmann/json.hpp>
#include <iostream>

namespace Storming {

    using json = nlohmann::json;

    Scene::Scene() {}

    Scene::~Scene() {}

    Entity Scene::CreateEntity(const std::string& name) {
        Entity entity = { m_Registry.create(), this };
        entity.AddComponent<TagComponent>(name.empty() ? "Entity" : name);
        entity.AddComponent<TransformComponent>();
        
        // Broadcast change
        BroadcastSceneTree();
        
        return entity;
    }

    void Scene::DestroyEntity(Entity entity) {
        m_Registry.destroy(entity);
        BroadcastSceneTree();
    }

    void Scene::BroadcastSceneTree() {
        json tree;
        tree["type"] = "scene_tree";
        auto view = m_Registry.view<TagComponent>();
        for (auto entity : view) {
            auto& tag = view.get<TagComponent>(entity);
            json entry;
            entry["id"] = (uint32_t)entity;
            entry["name"] = tag.Tag;
            tree["entities"].push_back(entry);
        }
        std::cout << "[TELEMETRY]" << tree.dump() << std::endl;
        std::cout.flush();
    }

    void Scene::OnUpdate(float ts) {
        // Render Sprites
        auto view = m_Registry.view<TransformComponent, SpriteRendererComponent>();
        for (auto entity : view) {
            auto [transform, sprite] = view.get<TransformComponent, SpriteRendererComponent>(entity);
            
            if (sprite.Texture)
                Renderer2D::DrawQuad(transform.Translation, {transform.Scale.x, transform.Scale.y}, sprite.Texture, sprite.Color);
            else
                Renderer2D::DrawQuad(transform.Translation, {transform.Scale.x, transform.Scale.y}, sprite.Color);
        }
    }

    void Scene::OnViewportResize(uint32_t width, uint32_t height) {
        m_ViewportWidth = width;
        m_ViewportHeight = height;
    }

}
