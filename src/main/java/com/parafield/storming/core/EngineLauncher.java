package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Responsible for launching and managing the Storming Engine process.
 * Handles process lifecycle, shared memory arguments, and broadcasting output to multiple listeners.
 */
public class EngineLauncher {

    private final String enginePath;
    private final List<Consumer<String>> logListeners = new ArrayList<>();
    private Process currentProcess;

    /**
     * Constructs a new EngineLauncher.
     *
     * @param enginePath The file path to the engine executable.
     */
    public EngineLauncher(String enginePath) {
        this.enginePath = enginePath;
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

    /**
     * Checks if the engine process is currently running.
     *
     * @return true if the engine process exists and is alive, false otherwise.
     */
    public boolean isRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }

    /**
     * Stops the running engine process gracefully, then forcibly if it fails to exit.
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
        }
    }

    /**
     * Launches the engine process without additional arguments.
     */
    public void launch() {
        launch("");
    }

    /**
     * Launches the engine process with an optional shared memory name.
     * Starts the engine in a separate thread and broadcasts its output.
     *
     * @param shmName The shared memory name to pass as an argument.
     */
    public void launch(String shmName) {
        if (isRunning()) {
            return;
        }

        broadcast("Launching Storming Engine...");
        new Thread(() -> {
            try {
                ProcessBuilder pb;
                if (!shmName.isEmpty()) {
                    pb = new ProcessBuilder(enginePath, "--shm", shmName);
                } else {
                    pb = new ProcessBuilder(enginePath);
                }
                
                pb.redirectErrorStream(true);
                currentProcess = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    broadcast(line);
                }
                
                int exitCode = currentProcess.waitFor();
                broadcast("[System] Engine exited with code: " + exitCode);
            } catch (Exception ex) {
                broadcast("[Error] Failed to launch: " + ex.getMessage());
            }
        }).start();
    }
}
