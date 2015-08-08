package com.winthier.minigames.event;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.event.player.PlayerLeaveEvent;
import com.winthier.minigames.game.Game;
import com.winthier.minigames.player.PlayerInfo;
import com.winthier.minigames.util.Msg;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
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

// import com.comphenix.protocol.PacketType;
// import com.comphenix.protocol.ProtocolLibrary;
// import com.comphenix.protocol.events.ListenerOptions;
// import com.comphenix.protocol.events.ListenerPriority;
// import com.comphenix.protocol.events.PacketAdapter;
// import com.comphenix.protocol.events.PacketEvent;

public class EventListener implements Listener {
    private final MinigamesPlugin plugin;

    public EventListener(MinigamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // try {
        //     enableProtocol();
        // } catch (Throwable t) {
        //     t.printStackTrace();
        // }
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
        final PlayerInfo info = MinigamesPlugin.getPlayerManager().getPlayerInfo(player);
        final Game game = info.getCurrentGame();
        if (game == null) {
            plugin.leavePlayer(player);
            //     event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your game could not be found.");
            //     return;
            // }
            // if (!game.getAcceptPlayerJoins()) {
            //     event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your game is not ready yet.");
            //     return;
        } else {
            if (!info.hasJoinedBefore()) {
                if (game.getState() == Game.State.PLAY) {
                    game.onPlayerReady(event.getPlayer());
                }
                info.hasJoinedBefore(true);
            }
            game.announce("&f%sJoined the game", player.getName());
        }
        final List<String> commandList = plugin.getConfig().getStringList("ForcedCommands");
        new BukkitRunnable() {
            @Override public void run() {
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
                game.announce(event.getLeaveMessage());
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

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // ProtocolLib
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    // @SuppressWarnings("deprecation")
    // public void enableProtocol() {
    //     PacketAdapter.AdapterParameteters params;
    //     params = PacketAdapter
    //         .params(plugin, PacketType.Play.Server.PLAYER_INFO)
    //         .serverSide()
    //         .listenerPriority(ListenerPriority.HIGHEST)
    //         //.options(ListenerOptions.ASYNC)
    //         ;
    //     PacketAdapter adapter;
    //     adapter = new PacketAdapter(params) {
    //         @Override
    //         public void onPacketSending(PacketEvent event) {
    //             // If the player goes offline, allow the packet
    //             final boolean online = event.getPacket().getBooleans().read(0);
    //             if (!online) return;
    //             final Player recipient = event.getPlayer();
    //             final String name = event.getPacket().getStrings().read(0);
    //             final Player player = Bukkit.getServer().getPlayer(name);
    //             if (recipient != null && player != null) {
    //                 // Allow sending players their own online packets
    //                 if (recipient.equals(player)) return;
    //                 // Allow sending packets to players in the same game
    //                 final Game game1 = MinigamesPlugin.getPlayerManager().getCurrentGame(recipient);
    //                 final Game game2 = MinigamesPlugin.getPlayerManager().getCurrentGame(player);
    //                 if (game1 == game2) return;
    //             }
    //             event.setCancelled(true);
    //         }
    //     };
    //     ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
    // }
}
