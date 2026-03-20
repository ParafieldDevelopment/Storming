#pragma once

#include <glm/glm.hpp>
#include <string>
#include <memory>
#include "Rendering/Texture.hpp"

namespace Storming {

    /**
     * Stores the name/identifier of an entity.
     * Used for the Hierarchy panel in the Editor.
     */
    struct TagComponent {
        std::string Tag;

        TagComponent() = default;
        TagComponent(const TagComponent&) = default;
        TagComponent(const std::string& tag) : Tag(tag) {}
    };

    /**
     * Defines the position, orientation, and size of an entity in 3D space.
     * While the engine is 2D, a 3D vector is used for Z-layering support.
     */
    struct TransformComponent {
        glm::vec3 Translation = { 0.0f, 0.0f, 0.0f };
        glm::vec3 Rotation = { 0.0f, 0.0f, 0.0f }; // Euler angles
        glm::vec3 Scale = { 1.0f, 1.0f, 1.0f };

        TransformComponent() = default;
        TransformComponent(const TransformComponent&) = default;
        TransformComponent(const glm::vec3& translation) : Translation(translation) {}
    };

    /**
     * Allows an entity to be rendered as a textured or colored quad.
     * Color acts as a tint if a texture is provided.
     */
    struct SpriteRendererComponent {
        glm::vec4 Color{ 1.0f, 1.0f, 1.0f, 1.0f };
        std::shared_ptr<Texture2D> Texture;
        std::string TexturePath = "";

        SpriteRendererComponent() = default;
        SpriteRendererComponent(const SpriteRendererComponent&) = default;
        SpriteRendererComponent(const glm::vec4& color) : Color(color) {}
    };

}
