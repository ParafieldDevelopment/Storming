package com.parafield.storming.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages project history and discovery for the Storming Engine.
 * Handles loading/saving the recent projects list and scanning for existing projects.
 */
public class ProjectManager {

    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".storming";
    private static final String RECENTS_FILE = CONFIG_DIR + File.separator + "recent_projects.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record ProjectEntry(String name, String path, long lastOpened) {}

    /**
     * Retrieves the list of recently opened projects, sorted by most recent.
     * Also automatically adds projects found in the default StormingProjects directory.
     * @return A list of ProjectEntry objects.
     */
    public static List<ProjectEntry> getRecentProjects() {
        List<ProjectEntry> recents = loadRecents();
        
        // Ensure the folders still exist
        recents = recents.stream()
                .filter(e -> new File(e.path()).exists())
                .collect(Collectors.toList());

        // Scan default directory for any missing projects
        List<ProjectEntry> discovered = scanForProjects();
        for (ProjectEntry d : discovered) {
            if (recents.stream().noneMatch(r -> r.path().equals(d.path()))) {
                recents.add(d);
            }
        }

        return recents.stream()
                .sorted((a, b) -> Long.compare(b.lastOpened(), a.lastOpened()))
                .collect(Collectors.toList());
    }

    /**
     * Adds a project to the recent list or updates its last opened timestamp.
     * @param path The absolute path to the project directory.
     */
    public static void addRecentProject(String path) {
        File projectDir = new File(path);
        if (!projectDir.exists()) return;

        List<ProjectEntry> recents = loadRecents();
        recents.removeIf(e -> e.path().equals(path));
        recents.add(new ProjectEntry(projectDir.getName(), path, System.currentTimeMillis()));

        saveRecents(recents);
    }

    private static List<ProjectEntry> loadRecents() {
        File file = new File(RECENTS_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<ProjectEntry>>() {}.getType();
            List<ProjectEntry> list = GSON.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void saveRecents(List<ProjectEntry> recents) {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            try (FileWriter writer = new FileWriter(RECENTS_FILE)) {
                GSON.toJson(recents, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the tiered configuration for a project:
     * 1. project.storm (Base - Shared)
     * 2. user.storm (Overrides - Private)
     * 3. local.storm (Overrides - Machine-specific)
     */
    public static JsonObject loadProjectConfig(File projectRoot) {
        JsonObject config = new JsonObject();
        File[] files = projectRoot.listFiles((dir, name) -> name.endsWith(".storm") && !name.endsWith(".user.storm") && !name.endsWith(".local.storm"));
        if (files != null && files.length > 0) {
            try {
                config = JsonParser.parseString(Files.readString(files[0].toPath())).getAsJsonObject();
                mergeConfig(config, loadTier(projectRoot, files[0].getName().replace(".storm", ".user.storm")));
                mergeConfig(config, loadTier(projectRoot, files[0].getName().replace(".storm", ".local.storm")));
            } catch (IOException e) { e.printStackTrace(); }
        }
        return config;
    }

    private static JsonObject loadTier(File root, String fileName) {
        File f = new File(root, fileName);
        if (!f.exists()) return new JsonObject();
        try {
            return JsonParser.parseString(Files.readString(f.toPath())).getAsJsonObject();
        } catch (IOException e) { return new JsonObject(); }
    }

    private static void mergeConfig(JsonObject target, JsonObject source) {
        for (String key : source.keySet()) {
            target.add(key, source.get(key));
        }
    }

    public static void saveUserConfig(File projectRoot, JsonObject config) {
        File[] files = projectRoot.listFiles((dir, name) -> name.endsWith(".storm") && !name.endsWith(".user.storm") && !name.endsWith(".local.storm"));
        if (files != null && files.length > 0) {
            try (FileWriter writer = new FileWriter(new File(projectRoot, files[0].getName().replace(".storm", ".user.storm")))) {
                GSON.toJson(config, writer);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private static List<ProjectEntry> scanForProjects() {
        List<ProjectEntry> found = new ArrayList<>();
        Path defaultPath = Paths.get(System.getProperty("user.home"), "StormingProjects");
        
        if (!Files.exists(defaultPath)) return found;

        try {
            Files.list(defaultPath).forEach(p -> {
                if (Files.isDirectory(p)) {
                    try {
                        Files.list(p).filter(f -> f.toString().endsWith(".storm")).findFirst().ifPresent(f -> {
                            found.add(new ProjectEntry(p.getFileName().toString(), p.toAbsolutePath().toString(), 0));
                        });
                    } catch (IOException ignored) {}
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return found;
    }
}


