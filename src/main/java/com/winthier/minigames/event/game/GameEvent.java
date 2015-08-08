package com.winthier.minigames.event.game;

import com.winthier.minigames.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEvent extends Event {
    private static HandlerList handlers = new HandlerList();
    protected final Game game;

    public GameEvent(Game game) {
        this.game = game;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Game getGame() {
        return game;
    }
}
