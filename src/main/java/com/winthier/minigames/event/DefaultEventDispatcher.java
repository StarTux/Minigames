package com.winthier.minigames.event;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.game.Game;
import java.lang.reflect.Method;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

/**
 * Find the associated game using methods found via reflection.
 */
public class DefaultEventDispatcher extends EventDispatcher {
    // GameFinder has a couple of implementations, all of
    // which have the job to take the argument that is
    // returned from the method and use it to locate the game
    // associated with this event.
    private static abstract class GameFinder {
        public abstract Game findGame(Event event, Method method) throws Exception;
    }
    private static class GameGameFinder extends GameFinder {
        @Override public Game findGame(Event event, Method method) throws Exception {
            Game game = (Game)method.invoke(event);
            if (game == null) return null;
            return game;
        }
    }
    private static class WorldGameFinder extends GameFinder {
        @Override public Game findGame(Event event, Method method) throws Exception {
            World world = (World)method.invoke(event);
            if (world == null) return null;
            Game result = MinigamesPlugin.getInstance().getWorldManager().getGameByWorld(world);
            if (result == null) {
                result = MinigamesPlugin.getInstance().getWorldManager().getGameByFile(world.getWorldFolder());
            }
            return result;
        }
    }
    private static class PlayerGameFinder extends GameFinder {
        @Override public Game findGame(Event event, Method method) throws Exception {
            HumanEntity player = (HumanEntity)method.invoke(event);
            if (player == null) return null;
            return MinigamesPlugin.getInstance().getPlayerManager().getCurrentGame(player.getUniqueId());
        }
    }
    private static class EntityGameFinder extends GameFinder {
        @Override public Game findGame(Event event, Method method) throws Exception {
            Entity entity = (Entity)method.invoke(event);
            return MinigamesPlugin.getInstance().getWorldManager().getGameByWorld(entity.getWorld());
        }
    }
    private static class BlockGameFinder extends GameFinder {
        @Override public Game findGame(Event event, Method method) throws Exception {
            Block block = (Block)method.invoke(event);
            return MinigamesPlugin.getInstance().getWorldManager().getGameByWorld(block.getWorld());
        }
    }
    private static class InventoryViewGameFinder extends GameFinder {
        @Override public Game findGame(Event event, Method method) throws Exception {
            InventoryView view = (InventoryView)method.invoke(event);
            return MinigamesPlugin.getInstance().getPlayerManager().getCurrentGame(view.getPlayer().getUniqueId());
        }
    }
    private static class InventoryGameFinder extends GameFinder {
        @Override public Game findGame(Event event, Method method) throws Exception {
            Inventory inv = (Inventory)method.invoke(event);
            InventoryHolder holder = inv.getHolder();
            if (!(holder instanceof BlockState)) return null;
            return MinigamesPlugin.getInstance().getInstance().getWorldManager().getGameByWorld(((BlockState)holder).getWorld());
        }
    }

    private final Method method;
    private final GameFinder gameFinder;

    private DefaultEventDispatcher(Class<? extends Event> event, EventPriority priority, Method method, GameFinder gameFinder) {
        super(event, priority);
        this.method = method;
        this.gameFinder = gameFinder;
    }

    /**
     * Return a prepared EventDispatcher for the given event
     * if one was found.
     */
    public static DefaultEventDispatcher forEvent(Class<? extends Event> event, EventPriority priority) {
        Method method = null;
        GameFinder gameFinder = null;
        if (null != (method = getMethod(event, "getGame", Game.class))) {
            gameFinder = new GameGameFinder();
        } else if (null != (method = getMethod(event, "getPlayer", HumanEntity.class)) ||
                   null != (method = getMethod(event, "getEntity", HumanEntity.class)) ||
                   null != (method = getMethod(event, "getWhoClicked", HumanEntity.class)) ||
                   null != (method = getMethod(event, "getEnchanter", HumanEntity.class))) {
            gameFinder = new PlayerGameFinder();
        } else if (null != (method = getMethod(event, "getWorld", World.class))) {
            gameFinder = new WorldGameFinder();
        } else if (null != (method = getMethod(event, "getBlock", Block.class))) {
            gameFinder = new BlockGameFinder();
        } else if (null != (method = getMethod(event, "getPlayer", Entity.class)) ||
                   null != (method = getMethod(event, "getEntity", Entity.class)) ||
                   null != (method = getMethod(event, "getItem", Entity.class)) ||
                   null != (method = getMethod(event, "getVehicle", Entity.class)) ||
                   null != (method = getMethod(event, "getPainting", Entity.class))) {
            gameFinder = new EntityGameFinder();
        } else if (null != (method = getMethod(event, "getView", InventoryView.class))) {
            gameFinder = new InventoryViewGameFinder();
        } else if (null != (method = getMethod(event, "getInventory", Inventory.class))) {
            gameFinder = new InventoryGameFinder();
        } else if (null != (method = getMethod(event, "getSource", Inventory.class))) {
            // InventoryMoveEvent(?)
            gameFinder = new InventoryGameFinder();
        }
        MinigamesPlugin.getInstance().getLogger().info("DefaultEventDispatcher: Using " + gameFinder.getClass().getSimpleName() + " for " + event.getSimpleName());
        if (method == null || gameFinder == null) return null;
        return new DefaultEventDispatcher(event, priority, method, gameFinder);
    }

    @Override
    public Game findGame(Event event) {
        try {
            return gameFinder.findGame(event, method);
        } catch (Throwable t) {
            System.err.println("Event: " + this.event.getSimpleName() + ", Method: " + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "(), Got: " + event.getClass().getSimpleName());
            t.printStackTrace();
        }
        return null;
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?> returnType) {
        Method method;
        try {
            method = clazz.getMethod(name);
        } catch (NoSuchMethodException nsme) {
            return null;
        }
        if (!returnType.isAssignableFrom(method.getReturnType())) return null;
        return method;
    }
}
