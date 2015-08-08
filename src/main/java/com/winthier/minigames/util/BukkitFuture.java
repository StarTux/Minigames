package com.winthier.minigames.util;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class BukkitFuture<V> extends BukkitRunnable {
    private V value;

    public V get() {
        return value;
    }

    public void set(V value) {
        this.value = value;
    }
}
