package com.winthier.minigames.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.game.Game;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Hearts {
    private final int CHARACTER_LIMIT = 32;
    private final double range = 32;
    private final long duration = 60L;
    private final String FULL_HEART = "\u2764";
    private final String EMPTY_HEART = "\u2764";
    private final String FULL_PREFIX = ChatColor.RED.toString();
    private final String EMPTY_PREFIX = ChatColor.BLACK.toString();

    private PacketConstructor entityMetadataCtor = null;
    private BukkitRunnable task;
    private long ticks = 0;
    private final Set<LivingEntity> showHealthList = new HashSet<>();
    private final Map<LivingEntity, Long> restoreNameMap = new HashMap<>();
    private final List<LivingEntity> restoreNameCache = new ArrayList<>();

    public void enable() {
        // Start onTick task
        task = new BukkitRunnable() {
            @Override public void run() {
                onTick();
            }
        };
        task.runTaskTimer(MinigamesPlugin.getInstance(), 1L, 1L);
    }

    public void disable() {
        task.cancel();
        task = null;
        showHealthList.clear();
        restoreNameMap.clear();
        restoreNameCache.clear();
    }

    private void onTick() {
        if (!restoreNameMap.isEmpty()) {
            for (Map.Entry<LivingEntity, Long> entry : restoreNameMap.entrySet()) {
                final long time = entry.getValue();
                if (time < ticks) {
                    final LivingEntity entity = entry.getKey();
                    restoreNameCache.add(entity);
                }
            }
        }
        if (!restoreNameCache.isEmpty()) {
            for (LivingEntity entity : restoreNameCache) {
                updateName(entity, false);
                restoreNameMap.remove(entity);
            }
            restoreNameCache.clear();
        }
        if (!showHealthList.isEmpty()) {
            for (LivingEntity entity : showHealthList) {
                updateName(entity, true);
                restoreNameMap.put(entity, ticks + duration);
            }
            showHealthList.clear();
        }
        ticks +=1 ;
    }

    public boolean isNearby(Location a, Location b) {
        if (!a.getWorld().equals(b.getWorld())) return false;
        final int dx = Math.abs(a.getBlockX() - b.getBlockX());
        if (dx > range) return false;
        final int dz = Math.abs(a.getBlockZ() - b.getBlockZ());
        if (dz > range) return false;
        return true;
    }

    public String createHeartName(LivingEntity entity) {
        final StringBuilder sb = new StringBuilder();

        int health = (int)Math.ceil(entity.getHealth() / 2.0);
        int max = (int)Math.ceil(entity.getMaxHealth() / 2.0);

        // Build new name
        int i = 0;
        sb.append(FULL_PREFIX);
        for (; i < health; i += 1) sb.append(FULL_HEART);
        sb.append(EMPTY_PREFIX);
        for (; i < max; i += 1) sb.append(EMPTY_HEART);
        String name = sb.toString();
        if (name.length() > CHARACTER_LIMIT) name = name.substring(0, CHARACTER_LIMIT);
        return name;
    }

    private PacketContainer createEntityMetadataPacket(int i, WrappedDataWatcher dataWatcher, boolean flag) {
        if (entityMetadataCtor == null) {
            entityMetadataCtor = ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, i, dataWatcher.getHandle(), flag);
        }
        return entityMetadataCtor.createPacket(i, dataWatcher.getHandle(), flag);
    }
    
    private PacketContainer createHeartsPacket(LivingEntity entity, boolean showHearts) {
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        String name = null;
        boolean visible = false;
        if (showHearts) {
            name = createHeartName(entity);
            visible = true;
        } else {
            name = entity.getCustomName();
            visible = entity.isCustomNameVisible();
        }
        if (name != null) {
            watcher.setObject(2, name);
            watcher.setObject(3, (visible ? (byte)1 : (byte)0));
        }
        PacketContainer packet = createEntityMetadataPacket(entity.getEntityId(), watcher, true);
        return packet;
    }

    private void updateName(LivingEntity entity, boolean showHearts) {
        // Send to damager, if any.
        PacketContainer packet = null;
        // Send to nearby players.
        final Location entityLocation = entity.getLocation();
        for (Player player : entity.getWorld().getPlayers()) {
            if (!isNearby(entityLocation, player.getLocation())) continue;
            if (packet == null) packet = createHeartsPacket(entity, showHearts);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
            }
        }
    }

    private LivingEntity checkEntity(Entity e) {
        switch (e.getType()) {
        case ENDER_DRAGON:
        case WITHER:
            return null;
        }
        if (!(e instanceof LivingEntity)) return null;
        LivingEntity living = (LivingEntity)e;
        //if (living.getCustomName() != null) return null;
        return living;
    }

    public void showHealth(Game game, LivingEntity entity) {
        showHealthList.add(entity);
    }

    public void showHealthLater(final Game game, final LivingEntity entity) {
        showHealth(game, entity);
    }
}
