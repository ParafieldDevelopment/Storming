package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Responsible for launching and managing the Storming Engine process.
 * Handles process lifecycle, shared memory arguments, and output logging.
 */
public class EngineLauncher {

    private final String enginePath;
    private final Consumer<String> logConsumer;
    private Process currentProcess;

    /**
     * Constructs a new EngineLauncher.
     *
     * @param enginePath The file path to the engine executable.
     * @param logConsumer A callback for handling engine log messages.
     */
    public EngineLauncher(String enginePath, Consumer<String> logConsumer) {
        this.enginePath = enginePath;
        this.logConsumer = logConsumer;
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
            logConsumer.accept("[System] Stopping Engine...");
            currentProcess.destroy();
            try {
                // Give it a moment to shut down gracefully before forcing
                if (!currentProcess.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    currentProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logConsumer.accept("[System] Engine Stopped.");
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
     * Starts the engine in a separate thread and captures its output.
     *
     * @param shmName The shared memory name to pass as an argument.
     */
    public void launch(String shmName) {
        if (isRunning()) {
            return;
        }

        logConsumer.accept("Launching Storming Engine...");
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
                    logConsumer.accept(line);
                }
                
                int exitCode = currentProcess.waitFor();
                logConsumer.accept("[System] Engine exited with code: " + exitCode);
            } catch (Exception ex) {
                logConsumer.accept("[Error] Failed to launch: " + ex.getMessage());
            }
        }).start();
    }
}
