package com.winthier.minigames.world;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.game.Game;
import com.winthier.minigames.util.BukkitFuture;
import com.winthier.minigames.util.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldManager {
    private final MinigamesPlugin plugin;
    private final Map<World, Game> worldMap = new WeakHashMap<World, Game>();

    /**
     * Map directories to games to remember which game owns which
     * world for the time until it has been loaded and Bukkit has
     * an actual instance of the World.
     */
    private final Map<File, Game> fileMap = new HashMap<File, Game>();

    public WorldManager(MinigamesPlugin plugin) {
        this.plugin = plugin;
    }

    public World createWorld(Game game) {
        final File dir;
        try {
            dir = Files.createTempDir("Minigames", game.getUuid().toString(), plugin.getServer().getWorldContainer());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        final String name = dir.getName();
        final WorldCreator creator = new WorldCreator(name);
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);
        creator.generator(new NullGenerator());
        final World world = Bukkit.getServer().createWorld(creator);
        worldMap.put(world, game);
        return world;
    }

    /**
     * Load a world by name from a the template folder.
     */
    public void loadWorld(final Game game, String name, BukkitFuture<World> callback) {
        File file = name.startsWith("/") ? File.listRoots()[0] : plugin.getTemplateFolder();
        for (String dir : name.split("/")) {
            if (dir == null || dir.isEmpty()) continue;
            file = new File(file, dir);
        }
        loadWorld(game, file, callback);
    }

    /**
     * Load a world from a template directory. The world files
     * will be copied in a background thread. To get a reference
     * to the actual world, it is recommended to listen to
     * WorldLoadEvent.
     */
    public void loadWorld(final Game game, final File src, BukkitFuture<World> callback) {
        File dst;
        try {
            dst = Files.createTempDir("Minigames", game.getUuid().toString(), plugin.getServer().getWorldContainer());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            if (callback != null) {
                callback.runTask(plugin);
            }
            return;
        }
        fileMap.put(dst, game);
        new WorldLoadTask(plugin, game, src, dst, callback).start();
    }

    public Game getGameByWorld(World world) {
        return worldMap.get(world);
    }

    public void putWorld(World world, Game game) {
        worldMap.put(world, game);
        game.addWorld(world);
    }

    public void removeWorldLater(final World world) {
        new BukkitRunnable() {
            @Override public void run() {
                removeWorld(world);
            }
        }.runTaskLater(plugin, 100L);
    }

    private void removeWorld(World world) {
        for (Player player : world.getPlayers()) {
            player.kickPlayer("World expired");
        }
        if (!Bukkit.getServer().unloadWorld(world, false)) {
            plugin.getLogger().warning("Failed to unload world " + world.getName());
            return;
        }
        final Game game = worldMap.remove(world);
        if (game != null) game.removeWorld(world);
    }

    public void unregisterGame(Game game) {
        for (World world : game.getWorlds()) {
            removeWorldLater(world);
        }
    }

    public Game getGameByFile(File file) {
        return fileMap.get(file);
    }

    public void clear() {
        // for (World world : new ArrayList<World>(worldMap.keySet())) {
        //     removeWorld(world);
        // }
        worldMap.clear();
    }
}
