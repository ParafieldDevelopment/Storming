package com.parafield.storming.core;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages Discord Rich Presence for the Storming Engine.
 */
public class DiscordRPCManager {

    private static final String CLIENT_ID = "1477836162717581412"; 
    private static final DiscordRPC RPC = DiscordRPC.INSTANCE;
    
    private static boolean running = false;
    private static boolean connected = false;
    private static long sessionStartTime = 0;
    
    private static final List<Consumer<String>> logListeners = new ArrayList<>();
    private static final List<String> logBuffer = new ArrayList<>();

    // Store the last requested activity to send it as soon as we connect
    private static String lastState = "Idle";
    private static String lastDetails = "Managing Projects";

    public static void addLogListener(Consumer<String> listener) {
        logListeners.add(listener);
        for (String msg : logBuffer) listener.accept(msg);
    }

    private static void log(String msg) {
        System.out.println(msg);
        if (logListeners.isEmpty()) logBuffer.add(msg);
        else for (Consumer<String> l : logListeners) l.accept(msg);
    }

    public static void init() {
        log("[System] Initializing Discord RPC...");

        DiscordEventHandlers handlers = new DiscordEventHandlers();
        
        handlers.ready = (user) -> {
            connected = true;
            log("[System] Discord RPC Connected: " + user.username);
            // SEND PENDING UPDATE NOW THAT WE ARE CONNECTED
            refreshPresence();
        };
        
        handlers.errored = (code, message) -> log("[Error] Discord RPC Error (" + code + "): " + message);
        handlers.disconnected = (code, message) -> {
            connected = false;
            log("[System] Discord RPC Disconnected: " + message);
        };

        try {
            RPC.Discord_Initialize(CLIENT_ID, handlers, true, "");
            running = true;
            sessionStartTime = System.currentTimeMillis() / 1000;

            Thread callbackThread = new Thread(() -> {
                while (running) {
                    RPC.Discord_RunCallbacks();
                    try {
                        Thread.sleep(1000); 
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "DiscordRPC-Callback-Handler");
            callbackThread.setDaemon(true);
            callbackThread.start();
        } catch (Throwable t) {
            log("[Error] Discord RPC failed to load native library!");
        }
    }

    public static void updateActivity(String state, String details) {
        lastState = state;
        lastDetails = details;
        if (connected) {
            refreshPresence();
        }
    }

    private static void refreshPresence() {
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.state = lastState;
        presence.details = "Storming: " + lastDetails;
        
        // RE-ENABLED ASSETS
        presence.largeImageKey = "storming_logo"; 
        presence.largeImageText = "Storming Engine";
        
        presence.startTimestamp = sessionStartTime;
        RPC.Discord_UpdatePresence(presence);
        log("[System] Discord Presence Synced: " + lastState);
    }

    public static void shutdown() {
        running = false;
        connected = false;
        RPC.Discord_Shutdown();
        log("[System] Discord RPC Shutdown.");
    }
}
