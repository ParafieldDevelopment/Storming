package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
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
        void onFinished(boolean success, String buildLogs);
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
            StringBuilder logs = new StringBuilder();
            try {
                String os = System.getProperty("os.name").toLowerCase();
                java.io.File buildDir = new java.io.File("Engine/2D/build");
                if (!buildDir.exists()) buildDir.mkdirs();

                listener.onStatus("Initializing CMake...");
                listener.onProgress(5);
                
                ProcessBuilder cmakePb = new ProcessBuilder("cmake", "..");
                cmakePb.directory(buildDir);
                cmakePb.redirectErrorStream(true);
                Process cmakeP = cmakePb.start();
                
                captureOutput(cmakeP.getInputStream(), logs, listener, 5, 40, "CMake");
                
                if (cmakeP.waitFor() != 0) {
                    listener.onFinished(false, logs.toString());
                    return;
                }

                listener.onStatus("Compiling Engine Core...");
                List<String> cmd = new ArrayList<>();
                if (os.contains("win")) {
                    cmd.addAll(List.of("cmake", "--build", "."));
                } else {
                    cmd.addAll(List.of("make", "-j" + Runtime.getRuntime().availableProcessors()));
                }
                
                ProcessBuilder buildPb = new ProcessBuilder(cmd);
                buildPb.directory(buildDir);
                buildPb.redirectErrorStream(true);
                Process p = buildPb.start();
                
                captureOutput(p.getInputStream(), logs, listener, 40, 100, "Build");

                boolean success = p.waitFor() == 0;
                listener.onFinished(success, logs.toString());
            } catch (Exception e) {
                listener.onFinished(false, logs.toString() + "\n[Internal Error] " + e.getMessage());
            }
        }).start();
    }

    /**
     * Reads output byte-by-character to ensure the UI updates even without newlines (crucial for downloads).
     */
    private void captureOutput(InputStream is, StringBuilder logAccumulator, BuildListener listener, int startProgress, int endProgress, String prefix) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder lineBuffer = new StringBuilder();
        int c;
        int lineCount = 0;
        
        while ((c = reader.read()) != -1) {
            char charRead = (char) c;
            lineBuffer.append(charRead);
            
            if (charRead == '\n' || charRead == '\r') {
                String line = lineBuffer.toString().trim();
                if (!line.isEmpty()) {
                    broadcast("[" + prefix + "] " + line);
                    logAccumulator.append(line).append("\n");
                    
                    // Update Status
                    String status = line;
                    if (status.startsWith("--")) status = status.substring(2).trim();
                    if (status.length() > 60) status = status.substring(0, 57) + "...";
                    listener.onStatus(prefix + ": " + status);
                    
                    // Fake progress increment to keep the bar moving
                    lineCount++;
                    int current = startProgress + Math.min(endProgress - startProgress - 1, lineCount / 3);
                    listener.onProgress(current);
                }
                lineBuffer.setLength(0);
            } else if (lineBuffer.length() > 120) { // Safety for very long non-newline output
                String partial = lineBuffer.toString().trim();
                listener.onStatus(prefix + ": " + partial);
                lineBuffer.setLength(0);
            }
        }
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
