#pragma once

#include <string>
#include <memory>
#include <unordered_map>
#include <mutex>
#include <future>
#include "Rendering/Texture.hpp"

namespace Storming {

    /**
     * Central registry for managing asset lifecycles.
     * Provides thread-safe, asynchronous loading capabilities.
     */
    class AssetRegistry {
    public:
        static AssetRegistry& Get() {
            static AssetRegistry instance;
            return instance;
        }

        // Asynchronously load a texture
        std::future<std::shared_ptr<Texture2D>> LoadTextureAsync(const std::string& path);

        // Get an already loaded texture
        std::shared_ptr<Texture2D> GetTexture(const std::string& path);

    private:
        AssetRegistry() = default;
        ~AssetRegistry() = default;

        std::unordered_map<std::string, std::shared_ptr<Texture2D>> m_Textures;
        std::mutex m_Mutex;
    };

}
