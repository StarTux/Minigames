package com.winthier.minigames.util;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.game.Game;
import com.winthier.minigames.world.WorldLoadTask;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.World;

/**
 * Utility to load worlds sequentially, canceling the game if one
 * world doesn't load and calling a callback when done.
 */
public class WorldLoader {
    private final Game game;
    private final String[] names;
    private final Map<String, World> worlds = new HashMap<>();
    private final World[] worldArray;
    private final BukkitFuture<WorldLoader> callback;
    // Status
    private int index = 0;

    private WorldLoader(Game game, BukkitFuture<WorldLoader> callback, String... names) {
        this.game = game;
        this.callback = callback;
        this.names = names;
        this.worldArray = new World[names.length];
        callback.set(this);
    }

    private void fail(String name) {
        MinigamesPlugin.getInstance().getLogger().warning("Failed to load world `" + name + "'. Cancelling game " + game.getName() + ".");
        game.cancel();
    }

    private void next() {
        final int i = index++;
        if (i >= names.length) {
            callback.runTask(MinigamesPlugin.getInstance());
            return;
        }
        final String name = names[i];
        MinigamesPlugin.getWorldManager().loadWorld(game, name, new BukkitFuture<World>() {
            @Override public void run() {
                if (this.get() == null) {
                    fail(name);
                } else {
                    worlds.put(name, this.get());
                    worldArray[i] = this.get();
                    next();
                }
            }
        });
    }

    public static void loadWorlds(Game game, BukkitFuture<WorldLoader> callback, String... names) {
        WorldLoader loader = new WorldLoader(game, callback, names);
        loader.next();
    }

    public World getWorld(String name) {
        return worlds.get(name);
    }

    public World getWorld(int i) {
        return worldArray[i];
    }
}
