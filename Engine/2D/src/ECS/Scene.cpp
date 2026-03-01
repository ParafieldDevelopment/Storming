#include "ECS/Scene.hpp"
#include "ECS/Entity.hpp"
#include "ECS/Component.hpp"
#include "Rendering/Renderer2D.hpp"
#include <nlohmann/json.hpp>
#include <iostream>
#include <fstream>

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

    void Scene::BroadcastEntityComponents(uint32_t entityID) {
        entt::entity handle = (entt::entity)entityID;
        if (!m_Registry.valid(handle)) return;

        json data;
        data["type"] = "entity_details";
        data["id"] = entityID;

        if (m_Registry.all_of<TagComponent>(handle)) {
            data["tag"] = m_Registry.get<TagComponent>(handle).Tag;
        }

        if (m_Registry.all_of<TransformComponent>(handle)) {
            auto& tc = m_Registry.get<TransformComponent>(handle);
            data["components"]["Transform"] = {
                {"translation", {tc.Translation.x, tc.Translation.y, tc.Translation.z}},
                {"rotation", {tc.Rotation.x, tc.Rotation.y, tc.Rotation.z}},
                {"scale", {tc.Scale.x, tc.Scale.y, tc.Scale.z}}
            };
        }

        if (m_Registry.all_of<SpriteRendererComponent>(handle)) {
            auto& src = m_Registry.get<SpriteRendererComponent>(handle);
            data["components"]["SpriteRenderer"] = {
                {"color", {src.Color.r, src.Color.g, src.Color.b, src.Color.a}}
            };
        }

        std::cout << "[TELEMETRY]" << data.dump() << std::endl;
        std::cout.flush();
    }

    void Scene::LoadFromFile(const std::string& path) {
        std::ifstream f(path);
        if (!f.is_open()) {
            std::cerr << "[Scene] Failed to load: " << path << std::endl;
            return;
        }

        json data = json::parse(f);
        if (data.contains("entities")) {
            // Clear current scene
            m_Registry.clear();

            for (auto& entityData : data["entities"]) {
                std::string name = entityData.value("name", "Entity");
                auto entity = CreateEntity(name);

                if (entityData.contains("components")) {
                    auto& comps = entityData["components"];
                    
                    if (comps.contains("Transform")) {
                        auto& tc = entity.GetComponent<TransformComponent>();
                        auto& tData = comps["Transform"];
                        // Future: Add position loading logic here
                    }

                    if (comps.contains("SpriteRenderer")) {
                        auto& src = entity.AddComponent<SpriteRendererComponent>();
                        // Future: Add color/texture loading logic here
                    }
                }
            }
        }
        BroadcastSceneTree();
    }

    void Scene::PickEntity(float x, float y) {
        auto view = m_Registry.view<TransformComponent, SpriteRendererComponent>();
        
        entt::entity picked = entt::null;

        view.each([&](auto entity, auto& tc, auto& src) {
            float halfWidth = tc.Scale.x / 2.0f;
            float halfHeight = tc.Scale.y / 2.0f;
            
            if (x >= (tc.Translation.x - halfWidth) && x <= (tc.Translation.x + halfWidth) &&
                y >= (tc.Translation.y - halfHeight) && y <= (tc.Translation.y + halfHeight)) {
                picked = entity;
            }
        });

        if (picked != entt::null) {
            BroadcastEntityComponents((uint32_t)picked);
        }
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
