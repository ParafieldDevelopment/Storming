package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

public class EngineLauncher {

    private final String enginePath;
    private final String engineDir;
    private final Consumer<String> logConsumer;
    private Process currentProcess;

    public EngineLauncher(String engineDir, Consumer<String> logConsumer) {
        this.engineDir = engineDir;
        this.logConsumer = logConsumer;

        // Determine executable path based on OS
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            this.enginePath = new File(engineDir, "build/bin/Debug/StormingEngine.exe").getPath();
        } else {
            this.enginePath = new File(engineDir, "build/bin/StormingEngine").getPath();
        }
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
        launch("");
    }

    public void sendCommand(String command) {
        if (currentProcess != null && currentProcess.isAlive()) {
            try {
                currentProcess.getOutputStream().write((command + "\n").getBytes()); // Add newline for getline in C++
                currentProcess.getOutputStream().flush();
            } catch (Exception e) {
                logConsumer.accept("[Error] Failed to send command to engine: " + e.getMessage());
            }
        }
    }

    public boolean launch(String shmName) {
        if (isRunning()) {
            return true;
        }

        File engineFile = new File(enginePath);
        if (!engineFile.exists()) {
            if (!buildEngine()) {
                logConsumer.accept("[Error] Engine build failed or was cancelled. Cannot launch.");
                return false;
            }
        }

        logConsumer.accept("Launching Storming Engine...");
        try {
            // engineFile is already defined and checked for existence earlier in the method.
            // No need to redefine it here.
            ProcessBuilder pb;
            if (!shmName.isEmpty()) {
                pb = new ProcessBuilder(engineFile.getAbsolutePath(), "--shm", shmName);
            } else {
                pb = new ProcessBuilder(engineFile.getAbsolutePath());
            }
            
            // Set the working directory to the executable's directory
            pb.directory(engineFile.getParentFile());
            pb.redirectErrorStream(true);
            currentProcess = pb.start();
            
            // Start a new thread to consume output to prevent deadlock
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logConsumer.accept(line);
                    }
                } catch (Exception e) {
                    logConsumer.accept("[Error] Engine output reader error: " + e.getMessage());
                }
            }).start();
            
            // Wait briefly to check if the process started successfully, e.g., for immediate crash
            if (!currentProcess.isAlive()) {
                logConsumer.accept("[Error] Engine process terminated immediately after launch. Exit code: " + currentProcess.exitValue());
                return false;
            }
            logConsumer.accept("[System] Engine process started successfully.");
            return true;
        } catch (Exception ex) {
            logConsumer.accept("[Error] Failed to launch: " + ex.getMessage());
            return false;
        }
    }

    private boolean buildEngine() {
        int result = JOptionPane.showConfirmDialog(null,
                "Storming Engine executable not found.\nWould you like to build it now? (Requires CMake)",
                "Engine Not Found",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return false;
        }

        logConsumer.accept("[System] Starting CMake build process...");
        try {
            // Step 1: Configure
            runBuildProcess(new ProcessBuilder("cmake", "-S", ".", "-B", "build"), "Configure");

            // Step 2: Build
            runBuildProcess(new ProcessBuilder("cmake", "--build", "build"), "Build");
            
            logConsumer.accept("[System] Build finished.");
            return new File(enginePath).exists();
        } catch (Exception e) {
            logConsumer.accept("[Error] Build failed: " + e.getMessage());
            return false;
        }
    }

    private void runBuildProcess(ProcessBuilder pb, String stepName) throws Exception {
        logConsumer.accept("[Build] Running " + stepName + " step...");
        pb.directory(new File(engineDir));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            logConsumer.accept("[CMake] " + line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(stepName + " step failed with exit code: " + exitCode);
        }
    }
}
