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
 */
public class EngineLauncher {

    private final String enginePath;
    private final List<Consumer<String>> logListeners = new ArrayList<>();
    private Consumer<String> telemetryListener;
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
     * Sets a listener for telemetry data (JSON strings).
     * @param listener The callback for telemetry.
     */
    public void setTelemetryListener(Consumer<String> listener) {
        this.telemetryListener = listener;
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

    public void launch() {
        launch("");
    }

    /**
     * Launches the engine process and starts monitoring its output.
     * Extracts telemetry lines prefixed with [TELEMETRY].
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
                
                writer = new BufferedWriter(new OutputStreamWriter(currentProcess.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[TELEMETRY]") && telemetryListener != null) {
                        telemetryListener.accept(line.substring(11));
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
