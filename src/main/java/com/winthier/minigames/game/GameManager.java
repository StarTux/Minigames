package com.winthier.minigames.game;

import com.winthier.minigames.MinigamesPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;

public class GameManager {
    private final MinigamesPlugin plugin;
    private final Map<UUID, Game> games = new HashMap<UUID, Game>();
    private GameLoader gameLoader = null;

    public GameManager(MinigamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                prepareGameLoader();
            }
        }.runTask(plugin);
    }

    private void prepareGameLoader() {
        if (gameLoader != null) return;
        gameLoader = new GameLoader(plugin);
        gameLoader.loadAll();
    }

    private Class<Game> getClass(String name) {
        prepareGameLoader();
        Class<Game> clazz = gameLoader.getGameClass(name);
        return clazz;
    }

    public Game getGame(UUID uuid) {
        return games.get(uuid);
    }

    public List<Game> getGames() {
        return new ArrayList<Game>(games.values());
    }

    public Game createGame(String gameName) {
        Class<Game> clazz = getClass(gameName);
        if (clazz == null) throw new IllegalArgumentException("Unknown game name: " + gameName + ".");
        Game game = null;
        try {
            game = (Game)clazz.getConstructor().newInstance();
            game.setName(gameName);
        } catch (Exception e) {
            throw new RuntimeException("Failed constructing game with class " + clazz.getName() + ".", e);
        }
        games.put(game.getUuid(), game);
        return game;
    }

    public void unregisterGame(Game game) {
        games.remove(game);
        // TODO
    }

    public void clear() {
        // TODO
    }
}
