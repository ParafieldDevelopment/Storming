#include "ECS/Scene.hpp"
#include "ECS/Entity.hpp"
#include "ECS/Component.hpp"
#include "Scripting/ScriptComponent.hpp"
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
        BroadcastSceneTree();
        return entity;
    }

    void Scene::DestroyEntity(Entity entity) {
        m_Registry.destroy(entity);
        BroadcastSceneTree();
    }

    std::string Scene::Serialize() {
        json data;
        data["entities"] = json::array();
        auto view = m_Registry.view<TagComponent>();
        for (auto entity : view) {
            json eJson;
            eJson["name"] = m_Registry.get<TagComponent>(entity).Tag;
            if (m_Registry.all_of<TransformComponent>(entity)) {
                auto& tc = m_Registry.get<TransformComponent>(entity);
                eJson["components"]["Transform"] = {
                    {"translation", {tc.Translation.x, tc.Translation.y, tc.Translation.z}},
                    {"rotation", {tc.Rotation.x, tc.Rotation.y, tc.Rotation.z}},
                    {"scale", {tc.Scale.x, tc.Scale.y, tc.Scale.z}}
                };
            }
            if (m_Registry.all_of<SpriteRendererComponent>(entity)) {
                auto& src = m_Registry.get<SpriteRendererComponent>(entity);
                eJson["components"]["SpriteRenderer"] = {
                    {"color", {src.Color.r, src.Color.g, src.Color.b, src.Color.a}},
                    {"texture", src.TexturePath}
                };
            }
            if (m_Registry.all_of<ScriptComponent>(entity)) {
                eJson["components"]["Script"] = { {"path", m_Registry.get<ScriptComponent>(entity).Path} };
            }
            data["entities"].push_back(eJson);
        }
        return data.dump(4);
    }

    void Scene::BroadcastSceneTree() {
        json tree; tree["type"] = "scene_tree"; tree["entities"] = json::array();
        auto view = m_Registry.view<TagComponent>();
        for (auto entity : view) {
            json entry; entry["id"] = (uint32_t)entity; entry["name"] = view.get<TagComponent>(entity).Tag;
            tree["entities"].push_back(entry);
        }
        std::cout << "[TELEMETRY]" << tree.dump() << std::endl; std::cout.flush();
    }

    void Scene::BroadcastEntityComponents(uint32_t entityID) {
        entt::entity handle = (entt::entity)entityID;
        if (!m_Registry.valid(handle)) return;
        json data; data["type"] = "entity_details"; data["id"] = entityID;
        if (m_Registry.all_of<TagComponent>(handle)) data["tag"] = m_Registry.get<TagComponent>(handle).Tag;
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
            data["components"]["SpriteRenderer"] = { {"color", {src.Color.r, src.Color.g, src.Color.b, src.Color.a}}, {"texture", src.TexturePath} };
        }
        if (m_Registry.all_of<ScriptComponent>(handle)) {
            data["components"]["Script"] = { {"path", m_Registry.get<ScriptComponent>(handle).Path} };
        }
        std::cout << "[TELEMETRY]" << data.dump() << std::endl; std::cout.flush();
    }

    void Scene::LoadFromFile(const std::string& path) {
        std::ifstream f(path); if (!f.is_open()) return;
        json data = json::parse(f);
        if (data.contains("entities")) {
            m_Registry.clear();
            for (auto& entityData : data["entities"]) {
                auto entity = CreateEntity(entityData.value("name", "Entity"));
                if (entityData.contains("components")) {
                    auto& comps = entityData["components"];
                    if (comps.contains("Transform")) {
                        auto& tc = entity.GetComponent<TransformComponent>(); auto& tData = comps["Transform"];
                        auto trans = tData["translation"]; tc.Translation = { trans[0], trans[1], trans[2] };
                        auto rot = tData["rotation"]; tc.Rotation = { rot[0], rot[1], rot[2] };
                        auto scl = tData["scale"]; tc.Scale = { scl[0], scl[1], scl[2] };
                    }
                    if (comps.contains("SpriteRenderer")) {
                        auto& src = entity.AddComponent<SpriteRendererComponent>(); auto c = comps["SpriteRenderer"]["color"];
                        src.Color = { c[0], c[1], c[2], c[3] };
                        if (comps["SpriteRenderer"].contains("texture")) {
                            std::string texPath = comps["SpriteRenderer"]["texture"];
                            if (!texPath.empty()) { src.Texture = Texture2D::Create(texPath); src.TexturePath = texPath; }
                        }
                    }
                    if (comps.contains("Script")) {
                        entity.AddComponent<ScriptComponent>(comps["Script"]["path"]);
                    }
                }
            }
        }
        BroadcastSceneTree();
    }

    uint32_t Scene::PickEntity(float x, float y) {
        auto view = m_Registry.view<TransformComponent, SpriteRendererComponent>();
        entt::entity picked = entt::null;
        view.each([&](auto entity, auto& tc, auto& src) {
            float hw = tc.Scale.x / 2.0f; float hh = tc.Scale.y / 2.0f;
            if (x >= (tc.Translation.x - hw) && x <= (tc.Translation.x + hw) && y >= (tc.Translation.y - hh) && y <= (tc.Translation.y + hh)) picked = entity;
        });
        if (picked != entt::null) { BroadcastEntityComponents((uint32_t)picked); return (uint32_t)picked; }
        return 0xFFFFFFFF;
    }

    void Scene::OnUpdate(float ts) {
        auto view = m_Registry.view<TransformComponent, SpriteRendererComponent>();
        for (auto entity : view) {
            auto [transform, sprite] = view.get<TransformComponent, SpriteRendererComponent>(entity);
            if (sprite.Texture) Renderer2D::DrawQuad(transform.Translation, {transform.Scale.x, transform.Scale.y}, sprite.Texture, sprite.Color);
            else Renderer2D::DrawQuad(transform.Translation, {transform.Scale.x, transform.Scale.y}, sprite.Color);
        }
    }

    void Scene::OnViewportResize(uint32_t width, uint32_t height) { m_ViewportWidth = width; m_ViewportHeight = height; }
}
