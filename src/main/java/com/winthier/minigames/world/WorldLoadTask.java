package com.winthier.minigames.world;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.game.Game;
import com.winthier.minigames.util.BukkitFuture;
import com.winthier.minigames.util.Files;
import java.io.File;
import java.io.IOException;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldLoadTask {
    MinigamesPlugin plugin;
    Game game;
    File src;
    File dst;
    BukkitFuture<World> callback;
    World.Environment environment = World.Environment.NORMAL;

    public WorldLoadTask(MinigamesPlugin plugin, Game game, File src, File dst, BukkitFuture<World> callback) {
        this.plugin = plugin;
        this.game = game;
        this.src = src;
        this.dst = dst;
        this.callback = callback;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(src, "config.yml"));
        try {
            String tmp  = config.getString("world.Environment");
            if (tmp != null) {
                this.environment = World.Environment.valueOf(tmp.toUpperCase());
            } else {
                if (src.getName().endsWith("_nether")) {
                    this.environment = World.Environment.NETHER;
                } else if (src.getName().endsWith("_the_end")) {
                    this.environment = World.Environment.THE_END;
                } else {
                    this.environment = World.Environment.NORMAL;
                }
            }
        } catch (IllegalArgumentException iae) {}
    }

    public void start() {
        new BukkitRunnable() { public void run() { copyWorld(); } }.runTaskAsynchronously(plugin);
    }

    /**
     * Ran async
     */
    private void copyWorld() {
        try {
            Files.copyDir(src, dst);
            new File(dst, "session.lock").delete();
            new File(dst, "uid.dat").delete();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            callback(null);
            return;
        }
        new BukkitRunnable() { public void run() { loadWorld(); } }.runTask(plugin);
    }

    private void loadWorld() {
        final String name = dst.getName();
        final WorldCreator creator = new WorldCreator(name);
        creator.environment(this.environment);
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);
        creator.generator(new NullGenerator());
        final World world = plugin.getServer().createWorld(creator);
        plugin.getWorldManager().putWorld(world, game);
        // Callback
        callback(world);
    }

    private void callback(World world) {
        if (callback != null) {
            callback.set(world);
            callback.runTask(plugin);
        }
    }
}
