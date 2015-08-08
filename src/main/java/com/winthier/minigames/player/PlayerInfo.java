package com.winthier.minigames.player;

import com.winthier.minigames.game.Game;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerInfo {
    private final UUID uuid;
    private Game currentGame = null;
    private boolean hasJoinedBefore = false;
    private Object customData = null;

    public PlayerInfo(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerInfo(OfflinePlayer player) {
        this(player.getUniqueId());
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getServer().getPlayer(uuid);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public boolean hasJoinedBefore() {
        return hasJoinedBefore;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T getCustomData(Class<? extends T> clazz) {
        if (currentGame == null) throw new NullPointerException("Cannot supply custom data for player who is not in a game.");
        if (customData == null) {
            try {
                customData = clazz.getConstructor(currentGame.getClass(), UUID.class).newInstance(currentGame, uuid);
            } catch (Exception e) {
                throw new IllegalArgumentException("Custom data class " + clazz.getName() + " lacks required constructor (" + currentGame.getClass().getSimpleName() + ", UUID)", e);
            }
        } else if (clazz.isInstance(customData)) {
            //
        } else {
            throw new ClassCastException("Bad custom data class. Expected: " + clazz.getName() + ". Found: " + customData.getClass().getName());
        }
        return (T)customData; // unchecked, kind of
    }

    public void hasJoinedBefore(boolean hasJoinedBefore) {
        this.hasJoinedBefore = hasJoinedBefore;
    }

    public void setCurrentGame(Game game) {
        // Remove player from previos game, if any.
        final Game old = getCurrentGame();
        if (old != null) old.removePlayer(this);
        // Add them to the new game and save it in their info.
        if (game == null) {
            currentGame = null;
        } else {
            currentGame = game;
            game.addPlayer(this);
        }
        hasJoinedBefore = false;
        customData = null;
    }
}
