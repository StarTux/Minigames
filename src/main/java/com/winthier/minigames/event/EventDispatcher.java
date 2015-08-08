package com.winthier.minigames.event;

import com.winthier.minigames.game.Game;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listen for one specific event with one priority.
 */
public abstract class EventDispatcher implements Listener, EventExecutor {
    protected final Class<? extends Event> event;
    private final EventPriority priority;
    private final Map<Game, Collection<HandlerCaller>> gameMap = new WeakHashMap<Game, Collection<HandlerCaller>>();

    public EventDispatcher(Class<? extends Event> event, EventPriority priority) {
        this.event = event;
        this.priority = priority;
    }

    public void enable(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvent(event, this, priority, this, plugin);
    }

    public abstract Game findGame(Event event);

    @Override
    public void execute(Listener listener, Event event) {
        if (!this.event.isInstance(event)) return; // Happens sometimes, dunno why. :-/
        Game game = findGame(event);
        if (game == null) return;
        Collection<HandlerCaller> callers = gameMap.get(game);
        if (callers == null) return;
        for (HandlerCaller caller : callers) caller.call(event);
    }

    public void registerEvent(Game game, Listener listener, Method method, boolean ignoreCancelled) {
        Collection<HandlerCaller> callers = gameMap.get(game);
        if (callers == null) {
            callers = new ArrayList<HandlerCaller>();
            gameMap.put(game, callers);
        }
        callers.add(new HandlerCaller(event, listener, method, ignoreCancelled));
    }

    public void unregisterGame(Game game) {
        gameMap.remove(game);
    }

    public void clear() {
        gameMap.clear();
    }
}
