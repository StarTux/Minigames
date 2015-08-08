package com.winthier.minigames.event.game;

import com.winthier.minigames.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a game changes one of its properties:
 * - Player count: getPlayerCount()
 * - (Reserved for future data)
 * 
 * Call this event when anything about a game that needs to be
 * known to the outside, other than the state, has changed.
 */
public class GameChangeEvent extends GameEvent {
    private static HandlerList handlers = new HandlerList();
    
    public GameChangeEvent(Game game) {
        super(game);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
