package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Responsible for launching and managing the Storming Engine process.
 */
public class EngineLauncher {

    private final String enginePath;
    private final List<Consumer<String>> logListeners = new ArrayList<>();
    private final List<Consumer<String>> telemetryListeners = new ArrayList<>();
    private final List<String> lastLogs = new LinkedList<>();
    private static final int MAX_SAVED_LOGS = 50;

    private Process currentProcess;
    private BufferedWriter writer;
    private boolean intentionalStop = false;

    public interface CrashListener {
        void onEngineCrashed(int exitCode, String logs);
    }
    private CrashListener crashListener;

    public interface BuildListener {
        void onStatus(String status);
        void onProgress(int progress);
        void onFinished(boolean success);
        default void onLog(String log) {}
    }

    public EngineLauncher(String enginePath) {
        this.enginePath = enginePath;
    }

    public void setCrashListener(CrashListener listener) {
        this.crashListener = listener;
    }

    public void addTelemetryListener(Consumer<String> listener) { telemetryListeners.add(listener); }
    public void removeTelemetryListener(Consumer<String> listener) { telemetryListeners.remove(listener); }
    public void addLogListener(Consumer<String> listener) { logListeners.add(listener); }
    public void removeLogListener(Consumer<String> listener) { logListeners.remove(listener); }

    private void broadcast(String message) {
        synchronized (lastLogs) {
            lastLogs.add(message);
            if (lastLogs.size() > MAX_SAVED_LOGS) lastLogs.remove(0);
        }
        for (Consumer<String> listener : new ArrayList<>(logListeners)) {
            listener.accept(message);
        }
    }

    private void broadcastTelemetry(String data) {
        for (Consumer<String> listener : new ArrayList<>(telemetryListeners)) {
            listener.accept(data);
        }
    }

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

    public boolean isRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }

    public boolean binaryExists() {
        return new java.io.File(enginePath).exists();
    }

    public void buildEngine(BuildListener listener) {
        new Thread(() -> {
            try {
                java.io.File buildDir = new java.io.File("Engine/2D/build");
                if (!buildDir.exists()) buildDir.mkdirs();

                broadcast("[System] Starting Engine Build...");
                
                // --- Step 1: Build Rust Physics ---
                listener.onStatus("Compiling Physics Engine (Rust)...");
                listener.onProgress(5);
                ProcessBuilder rustPb = new ProcessBuilder("cargo", "build", "--target", "x86_64-pc-windows-gnu");
                rustPb.directory(new java.io.File("Engine/Physics"));
                rustPb.redirectErrorStream(true);
                Process rustP = rustPb.start();
                BufferedReader rustReader = new BufferedReader(new InputStreamReader(rustP.getInputStream()));
                String rl;
                while ((rl = rustReader.readLine()) != null) {
                    broadcast("[Rust] " + rl);
                    listener.onLog("[Rust] " + rl);
                }
                if (rustP.waitFor() != 0) {
                    broadcast("[Error] Rust physics build failed.");
                    listener.onFinished(false);
                    return;
                }

                // --- Step 2: CMake Configure ---
                listener.onStatus("Configuring Project (CMake)...");
                listener.onProgress(20);
                
                ProcessBuilder cmakePb = new ProcessBuilder("cmake", "..");
                cmakePb.directory(buildDir);
                cmakePb.redirectErrorStream(true);
                Process cmakeP = cmakePb.start();
                
                BufferedReader cmakeReader = new BufferedReader(new InputStreamReader(cmakeP.getInputStream()));
                String cl;
                while ((cl = cmakeReader.readLine()) != null) {
                    broadcast("[CMake] " + cl);
                    listener.onLog("[CMake] " + cl);
                }

                if (cmakeP.waitFor() != 0) {
                    broadcast("[Error] CMake configuration failed.");
                    listener.onFinished(false);
                    return;
                }

                listener.onStatus("Compiling Engine Core...");
                listener.onProgress(40);
                
                ProcessBuilder buildPb = new ProcessBuilder("cmake", "--build", ".");
                buildPb.directory(buildDir);
                buildPb.redirectErrorStream(true);
                Process p = buildPb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    broadcast("[Build] " + line);
                    listener.onLog("[Build] " + line);
                    count++;
                    if (count % 5 == 0 && count < 40) listener.onProgress(40 + (count / 2));
                }

                boolean success = p.waitFor() == 0;
                if (!success) broadcast("[Error] Build failed. Check the logs above.");
                else broadcast("[System] Build successful!");
                
                listener.onProgress(100);
                listener.onFinished(success);
            } catch (Exception e) {
                broadcast("[Error] Build process exception: " + e.getMessage());
                listener.onLog("[Error] " + e.getMessage());
                e.printStackTrace();
                listener.onFinished(false);
            }
        }).start();
    }

    public void stop() {
        if (isRunning()) {
            intentionalStop = true;
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

    public void launch(String shmName, boolean isEditor) {
        if (isRunning()) return;
        intentionalStop = false;

        new Thread(() -> {
            try {
                broadcast("Launching Storming Engine...");
                List<String> args = new ArrayList<>();
                args.add(enginePath);
                if (!shmName.isEmpty()) { args.add("--shm"); args.add(shmName); }
                if (isEditor) args.add("--editor");
                
                ProcessBuilder pb = new ProcessBuilder(args);
                pb.redirectErrorStream(true);
                currentProcess = pb.start();
                
                writer = new BufferedWriter(new OutputStreamWriter(currentProcess.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[TELEMETRY]")) broadcastTelemetry(line.substring(11));
                    else broadcast(line);
                }
                
                int exitCode = currentProcess.waitFor();
                broadcast("[System] Engine exited with code: " + exitCode);
                writer = null;

                if (!intentionalStop && exitCode != 0) {
                    StringBuilder sb = new StringBuilder();
                    synchronized (lastLogs) {
                        for (String l : lastLogs) sb.append(l).append("\n");
                    }
                    if (crashListener != null) crashListener.onEngineCrashed(exitCode, sb.toString());
                }
            } catch (Exception ex) {
                broadcast("[Error] Process execution failed: " + ex.getMessage());
            }
        }).start();
    }
}
