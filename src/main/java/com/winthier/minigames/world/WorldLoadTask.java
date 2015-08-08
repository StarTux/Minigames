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
import org.bukkit.scheduler.BukkitRunnable;

public class WorldLoadTask {
    private final MinigamesPlugin plugin;
    private final Game game;
    private final File src;
    private final File dst;
    private final BukkitFuture<World> callback;
    private final World.Environment environment;

    public WorldLoadTask(MinigamesPlugin plugin, Game game, File src, File dst, BukkitFuture<World> callback) {
        this.plugin = plugin;
        this.game = game;
        this.src = src;
        this.dst = dst;
        this.callback = callback;
        if (src.getName().endsWith("_nether")) {
            this.environment = World.Environment.NETHER;
        } else if (src.getName().endsWith("_the_end")) {
            this.environment = World.Environment.THE_END;
        } else {
            this.environment = World.Environment.NORMAL;
        }
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
