package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Responsible for launching and managing the Storming Engine process.
 * Handles process lifecycle, shared memory arguments, and broadcasting output.
 * Features a telemetry bridge and a command gateway.
 * Automatically attempts to build the engine if the executable is missing.
 */
public class EngineLauncher {

    private final String enginePath;
    private final List<Consumer<String>> logListeners = new ArrayList<>();
    private final List<Consumer<String>> telemetryListeners = new ArrayList<>();
    private Process currentProcess;
    private BufferedWriter writer;

    /**
     * Constructs a new EngineLauncher.
     *
     * @param enginePath The file path to the engine executable.
     */
    public EngineLauncher(String enginePath) {
        this.enginePath = enginePath;
    }

    /**
     * Adds a listener for telemetry data (JSON strings).
     * @param listener The callback for telemetry.
     */
    public void addTelemetryListener(Consumer<String> listener) {
        telemetryListeners.add(listener);
    }

    /**
     * Removes a telemetry listener.
     * @param listener The callback to remove.
     */
    public void removeTelemetryListener(Consumer<String> listener) {
        telemetryListeners.remove(listener);
    }

    /**
     * Adds a listener to receive engine log messages.
     * @param listener The callback to add.
     */
    public void addLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }

    /**
     * Removes a log listener.
     * @param listener The callback to remove.
     */
    public void removeLogListener(Consumer<String> listener) {
        logListeners.remove(listener);
    }

    private void broadcast(String message) {
        for (Consumer<String> listener : new ArrayList<>(logListeners)) {
            listener.accept(message);
        }
    }

    private void broadcastTelemetry(String data) {
        for (Consumer<String> listener : new ArrayList<>(telemetryListeners)) {
            listener.accept(data);
        }
    }

    /**
     * Sends a command string to the engine's standard input.
     * @param command The command to send (e.g., JSON).
     */
    public void sendCommand(String command) {
        if (writer != null) {
            try {
                writer.write(command);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                broadcast("[Error] Failed to send command: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if the engine process is currently running.
     *
     * @return true if the engine process exists and is alive, false otherwise.
     */
    public boolean isRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }

    /**
     * Stops the running engine process gracefully.
     */
    public void stop() {
        if (isRunning()) {
            broadcast("[System] Stopping Engine...");
            currentProcess.destroy();
            try {
                if (!currentProcess.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    currentProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            broadcast("[System] Engine Stopped.");
            writer = null;
        }
    }

    private boolean buildEngine() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            // 1. Create build directory
            java.io.File buildDir = new java.io.File("Engine/2D/build");
            if (!buildDir.exists()) buildDir.mkdirs();

            // 2. Run CMake
            broadcast("[System] Running CMake...");
            ProcessBuilder cmakePb = new ProcessBuilder("cmake", "..");
            cmakePb.directory(buildDir);
            Process cmakeP = cmakePb.start();
            if (cmakeP.waitFor() != 0) return false;

            // 3. Run Build
            broadcast("[System] Compiling Engine...");
            List<String> cmd = new ArrayList<>();
            if (os.contains("win")) {
                cmd.addAll(List.of("cmake", "--build", "."));
            } else {
                cmd.addAll(List.of("make", "-j" + Runtime.getRuntime().availableProcessors()));
            }
            
            ProcessBuilder buildPb = new ProcessBuilder(cmd);
            buildPb.directory(buildDir);
            Process buildP = buildPb.start();
            
            return buildP.waitFor() == 0;
        } catch (Exception e) {
            broadcast("[Error] Build exception: " + e.getMessage());
            return false;
        }
    }

    public void launch() {
        launch("");
    }

    public void launch(String shmName) {
        launch(shmName, false);
    }

    /**
     * Launches the engine process and starts monitoring its output.
     *
     * @param shmName  The shared memory name to pass as an argument.
     * @param isEditor Whether to launch the engine in editor mode.
     */
    public void launch(String shmName, boolean isEditor) {
        if (isRunning()) {
            return;
        }

        new Thread(() -> {
            try {
                java.io.File exe = new java.io.File(enginePath);
                if (!exe.exists()) {
                    broadcast("[System] Engine binary not found. Attempting automatic build...");
                    if (!buildEngine()) {
                        broadcast("[Error] Auto-build failed. Please check C++ environment.");
                        return;
                    }
                }

                broadcast("Launching Storming Engine...");
                List<String> args = new ArrayList<>();
                args.add(enginePath);
                if (!shmName.isEmpty()) {
                    args.add("--shm");
                    args.add(shmName);
                }
                if (isEditor) {
                    args.add("--editor");
                }
                
                ProcessBuilder pb = new ProcessBuilder(args);
                pb.redirectErrorStream(true);
                currentProcess = pb.start();
                
                writer = new BufferedWriter(new OutputStreamWriter(currentProcess.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[TELEMETRY]")) {
                        broadcastTelemetry(line.substring(11));
                    } else {
                        broadcast(line);
                    }
                }
                
                int exitCode = currentProcess.waitFor();
                broadcast("[System] Engine exited with code: " + exitCode);
                writer = null;
            } catch (Exception ex) {
                broadcast("[Error] Failed to launch: " + ex.getMessage());
            }
        }).start();
    }
}
