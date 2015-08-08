package com.winthier.minigames.event.game;

import com.winthier.minigames.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a game changes state.
 */
public class GameStateEvent extends GameEvent {
    private static HandlerList handlers = new HandlerList();
    private final Game.State state;
    
    public GameStateEvent(Game game, Game.State state) {
        super(game);
        this.state = state;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Game.State getState() {
        return state;
    }

    public Game.State getPreviousState() {
        return game.getState();
    }
}
