package com.winthier.minigames.event;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.event.player.PlayerLeaveEvent;
import com.winthier.minigames.game.Game;
import com.winthier.minigames.player.PlayerInfo;
import com.winthier.minigames.util.Msg;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class EventListener implements Listener {
    private final MinigamesPlugin plugin;

    public EventListener(MinigamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Set the join message
     * Make sure that a player who joins without being registered
     * for a game will be sent back. We could deny him to login in
     * the first place, but that may confuse BungeeCord to the
     * extend that a player can no longer join at all.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinLowest(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        final Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.setGameMode(GameMode.ADVENTURE);
        }
        final PlayerInfo info = MinigamesPlugin.getPlayerManager().getPlayerInfo(player);
        final Game game = info.getCurrentGame();
        if (game == null) {
            plugin.leavePlayer(player);
        } else {
            if (!info.hasJoinedBefore()) {
                if (game.getState() == Game.State.PLAY) {
                    game.onPlayerReady(event.getPlayer());
                }
                info.hasJoinedBefore(true);
            }
            game.announce("&f%s joined the game", player.getName());
        }
        final List<String> commandList = plugin.getConfig().getStringList("ForcedCommands");
        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isValid()) return;
                for (String cmd : commandList) {
                    player.performCommand(cmd);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event)
    {
        final Player player = event.getPlayer();
        final PlayerInfo info = MinigamesPlugin.getPlayerManager().getPlayerInfo(player);
        if (info.hasJoinedBefore()) return;
        final Game game = info.getCurrentGame();
        if (game == null) return;
        final Location loc = game.getSpawnLocation(player);
        if (loc == null) return;
        event.setSpawnLocation(loc);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if (event.getLeaveMessage() != null) {
            final Game game = event.getGame();
            if (game != null) {
                Player player = event.getPlayer();
                if (player != null) {
                    game.announce("&f%s left the game", player.getName());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final PlayerInfo info = MinigamesPlugin.getPlayerManager().getPlayerInfo(player);
        final Game game = info.getCurrentGame();
        if (event.getDeathMessage() != null && game != null) {
                game.announce(event.getDeathMessage());
        }
        event.setDeathMessage(null);
    }

    /**
     * Only players who are in the same game should see each
     * others' chat messages.
     */
    // @SuppressWarnings("deprecation") // Ignore PlayerChatEvent warnings
    // @EventHandler
    // public void onPlayerChat(org.bukkit.event.player.PlayerChatEvent event) {
    //     event.getRecipients().clear();
    //     Player sender = event.getPlayer();
    //     Game game = plugin.getPlayerManager().getCurrentGame(sender);
    //     if (game == null) return;
    //     for (Player recipient : game.getOnlinePlayers()) {
    //         event.getRecipients().add(recipient);
    //     }
    // }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final Game game = plugin.getPlayerManager().getCurrentGame(player);
        final String[] tokens = event.getMessage().split(" ");
        String command = tokens[0];
        if (command.startsWith("/")) command = command.substring(1, command.length());
        if (plugin.getConfig().getStringList("AllowedCommands").contains(command.toLowerCase())) return;
        final String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        boolean result = false;
        if ("opme".equalsIgnoreCase(command) && player.hasPermission("minigames.opme")) {
            boolean b = !player.isOp();
            player.setOp(b);
            event.setCancelled(true);
            if (b) {
                Msg.info(player, "Opped %s.", player.getName());
            } else {
                Msg.info(player, "De-opped %s.", player.getName());
            }
            return;
        }
        if (game != null) {
            try {
                result = game.onCommand(player, command, args);
            } catch (CommandException ce) {
                Msg.send(player, "&c%s", ce.getMessage());
                result = true;
            }
        }
        if (!result) {
            if ("leave".equalsIgnoreCase(command)) {
                MinigamesPlugin.leavePlayer(player);
                event.setCancelled(true);
            } else if (!player.isOp()) {
                Msg.send(player, "&cCommand not found: %s", command);
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }
}
