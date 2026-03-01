# Storming Engine: 2026 Revival Roadmap

## Pre-Phase: Engine Startup (v2026.0.0)
*   **[Done]** **Shared Memory (SHM) Pipeline:** High-speed pixel transfer proven and stable.
*   **[Done]** **Embedded Mode:** Engine runs headless and streams to Editor viewport.
*   **[Done]** **C++ Engine Rewrite:** C++20, SDL3, and Data-Oriented foundation.
*   **[Done]** **JSON Command Protocol:** Stdin/stdout bridge for Editor -> Engine commands.
*   **[Done]** **Telemetry Bridge:** Real-time reporting of FPS, DC, and Quads.
*   **[Done]** **Auto Engine Compile:** Editor automatically manages C++ builds.

## Phase 1: The Core Foundation (v2026.0.1)
*   **[Done]** Modern "IntelliJ-style" Java Editor.
*   **[Done]** HAL/RHI abstraction for OpenGL 4.5.
*   **[Done]** High-performance directory structure.
*   **[Done]** **Rendering Compatibility:** Resolved invisible assets and WM maximization issues.

## Phase 2: The 2D World (v2026.0.2)
*   **[Done]** **ECS Integration:** Deep integration of EnTT.
*   **[Done]** **Texture & Sprite Support:** PNG/JPG loading and rendering.
*   **[In Progress]** **The Scene Tree:** Syncing entity hierarchy between C++ and Java.
*   **[Next]** **Inspector Binding:** Real-time property editing via the Editor's UI.
*   **[Next]** **Renderer2D Batching:** Optimizing for 32+ texture slots.

## Phase 3: The Scripting Layer (v2026.0.3)
*   **LUA/C# Integration:** Flexible gameplay logic layer.
*   **Hot Reload:** Instant logic updates.

## Phase 4: Advanced Graphics & 3D (v2026.0.4)
*   **Vulkan Backend:** Peak performance RHI.
*   **3D Foundation:** Model loading and PBR.
*   **Plugin API:** Java-based Editor expansion.
