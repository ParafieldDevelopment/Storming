package com.parafield.storming.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class EngineLauncher {

    private final String enginePath;
    private final Consumer<String> logConsumer;

    public EngineLauncher(String enginePath, Consumer<String> logConsumer) {
        this.enginePath = enginePath;
        this.logConsumer = logConsumer;
    }

    public void launch() {
        logConsumer.accept("Launching Storming Engine...");
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(enginePath);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsumer.accept("[Engine] " + line);
                }
            } catch (Exception ex) {
                logConsumer.accept("[Error] Failed to launch: " + ex.getMessage());
            }
        }).start();
    }
}
