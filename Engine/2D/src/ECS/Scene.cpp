#include "ECS/Scene.hpp"
#include "ECS/Entity.hpp"
#include "ECS/Component.hpp"
#include "Rendering/Renderer2D.hpp"
#include "Physics/PhysicsSystem.hpp"
#include <nlohmann/json.hpp>
#include <iostream>
#include <fstream>

namespace Storming {

    using json = nlohmann::json;

    Scene::Scene() {
        m_PhysicsSystem = new PhysicsSystem();
    }

    Scene::~Scene() {
        delete m_PhysicsSystem;
    }

    /**
     * Creates a new entity in the scene with a Tag and Transform component.
     * Automatically broadcasts the updated scene tree to the Editor.
     * 
     * @param name The name of the entity (e.g., "Player").
     * @return An Entity object wrapper around the ECS handle.
     */
    Entity Scene::CreateEntity(const std::string& name) {
        Entity entity = { m_Registry.create(), this };
        entity.AddComponent<TagComponent>(name.empty() ? "Entity" : name);
        entity.AddComponent<TransformComponent>();
        
        BroadcastSceneTree();
        return entity;
    }

    /**
     * Destroys an entity and removes it from the EnTT registry.
     * @param entity The entity to destroy.
     */
    void Scene::DestroyEntity(Entity entity) {
        m_Registry.destroy(entity);
        BroadcastSceneTree();
    }

    /**
     * Serializes the entire scene (all entities and components) into a JSON string.
     * This string is intended to be saved to a .storm_scene file by the Editor.
     * 
     * @return A formatted JSON string representing the scene state.
     */
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

            if (m_Registry.all_of<RigidBody2DComponent>(entity)) {
                auto& rb = m_Registry.get<RigidBody2DComponent>(entity);
                eJson["components"]["RigidBody2D"] = {
                    {"type", (int)rb.Type},
                    {"fixed_rotation", rb.FixedRotation}
                };
            }

            if (m_Registry.all_of<BoxCollider2DComponent>(entity)) {
                auto& bc = m_Registry.get<BoxCollider2DComponent>(entity);
                eJson["components"]["BoxCollider2D"] = {
                    {"size", {bc.Size.x, bc.Size.y}},
                    {"offset", {bc.Offset.x, bc.Offset.y}},
                    {"density", bc.Density},
                    {"friction", bc.Friction},
                    {"restitution", bc.Restitution}
                };
            }

