# Storming Engine: C++ Core Architecture

This document provides a technical overview of the C++ Core of the Storming Engine. The engine is designed as a high-performance, decoupled renderer that communicates with the Java Editor via a strict JSON-based IPC (Inter-Process Communication) protocol.

## Dual-Engine Architecture

The engine supports two distinct operational modes, allowing for a Godot-style workflow where the Editor workspace and the Game runtime remain separate.

### 1. Editor Mode (`--editor`)
Used for the background workspace view.
*   **Visual Overlays**: Renders an infinite grid and coordinate origin cross (Red X, Green Y).
*   **Selection Highlighting**: Draws a yellow bounding box around the currently selected entity.
*   **Stateful Tracking**: The engine maintains an internal `m_SelectedEntityID` to facilitate relative transformations.

### 2. Runtime Mode (Default)
Used for the Simulation Window.
*   **Clean View**: No grid or editor-specific overlays.
*   **Performance Focused**: Optimized for running game logic and scripts without editor overhead.

## Core Systems

### Application Lifecycle (`Application.cpp`)
The `Application` class manages the main engine loop, windowing (via SDL3), and the IPC bridge.

*   **Initialization**: Sets up the OpenGL 4.5 Core context and configures the `Renderer2D`. If launched with `--shm`, it initializes a `FrameBuffer` linked to Shared Memory.
*   **Robust IPC**: Processes commands using a line-buffered non-blocking `STDIN` reader. This ensures that high-frequency data (like mouse dragging) is processed reliably without dropping packets.
*   **The Run Loop**:
    1.  **Poll Events**: Handles SDL window events.
    2.  **Process Commands**: Parses and executes incoming JSON commands immediately.
    3.  **Render Frame**: Clears the buffer and calls `Scene::OnUpdate`.
    4.  **Editor Overlays**: If in Editor Mode, renders the grid, origin, and selection box.
    5.  **Sync/Swap**: In Editor Mode, `glFinish()` is called to ensure memory consistency before Java reads the pixel buffer.
    6.  **Telemetry**: Performance stats (FPS, Draw Calls, Quads) are broadcast to `STDOUT` every 500ms.

### Entity Component System (ECS)
The engine uses **EnTT**, a fast, cache-friendly ECS library.

*   **`Scene`**: The container for all game objects. It owns the `entt::registry`.
    *   **`OnUpdate(float ts)`**: Iterates through entities with a `SpriteRendererComponent` and submits them to the `Renderer2D`.
    *   **`PickEntity(float x, float y)`**: Performs AABB (Axis-Aligned Bounding Box) physics checks to find and select entities under the mouse cursor.
    *   **`Serialize()`**: Converts the entire registry into a formatted JSON string for saving.
*   **`Components`**: Pure data structures.
    *   **`TagComponent`**: The entity's name (visible in Hierarchy).
    *   **`TransformComponent`**: Position (Translation), Rotation (Euler Z for 2D), and Scale.
    *   **`SpriteRendererComponent`**: Color (RGBA) and Texture reference.

### Rendering Pipeline (`Renderer2D.cpp`)
A high-performance batch-oriented 2D renderer.

*   **Batching**: Quads and Lines are accumulated in CPU-side buffers and uploaded to the GPU in bulk.
*   **Lines**: Supports a secondary batching path for debug lines, grids, and selection boxes.
*   **Textures**: Supports up to 16 texture slots per draw call using a dynamic array sampler `u_Textures[16]`.

## Inter-Process Communication (IPC)

### Commands (Java -> C++)
Sent to the Engine's `STDIN` as single-line JSON.

| Action | Payload | Description |
| :--- | :--- | :--- |
| **`load_scene`** | `{"path": "..."}` | Clears the current scene and loads entities from a file. |
| **`select_entity`** | `{"id": 123}` | Selects an entity and requests its full details. |
| **`translate_selected`** | `{"dx": val, "dy": val}`| Moves the selected entity by a delta world amount. |
| **`rotate_selected`** | `{"da": val}` | Rotates the selected entity by a delta angle (radians). |
| **`scale_selected`** | `{"ds": val}` | Uniformly scales the selected entity by a delta amount. |
| **`update_component`** | `{"id": 1, ...}` | Directly updates a specific component field (e.g., Color). |
| **`create_entity`** | `{"name": "...", "sprite": bool}` | Spawns a new entity in the active registry. |
| **`delete_entity`** | `{"id": 123}` | Destroys the specified entity. |
| **`request_save`** | `{}` | Requests a full JSON dump of the scene state. |
| **`request_picking`** | `{"x": val, "y": val}` | Performs a hit test at normalized coordinates. |

### Telemetry (C++ -> Java)
Broadcast to `STDOUT` with the `[TELEMETRY]` prefix.

| Type | Payload | Description |
| :--- | :--- | :--- |
| **`scene_tree`** | `{"entities": [...]}` | The full hierarchy list. Sent on any scene change. |
| **`entity_details`** | `{"components": {...}}` | Full component state for the Inspector panel. |
| **`telemetry`** | `{"fps": 60, ...}` | Real-time performance and renderer stats. |
| **`scene_data_dump`** | `{"data": "..."}` | The raw JSON string of the scene, ready for disk writing. |

## Build System

*   **CMake**: Manages the C++ build process.
*   **Key Dependencies**: `SDL3`, `GLAD`, `EnTT`, `GLM`, `nlohmann/json`.
