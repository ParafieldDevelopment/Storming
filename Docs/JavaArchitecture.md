# Storming Engine: Java Editor Documentation

This document provides an overview of the Java-based Editor for the Storming Engine, detailing its core systems, UI components, and current development status.

## Core Application Lifecycle

*   **`EditorApp`**: The entry point for the Java application. It manages global themes (using FlatLaf), configures UI refinements, and handles the initial startup sequence (Splash Screen -> Project Selector -> Main Window).
*   **`EngineLauncher`**: Responsible for the lifecycle of the C++ Storming Engine process. It handles launching with shared memory arguments, capturing engine logs, and graceful termination.

## Window Management

*   **`MainWindow`**: The central orchestrator of the editor's layout. It manages complex nested split panes, JetBrains-style sidebars, and the main workspace tabs.
*   **`ProjectSelectorWindow`**: The initial launcher for the engine. Features a modern, animated interface for project management.
*   **`SimulationWindow`**: A dedicated debugging window for running engine simulations with integrated playback controls and real-time statistics.
*   **`SplashWindow`**: A simple, stylized startup window displayed during initialization.

## UI Components & Utilities

*   **`ToolWindow`**: A standardized container used for various editor panels (e.g., Inspector, Hierarchy), featuring a unified header and action buttons.
*   **`SideBar`**: A specialized JetBrains-style sidebar for hosting tool window toggles and action shortcuts.
*   **`StormingMenuBar`**: A custom menu bar supporting alpha-fade animations, integrated playback controls, and a "hamburger" mode for space efficiency.
*   **`UIAnimator`**: A comprehensive animation utility providing cubic ease-out transitions for split panes and alpha properties.
*   **`Icons`**: A centralized resource manager for SVG and PNG icons, supporting automatic scaling and theme-aware fallback.

## Editor Panels & Systems

| Panel | Description | Status |
| :--- | :--- | :--- |
| **`ProjectBrowserPanel`** | A functional file system explorer for navigating the current project directory. | **Implemented** |
| **`TerminalPanel`** | A multi-tabbed terminal emulator using JediTerm and Pty4j. | **Implemented** |
| **`SceneViewPanel`** | A real-time engine viewport utilizing JNA for shared memory image streaming. | **Implemented** |
| **`ConsolePanel`** | A stylized logging interface with level-based filtering and search. | **Implemented** |
| **`InspectorPanel`** | Property editor for scene objects. | **WIP (Placeholder)** |
| **`HierarchyPanel`** | Scene tree navigation and object management. | **WIP (Placeholder)** |
| **`GitPanel`** | Integrated version control interface. | **WIP (Placeholder)** |
| **`PRPanel`** | Pull Request management and detail tracking. | **WIP (Placeholder)** |
| **`AnalyzerPanel`** | Script and asset health analyzer. | **WIP (Placeholder)** |

## Development Note (WIP Features)

As outlined in the [Roadmap](Roadmap.md), several high-level editor systems (Git, PR, Hierarchy, Inspector, and Analyzer) are currently in an early **Work-in-Progress (WIP)** state. These components serve as visual placeholders and utilize mock data to illustrate the final intended user experience. They will be incrementally integrated with the C++ engine and external providers in upcoming development phases.
