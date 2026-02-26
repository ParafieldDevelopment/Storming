# Storming Engine

A modular engine architecture with a Java-based Editor and C++ High Performance Core.

## Project Structure
- `src/main/java`: Java-based Editor source code.
- `src/main/resources`: Editor assets and resources.
- `Engine/`: C++ Core engine (2D/3D).
- `Plugins/`: Lua-based plugin system.
- `Docs/`: Technical documentation and tutorials.

## Building the Editor
The editor uses Gradle.
```bash
./gradlew build
```

To run the editor:
```bash
./gradlew run
```

## Building the Engine
The engine uses CMake.
```bash
cd Engine/2D
mkdir build && cd build
cmake ..
make
```