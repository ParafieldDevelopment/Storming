package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class EngineLauncher {

    private final String enginePath;
    private final Consumer<String> logConsumer;
    private Process currentProcess;

    public EngineLauncher(String enginePath, Consumer<String> logConsumer) {
        this.enginePath = enginePath;
        this.logConsumer = logConsumer;
    }

    public boolean isRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }

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

    public void launch() {
        launch(0);
    }

    public void launch(long parentWindowID) {
        if (isRunning()) {
            return;
        }

        logConsumer.accept("Launching Storming Engine...");
        new Thread(() -> {
            try {
                ProcessBuilder pb;
                if (parentWindowID != 0) {
                    pb = new ProcessBuilder(enginePath, "--parent-id", String.valueOf(parentWindowID));
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
