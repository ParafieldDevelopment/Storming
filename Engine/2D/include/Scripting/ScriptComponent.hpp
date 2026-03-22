#pragma once

#include <string>
#include <sol/sol.hpp>

namespace Storming {

    struct ScriptComponent {
        std::string Path = "";
        
        // Runtime state (Lua Table instance)
        sol::table Instance;
        bool IsInitialized = false;

        ScriptComponent() = default;
        ScriptComponent(const ScriptComponent&) = default;
        ScriptComponent(const std::string& path) : Path(path) {}
    };

}
