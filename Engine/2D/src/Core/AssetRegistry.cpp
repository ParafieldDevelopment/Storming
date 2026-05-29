#include "Core/AssetRegistry.hpp"
#include <future>

namespace Storming {

    std::future<std::shared_ptr<Texture2D>> AssetRegistry::LoadTextureAsync(const std::string& path) {
        return std::async(std::launch::async, [path]() -> std::shared_ptr<Texture2D> {
            // Synchronous load on background thread
            auto texture = Texture2D::Create(path);
            
            // Lock and cache
            auto& instance = AssetRegistry::Get();
            std::lock_guard<std::mutex> lock(instance.m_Mutex);
            instance.m_Textures[path] = texture;
            
            return texture;
        });
    }

    std::shared_ptr<Texture2D> AssetRegistry::GetTexture(const std::string& path) {
        std::lock_guard<std::mutex> lock(m_Mutex);
        if (m_Textures.find(path) != m_Textures.end()) {
            return m_Textures[path];
        }
        return nullptr;
    }

}
