package com.winthier.minigames;

import com.winthier.minigames.event.EventListener;
import com.winthier.minigames.event.EventManager;
import com.winthier.minigames.event.player.PlayerLeaveEvent;
import com.winthier.minigames.game.Game;
import com.winthier.minigames.game.GameManager;
import com.winthier.minigames.player.PlayerManager;
import com.winthier.minigames.util.Hearts;
import com.winthier.minigames.world.WorldManager;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class MinigamesPlugin extends JavaPlugin {
    private static MinigamesPlugin instance = null;

    // Managers
    private final GameManager gameManager = new GameManager(this);
    private final EventManager eventManager = new EventManager(this);
    private final WorldManager worldManager = new WorldManager(this);
    private final PlayerManager playerManager = new PlayerManager(this);
    private final EventListener eventListener = new EventListener(this);
    // Cache
    private Hearts hearts = null;

    @Override
    public void onEnable() {
        instance = this;
        eventListener.onEnable();
        gameManager.onEnable();
        saveDefaultConfig();
        reloadConfig();
        //loadConfiguration();
    }

    @Override
    public void onDisable() {
        playerManager.clear();
        worldManager.clear();
        eventManager.clear();
        gameManager.clear();
        if (hearts != null) hearts.disable();
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        return false;
    }

    public static Game createGame(String gameName) {
        final Game game = instance.gameManager.createGame(gameName);
        // From here on, the game is registered and has to be
        // unregistered with the GameManager in case something
        // fails.
        new BukkitRunnable() {
            // Enable the game on the next tick because the caller
            // of this might make modifications such load a
            // configuration or as adding players before the game
            // itself does any checks.
            @Override public void run() {
                game.enable();
            }
        }.runTaskLater(instance, 2L);
        return game;
    }

    public boolean addPlayer(Game game, UUID uuid) {
        if (playerManager.getCurrentGame(uuid) != null) {
            getLogger().warning("Tried to add player " + uuid + " to game " + game.getUuid() + " but they were already in game " + playerManager.getCurrentGame(uuid).getUuid());
            return false;
        } else {
            playerManager.setCurrentGame(uuid, game);
            return true;
        }
    }

    /**
     * Call this whenever a player expressed a desire to leave
     * their game or should be removed for another reason.
     */
    public static void leavePlayer(Player player) {
        // Call event - prior to making any changes
        Game game = instance.playerManager.getCurrentGame(player);
        PlayerLeaveEvent event = new PlayerLeaveEvent(game, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        // Unset current game
        instance.playerManager.setCurrentGame(player, null);
    }

    public static void leavePlayer(UUID uuid){ 
        final Player player = Bukkit.getServer().getPlayer(uuid);
        if (player != null) {
            leavePlayer(player);
        } else {
            // Call event - prior to making any changes
            Game game = instance.playerManager.getCurrentGame(uuid);
            PlayerLeaveEvent event = new PlayerLeaveEvent(game, uuid);
            Bukkit.getServer().getPluginManager().callEvent(event);
            // Unset current game
            instance.playerManager.setCurrentGame(uuid, null);
        }
    }

    /**
     * Get the top folder which contains all template worlds and
     * configurations.
     */
    public static File getTemplateFolder() {
        return new File("template");
    }

    public static File getGamesFolder() {
        return new File("games");
    }

    public static File getConfigFolder() {
        return new File("config");
    }

    public static MinigamesPlugin getInstance() {
        return instance;
    }

    public static EventManager getEventManager() {
        return instance.eventManager;
    }

    public static WorldManager getWorldManager() {
        return instance.worldManager;
    }

    public static PlayerManager getPlayerManager() {
        return instance.playerManager;
    }

    public static GameManager getGameManager() {
        return instance.gameManager;
    }

    public static Hearts getHearts() {
        if (instance.hearts == null) {
            instance.hearts = new Hearts();
            instance.hearts.enable();
        }
        return instance.hearts;
    }

    public void unregisterGame(Game game) {
        eventManager.unregisterGame(game);
        worldManager.unregisterGame(game);
        playerManager.unregisterGame(game);
        gameManager.unregisterGame(game);
    }

    public void clear() {
        eventManager.clear();
        // worldManager.clear();
    }
}
