#include "Core/AssetRegistry.hpp"
#include <future>
#include <iostream>

namespace Storming {

    std::future<std::shared_ptr<Texture2D>> AssetRegistry::LoadTextureAsync(const std::string& path) {
        return std::async(std::launch::async, [path]() -> std::shared_ptr<Texture2D> {
            auto texture = Texture2D::Create(path);
            
            auto& instance = AssetRegistry::Get();
            std::lock_guard<std::mutex> lock(instance.m_Mutex);
            
            if (texture) {
                instance.m_Textures[path] = texture;
                std::cout << "[TELEMETRY]{\"type\":\"event\",\"action\":\"asset_loaded\",\"path\":\"" << path << "\",\"status\":\"success\"}" << std::endl;
            } else {
                std::cout << "[TELEMETRY]{\"type\":\"event\",\"action\":\"asset_loaded\",\"path\":\"" << path << "\",\"status\":\"failed\"}" << std::endl;
            }
            std::cout.flush();
            
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
