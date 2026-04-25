cmake_minimum_required(VERSION 3.20)
set(CMAKE_POLICY_VERSION_MINIMUM 3.5)
project(StormingEngine VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Output directories
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# --- Dependencies ---

# 1. SDL3 (Windowing, Input)
find_package(SDL3 QUIET)
if(NOT SDL3_FOUND)
    message(STATUS "SDL3 not found locally, fetching from GitHub...")
    include(FetchContent)
    FetchContent_Declare(
        SDL3
        GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
        GIT_TAG release-3.2.0
        GIT_SHALLOW TRUE
        GIT_PROGRESS TRUE
    )
    # Speed up SDL3 build by disabling unneeded features
    set(SDL_SHARED ON CACHE BOOL "Build shared SDL library" FORCE)
    set(SDL_STATIC OFF CACHE BOOL "Build static SDL library" FORCE)
    set(SDL_TEST OFF CACHE BOOL "Build SDL tests" FORCE)
    set(SDL_EXAMPLES OFF CACHE BOOL "Build SDL examples" FORCE)
    set(SDL_DOCS OFF CACHE BOOL "Build SDL documentation" FORCE)
    set(SDL_INSTALL OFF CACHE BOOL "Enable SDL installation" FORCE)
    
    FetchContent_MakeAvailable(SDL3)
endif()

# 2. EnTT (ECS)
include(FetchContent)
FetchContent_Declare(
    entt
    GIT_REPOSITORY https://github.com/skypjack/entt.git
    GIT_TAG v3.13.0
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(entt)

# 3. GLAD (GL Loader)
FetchContent_Declare(
    glad
    GIT_REPOSITORY https://github.com/Dav1dde/glad.git
    GIT_TAG v0.1.36
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(glad)

# 4. GLM (Math)
FetchContent_Declare(
    glm
    GIT_REPOSITORY https://github.com/g-truc/glm.git
    GIT_TAG 0.9.9.8
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(glm)

# 5. nlohmann_json (Command Protocol)
FetchContent_Declare(
    json
    GIT_REPOSITORY https://github.com/nlohmann/json.git
    GIT_TAG v3.11.3
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(json)

# 6. stb (Image Loading)
FetchContent_Declare(
    stb
    GIT_REPOSITORY https://github.com/nothings/stb.git
    GIT_TAG master
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(stb)

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
if(WIN32)
    target_link_libraries(StormingEngine PRIVATE 
        SDL3::SDL3
        EnTT::EnTT
        glad
        glm::glm
        nlohmann_json::nlohmann_json
    )
else()
    target_link_libraries(StormingEngine PRIVATE 
        SDL3::SDL3
        EnTT::EnTT
        glad
        glm::glm
        nlohmann_json::nlohmann_json
        rt
    )
endif()

# Compiler definitions
if(MSVC)
    target_compile_options(StormingEngine PRIVATE /W4)
else()
    target_compile_options(StormingEngine PRIVATE -Wall -Wextra -Wpedantic)
endif()
