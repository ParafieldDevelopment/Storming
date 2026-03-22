cmake_minimum_required(VERSION 3.20)
set(CMAKE_POLICY_VERSION_MINIMUM 3.5)
cmake_policy(SET CMP0135 NEW) # Fixes download timestamp warnings

project(StormingEngine VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Output directories
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# --- Dependencies (Using URL downloads for speed and reliability) ---
include(FetchContent)

# 1. SDL3 (Windowing, Input)
find_package(SDL3 REQUIRED)

# 2. EnTT (ECS)
FetchContent_Declare(
    entt
    URL https://github.com/skypjack/entt/archive/refs/tags/v3.13.0.tar.gz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(entt)

# 3. GLAD (GL Loader) - Switched to stable URL
FetchContent_Declare(
    glad
    URL https://github.com/Dav1dde/glad/archive/refs/tags/v0.1.36.tar.gz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(glad)

# 4. GLM (Math)
FetchContent_Declare(
    glm
    URL https://github.com/g-truc/glm/archive/refs/tags/0.9.9.8.tar.gz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(glm)

# 5. nlohmann_json (Command Protocol)
FetchContent_Declare(
    json
    URL https://github.com/nlohmann/json/releases/download/v3.11.3/json.tar.xz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(json)

# 6. stb (Image Loading)
FetchContent_Declare(
    stb
    URL https://github.com/nothings/stb/archive/refs/heads/master.tar.gz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(stb)

# 7. Lua (Scripting)
FetchContent_Declare(
    lua
    URL https://github.com/walterschell/Lua/archive/refs/tags/v5.4.6.tar.gz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(lua)

# 8. Sol2 (Lua Bindings)
FetchContent_Declare(
    sol2
    URL https://github.com/ThePhD/sol2/archive/refs/heads/develop.tar.gz
    DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(sol2)

# --- Sources ---
file(GLOB_RECURSE SOURCES 
    "src/*.cpp"
    "src/*.hpp"
    "include/*.hpp"
)

# --- Executable ---
add_executable(StormingEngine ${SOURCES})

# Include directories
target_include_directories(StormingEngine PRIVATE 
    include
    src
    ${stb_SOURCE_DIR}
)

# Link libraries
target_link_libraries(StormingEngine PRIVATE 
    SDL3::SDL3
    EnTT::EnTT
    glad
    glm::glm
    nlohmann_json::nlohmann_json
    rt
    lua
    sol2
)

target_compile_definitions(StormingEngine PRIVATE SOL_ALL_SAFETIES_ON=1)

# Compiler definitions
if(MSVC)
    target_compile_options(StormingEngine PRIVATE /W4)
else()
    target_compile_options(StormingEngine PRIVATE -Wall -Wextra -Wpedantic)
endif()
