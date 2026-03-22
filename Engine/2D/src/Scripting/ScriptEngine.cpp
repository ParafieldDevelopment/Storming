#include "Scripting/ScriptEngine.hpp"
#include "Scripting/ScriptComponent.hpp"
#include "ECS/Component.hpp"
#include "ECS/Entity.hpp"
#include "Core/Log.hpp"
#include <sol/sol.hpp>
#include <iostream>
#include <fstream>
#include <sstream>

namespace Storming {

    struct ScriptEngineData {
        sol::state Lua;
        Scene* SceneContext = nullptr;
    };

    static ScriptEngineData* s_Data = nullptr;

    void ScriptEngine::Init() {
        s_Data = new ScriptEngineData();
        
        // Initialize Lua
        s_Data->Lua.open_libraries(sol::lib::base, sol::lib::math, sol::lib::string);
        
        // Bind Log
        s_Data->Lua.set_function("Log", [](const std::string& message) {
            ST_INFO("[Script] " + message);
        });

        RegisterComponents();
    }

    void ScriptEngine::Shutdown() {
        delete s_Data;
        s_Data = nullptr;
    }

    void ScriptEngine::RegisterComponents() {
        // Transform Binding
        s_Data->Lua.new_usertype<glm::vec3>("Vec3",
            sol::constructors<glm::vec3(float, float, float)>(),
            "x", &glm::vec3::x,
            "y", &glm::vec3::y,
            "z", &glm::vec3::z
        );

        s_Data->Lua.new_usertype<TransformComponent>("Transform",
            "Translation", &TransformComponent::Translation,
            "Rotation", &TransformComponent::Rotation,
            "Scale", &TransformComponent::Scale
        );

        // Entity Binding (Minimal)
        s_Data->Lua.new_usertype<Entity>("Entity",
            "GetTransform", [](Entity& entity) -> TransformComponent& {
                return entity.GetComponent<TransformComponent>();
            }
        );
    }

    void ScriptEngine::OnRuntimeStart(Scene* scene) {
        s_Data->SceneContext = scene;
    }

    void ScriptEngine::OnRuntimeStop() {
        s_Data->SceneContext = nullptr;
    }

    void ScriptEngine::OnUpdate(float dt) {
        if (!s_Data->SceneContext) return;

        auto view = s_Data->SceneContext->GetRegistry().view<ScriptComponent>();
        for (auto e : view) {
            Entity entity = { e, s_Data->SceneContext };
            auto& sc = entity.GetComponent<ScriptComponent>();

            if (!sc.IsInitialized) {
                // Load script from file
                std::ifstream file(sc.Path);
                if (file.is_open()) {
                    std::stringstream buffer;
                    buffer << file.rdbuf();
                    
                    try {
                        // Load and run the script chunks
                        sol::load_result loadResult = s_Data->Lua.load(buffer.str(), sc.Path);
                        if (!loadResult.valid()) {
                            sol::error err = loadResult;
                            ST_ERROR("Lua Load Error: " + std::string(err.what()));
                            continue;
                        }
                        
                        // Execute to get the table
                        sol::protected_function_result scriptResult = loadResult();
                        if (scriptResult.valid()) {
                            sc.Instance = scriptResult;
                            sc.Instance["Entity"] = entity; // Inject 'Entity' into script
                            
                            // Call OnCreate
                            sol::function onCreate = sc.Instance["OnCreate"];
                            if (onCreate.valid()) onCreate(sc.Instance);
                            
                            sc.IsInitialized = true;
                        } else {
                            sol::error err = scriptResult;
                            ST_ERROR("Lua Execute Error: " + std::string(err.what()));
                        }
                    } catch (const std::exception& e) {
                        ST_ERROR("Lua Exception: " + std::string(e.what()));
                    }
                }
            }

            if (sc.IsInitialized) {
                sol::function onUpdate = sc.Instance["OnUpdate"];
                if (onUpdate.valid()) {
                    try {
                        onUpdate(sc.Instance, dt);
                    } catch (const std::exception& e) {
                        ST_ERROR("Lua Error in OnUpdate: " + std::string(e.what()));
                    }
                }
            }
        }
    }

}
