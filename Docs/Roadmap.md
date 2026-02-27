# Storming Engine: 2026 Revival Roadmap

## Pre-Phase: Engine Startup (v2026.0.0)
*   **[Done]** **Shared Memory (SHM) Pipeline:** Establish a high-speed pixel transfer between C++ Engine and Java Editor.
*   **[Semi-Done]** **Embedded Mode:** Engine can run hidden (`--shm`) while streaming its output to the Editor's viewport.
*   **[Next]** **JSON Command Protocol:** Use stdin/stdout for basic Editor -> Engine commands (e.g., Change Clear Color).
*   **[Next]** **HAL Abstraction:** Move raw OpenGL code from `Application` into a dedicated `OpenGLRendererAPI` class.
*   **[Next]** **Telemetry Bridge:** Engine reports FPS and Draw Call statistics back to the Editor's Console.

*   **[REQUIRED]** **Auto Engine Compile:** Automatically make Storming Initalitize the Engine.

## Phase 1: The Core Foundation (v2026.0.1)
*   **[Redesigning]** Improved UI
*   **[Semi-Done]** New high-performance directory structure for C++ Engine.
*   **[Semi-Done]** Modern "IntelliJ-style" Java Editor with Dark Theme (FlatLaf).
*   **[Semi-Done]** Hardware Abstraction Layer (HAL) for multi-backend rendering (OpenGL/Vulkan).
*   **[Semi-Done]** Basic Engine Launcher "Play" bridge from Editor to Engine.
*   **[Next]** **The `.storm` Project Format:** A JSON-based file system to track assets and scenes.

## Phase 2: The 2D World (v2026.0.2)
*   **Entity Component System (ECS):** A clean C++ implementation to manage "Entities" (players, enemies, objects).
*   **Renderer2D (OpenGL Backend):** Efficiently draw thousands of sprites using batching.
*   **The Scene Tree:** Implement the "Hierarchy" in both the Engine (for logic) and the Editor (for editing).
*   **Inspector Binding:** Ability to click an object in the Scene and change its position/color in the Editor's Inspector.

## Phase 3: The Scripting Layer (v2026.0.3)
*   **LUA/C# Integration:** Bringing back scripting. We'll decide between a lightweight LUA integration or a full C# (Mono/DotNet) runtime for game logic.
*   **Hot Reload:** Change a script and see the results in the engine immediately without restarting.

## Phase 4: Advanced Graphics & 3D (v2026.0.4)
*   **Vulkan Renderer implementation:** Complete the high-performance HAL backend.
*   **3D Foundation:** Basic 3D math, Model loading (.obj/.fbx), and a simple PBR (Physically Based Rendering) shader.
*   **Plugin API:** Allow users to write their own Editor tools in Java.
