package com.winthier.minigames.event.player;

import com.winthier.minigames.game.Game;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerReadyEvent extends Event {
    private static HandlerList handlers = new HandlerList();
    private final Game game;
    private final UUID uuid;

    public PlayerReadyEvent(Game game, UUID uuid) {
        this.game = game;
        this.uuid = uuid;
    }

    public PlayerReadyEvent(Game game, Player player) {
        this(game, player.getUniqueId());
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
