package com.winthier.minigames.event.player;

import com.winthier.minigames.game.Game;
import com.winthier.minigames.util.Msg;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeaveEvent extends Event {
    private static HandlerList handlers = new HandlerList();
    private final Game game;
    private final UUID uuid;
    private String leaveMessage = null;

    public PlayerLeaveEvent(Game game, UUID uuid) {
        this.game = game;
        this.uuid = uuid;
    }

    public PlayerLeaveEvent(Game game, Player player) {
        this(game, player.getUniqueId());
        leaveMessage = Msg.format("&8%s left", player.getName());
    }

    public UUID getUuid() {
        return uuid;
    }

    public Game getGame() {
        return game;
    }

    /**
     * @return The player if online, or null
     */
    public Player getPlayer() {
        return Bukkit.getServer().getPlayer(uuid);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getServer().getOfflinePlayer(uuid);
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