            data["entities"].push_back(eJson);
        }
        return data.dump(4);
    }

    /**
     * Broadcasts the current hierarchy list (ID + Name) to STDOUT.
     * The Java Editor listens for this telemetry to update the Hierarchy Panel.
     */
    void Scene::BroadcastSceneTree() {
        json tree;
        tree["type"] = "scene_tree";
        tree["entities"] = json::array();
        
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

    /**
     * Broadcasts detailed component data for a specific entity to STDOUT.
     * Used to populate the Inspector Panel when an entity is selected.
     * 
     * @param entityID The ECS handle ID of the entity.
     */
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
                {"color", {src.Color.r, src.Color.g, src.Color.b, src.Color.a}},
                {"texture", src.TexturePath}
            };
        }

        if (m_Registry.all_of<RigidBody2DComponent>(handle)) {
            auto& rb = m_Registry.get<RigidBody2DComponent>(handle);
            data["components"]["RigidBody2D"] = {
                {"type", (int)rb.Type},
                {"fixed_rotation", rb.FixedRotation}
            };
        }

        if (m_Registry.all_of<BoxCollider2DComponent>(handle)) {
            auto& bc = m_Registry.get<BoxCollider2DComponent>(handle);
            data["components"]["BoxCollider2D"] = {
                {"size", {bc.Size.x, bc.Size.y}},
                {"offset", {bc.Offset.x, bc.Offset.y}},
                {"density", bc.Density},
                {"friction", bc.Friction},
                {"restitution", bc.Restitution}
            };
        }

        std::cout << "[TELEMETRY]" << data.dump() << std::endl;
        std::cout.flush();
    }

    /**
     * Replaces the current scene with entities loaded from a JSON file.
     * 
     * @param path The absolute file path to the .storm_scene file.
     */
    void Scene::LoadFromFile(const std::string& path) {
        std::ifstream f(path);
        if (!f.is_open()) {
            std::cerr << "[Scene] Failed to load: " << path << std::endl;
            return;
        }

        json data = json::parse(f);
        if (data.contains("entities")) {
            m_Registry.clear();

            for (auto& entityData : data["entities"]) {
                std::string name = entityData.value("name", "Entity");
                auto entity = CreateEntity(name);

                if (entityData.contains("components")) {
                    auto& comps = entityData["components"];
                    
                    if (comps.contains("Transform")) {
                        auto& tc = entity.GetComponent<TransformComponent>();
                        auto& tData = comps["Transform"];
                        
                        auto trans = tData["translation"];
                        tc.Translation = { trans[0], trans[1], trans[2] };
                        
                        auto rot = tData["rotation"];
                        tc.Rotation = { rot[0], rot[1], rot[2] };
                        
                        auto scl = tData["scale"];
                        tc.Scale = { scl[0], scl[1], scl[2] };
                    }

                    if (comps.contains("SpriteRenderer")) {
                        auto& src = entity.AddComponent<SpriteRendererComponent>();
                        auto c = comps["SpriteRenderer"]["color"];
                        src.Color = { c[0], c[1], c[2], c[3] };
                        if (comps["SpriteRenderer"].contains("texture")) {
                            std::string texPath = comps["SpriteRenderer"]["texture"];
                            if (!texPath.empty()) {
                                src.Texture = Texture2D::Create(texPath);
                                src.TexturePath = texPath;
                            }
                        }
                    }

                    if (comps.contains("RigidBody2D")) {
                        auto& rb = entity.AddComponent<RigidBody2DComponent>();
                        rb.Type = (RigidBody2DComponent::BodyType)comps["RigidBody2D"]["type"];
                        rb.FixedRotation = comps["RigidBody2D"]["fixed_rotation"];
                    }

                    if (comps.contains("BoxCollider2D")) {
                        auto& bc = entity.AddComponent<BoxCollider2DComponent>();
                        auto s = comps["BoxCollider2D"]["size"];
                        bc.Size = { s[0], s[1] };
                        auto o = comps["BoxCollider2D"]["offset"];
                        bc.Offset = { o[0], o[1] };
                        bc.Density = comps["BoxCollider2D"]["density"];
                        bc.Friction = comps["BoxCollider2D"]["friction"];
                        bc.Restitution = comps["BoxCollider2D"]["restitution"];
                    }
                }
            }
        }
        BroadcastSceneTree();
    }

    /**
     * Performs a reverse-order AABB hit test to find the entity under the given coordinates.
     * Used for mouse picking in the Editor Viewport.
     * 
     * @param x Normalized X coordinate (-1.0 to 1.0).
     * @param y Normalized Y coordinate (-1.0 to 1.0).
     * @return The entity ID if found, or 0xFFFFFFFF if no entity was hit.
     */
    uint32_t Scene::PickEntity(float x, float y) {
        auto view = m_Registry.view<TransformComponent, SpriteRendererComponent>();
        entt::entity picked = entt::null;

        // Iterate all sprites to check bounds
        view.each([&](auto entity, auto& tc, auto& /*src*/) {
            float halfWidth = tc.Scale.x / 2.0f;
            float halfHeight = tc.Scale.y / 2.0f;
            
            if (x >= (tc.Translation.x - halfWidth) && x <= (tc.Translation.x + halfWidth) &&
                y >= (tc.Translation.y - halfHeight) && y <= (tc.Translation.y + halfHeight)) {
                picked = entity; // Last one found is "on top" in Z-order roughly
            }
        });

        if (picked != entt::null) {
            BroadcastEntityComponents((uint32_t)picked);
            return (uint32_t)picked;
        }
        return 0xFFFFFFFF;
    }

    /**
     * The primary update loop called every frame.
     * Iterates over all renderable entities and submits them to the Renderer2D.
     * 
     * @param ts TimeStep (delta time) in seconds.
     */
    void Scene::OnUpdate(float ts) {
        if (m_PhysicsSystem)
            m_PhysicsSystem->OnUpdate(this, ts);

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
