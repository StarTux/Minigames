package com.winthier.minigames.game;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.game.Game;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class GameLoader {
    private final MinigamesPlugin plugin;
    private final Map<String, Class<Game>> gameClasses = new HashMap<String, Class<Game>>();

    public GameLoader(MinigamesPlugin plugin) {
        this.plugin = plugin;
    }

    public Class<Game> getGameClass(String name) {
        return gameClasses.get(name);
    }

    public void loadAll() {
        File gamesFolder = plugin.getGamesFolder();
        if (!gamesFolder.exists()) gamesFolder.mkdir();
        for (File file : gamesFolder.listFiles()) {
            if (!file.getName().endsWith(".jar")) continue;
            try {
                load(file);
            } catch (Exception e) {
                plugin.getLogger().warning("Skipping game file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void load(File file) throws Exception {
        JarFile jarFile = new JarFile(file);
        JarEntry entry = jarFile.getJarEntry("game.yml");
        if (entry == null) {
            throw new FileNotFoundException("Cannot find game.yml");
        }
        InputStream in = jarFile.getInputStream(entry);
        InputStreamReader reader = new InputStreamReader(in);
        ConfigurationSection config = YamlConfiguration.loadConfiguration(reader);
        URL[] url = new URL[1];
        url[0] = file.toURI().toURL();
        URLClassLoader loader = new URLClassLoader(url, getClass().getClassLoader());
        load(loader, config.getString("name"), config.getString("main"));
    }

    @SuppressWarnings("unchecked")
    private void load(URLClassLoader loader, String name, String main) throws Exception {
        if (name == null) {
            throw new NullPointerException("game.yml: Main:" + name + ": string value expected");
        }
        if (main == null) {
            throw new NullPointerException("game.yml: Main:" + main + ": string value expected");
        }
        Class<?> clazz = Class.forName(main, true, loader);
        if (!Game.class.isAssignableFrom(clazz)) {
            throw new ClassNotFoundException(main + ": Not a subclass of Game");
        }
        gameClasses.put(name, (Class<Game>)clazz);
        plugin.getLogger().info("Loaded game " + name + ".");
    }
}
