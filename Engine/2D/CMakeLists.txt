cmake_minimum_required(VERSION 3.20)
set(CMAKE_POLICY_VERSION_MINIMUM 3.5)
project(StormingEngine VERSION 0.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Output directories
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# --- Dependencies ---

# 1. SDL3 (Windowing, Input)
find_package(SDL3 REQUIRED)

# 2. EnTT (ECS)
include(FetchContent)
FetchContent_Declare(
    entt
    GIT_REPOSITORY https://github.com/skypjack/entt.git
    GIT_TAG v3.13.0
)
FetchContent_MakeAvailable(entt)

# 3. GLAD (GL Loader)
FetchContent_Declare(
    glad
    GIT_REPOSITORY https://github.com/Dav1dde/glad.git
    GIT_TAG v0.1.36
)
FetchContent_MakeAvailable(glad)
# Custom glad generation for OpenGL 4.5
# We don't need add_library here anymore if it exists

# 4. GLM (Math)
FetchContent_Declare(
    glm
    GIT_REPOSITORY https://github.com/g-truc/glm.git
    GIT_TAG 0.9.9.8
)
FetchContent_MakeAvailable(glm)

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
)

# Link libraries
target_link_libraries(StormingEngine PRIVATE 
    SDL3::SDL3
    EnTT::EnTT
    glad
    glm::glm
    rt
)

# Compiler definitions
if(MSVC)
    target_compile_options(StormingEngine PRIVATE /W4)
else()
    target_compile_options(StormingEngine PRIVATE -Wall -Wextra -Wpedantic)
endif()
