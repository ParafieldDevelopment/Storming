#include "ECS/Scene.hpp"
#include "ECS/Entity.hpp"
#include "ECS/Component.hpp"
#include "Rendering/Renderer2D.hpp"

namespace Storming {

    Scene::Scene() {}

    Scene::~Scene() {}

    Entity Scene::CreateEntity(const std::string& name) {
        Entity entity = { m_Registry.create(), this };
        entity.AddComponent<TagComponent>(name.empty() ? "Entity" : name);
        entity.AddComponent<TransformComponent>();
        return entity;
    }

    void Scene::DestroyEntity(Entity entity) {
        m_Registry.destroy(entity);
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
