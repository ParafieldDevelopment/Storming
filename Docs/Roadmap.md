# Storming Engine: 2026 Revival Roadmap

## Pre-Phase: Engine Startup (v2026.0.0)
*   **[Done]** **Shared Memory (SHM) Pipeline:** Established high-speed pixel transfer between C++ Engine and Java Editor.
*   **[Done]** **Embedded Mode:** Engine runs hidden (`--shm`) while streaming output to Editor viewport.
*   **[Done]** **C++ Engine Rewrite:** Full transition to C++20, SDL3, and Data-Oriented Architecture.
*   **[Next]** **JSON Command Protocol:** Stdin/stdout bridge for Editor -> Engine communication.
*   **[Next]** **Telemetry Bridge:** Real-time reporting of FPS, Draw Calls, and Frame Timing to the Editor.

## Phase 1: The Core Foundation (v2026.0.1)
*   **[Done]** Modern "IntelliJ-style" Java Editor with Dark Theme (FlatLaf).
*   **[Done]** High-performance directory structure for C++ Engine.
*   **[Done]** HAL/RHI for multi-backend rendering (OpenGL 4.5 Initial).
*   **[Done]** Basic Engine Launcher "Play" bridge from Editor to Engine.
*   **[Next]** **The `.storm` Project Format:** JSON-based asset and scene tracking.

## Phase 2: The 2D World (v2026.0.2)
*   **ECS Integration:** Utilizing **EnTT** for ultra-fast, cache-friendly data management.
*   **Renderer2D Batching:** Automatic batching of quads and textures to minimize draw calls.
*   **The Scene Tree:** Hierarchical entity management synced between Engine and Editor.
*   **Inspector Binding:** Real-time property editing via the Editor's Inspector.

## Phase 3: The Scripting Layer (v2026.0.3)
*   **LUA/C# Integration:** Flexible logic layer (LUA for lightweight, C# for robust development).
*   **Hot Reload:** Instant logic updates without restarting the engine process.

## Phase 4: Advanced Graphics & 3D (v2026.0.4)
*   **Vulkan Backend:** Full implementation of the Vulkan RHI for peak performance.
*   **3D Foundation:** Basic 3D math, PBR shading, and model loading.
*   **Plugin API:** Java-based API for expanding Editor functionality.
