package com.parafield.storming.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
     * Scans the default StormingProjects directory for valid .storm projects.
     */
    private static List<ProjectEntry> scanForProjects() {
        List<ProjectEntry> found = new ArrayList<>();
        Path defaultPath = Paths.get(System.getProperty("user.home"), "StormingProjects");
        
        if (!Files.exists(defaultPath)) return found;

        try {
            Files.list(defaultPath).forEach(p -> {
                if (Files.isDirectory(p)) {
                    // Look for a .storm file inside
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
