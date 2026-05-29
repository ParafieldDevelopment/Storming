# Storming Engine: The "Anything Engine" Vision

Storming is designed to be a high-performance, extremely flexible engine that scales from lightweight 2D games to massive, multi-threaded 3D simulations.

## Phase 0: The Foundation (v2026.0.0) - [COMPLETE]
*   **Decoupled Architecture:** Java Editor + C++20 Core.
*   **SHM Pipeline:** Shared Memory for near-zero latency viewport streaming.
*   **JSON Command Bridge:** Non-blocking STDIN/STDOUT IPC.
*   **Telemetry:** Real-time engine health and performance monitoring.

## Phase 1: Core & Property Sync (v2026.1.0) - [CURRENT]
*   **[Done]** **Modern Editor UI:** FlatLaf-based IntelliJ-style workspace.
*   **[Done]** **ECS Core:** Data-oriented architecture using EnTT.
*   **[Done]** **Inspector Binding:** Real-time bi-directional property editing between Java and C++.
*   **[In Progress]** **Project Format:** Standardized `.storm` and `.storm_scene` JSON serialization.
*   **[Next]** **The Asset Pipeline:** Threaded texture/mesh loading and asset registry.

## Phase 2: The Physics & Build Bridge (v2026.2.0)
*   **[Done] Rust/Rapier Integration:** Implementing the Rapier physics engine via a Rust-C++ FFI bridge.
*   **[Done] FFI Bridge:** Successfully established manual linking for MinGW and basic world control.
*   **[Done] ECS Integration:** Synchronizing EnTT entities with Rapier RigidBodies.
*   **Corrosion Build System:** Native integration of Cargo into the CMake build flow.
*   **Deterministic Physics:** Setting the stage for multi-platform consistency.
*   **2D-to-3D Scaling:** Designing physics components to handle both Rapier2D and Rapier3D seamlessly.

## Phase 3: Scripting & Logic (v2026.3.0)
*   **High-Level Scripting:** Integration of Lua or C# (via Mono/NativeAOT) for gameplay logic.
*   **Hot Reloading:** Instant iteration for logic and physics parameters.
*   **Visual Scripting Prototype:** Early stage node-based logic system.

## Phase 4: High-Performance Graphics (v2026.4.0)
*   **Vulkan RHI:** Moving beyond OpenGL 4.5 to a modern Vulkan Rendering Hardware Interface.
*   **3D Core:** PBR (Physically Based Rendering), Model loading (glTF), and 3D Camera systems.
*   **Global Illumination:** Basic implementation of advanced lighting techniques.

## Phase 5: The Ecosystem (v2026.5.0)
*   **Plugin API:** Java-based API for expanding Editor functionality.
*   **Storming Store:** A package manager for sharing assets and scripts.
*   **Multi-Platform Export:** One-click export for Windows, Linux, and Mac.
