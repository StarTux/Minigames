package com.winthier.minigames.util;

import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ParticleEffect {
    private final Effect effect;
    private final int id, data;
    private final float offsetX, offsetY, offsetZ;
    private final float speed;
    private final int particleCount;
    private final int radius;

    public ParticleEffect(Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ, float speed, int particleCount, int radius) {
        this.effect = effect;
        this.id = id;
        this.data = data;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.particleCount = particleCount;
        this.radius = radius;
    }

    public ParticleEffect(Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ, float speed, int particleCount) {
        this(effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, 16);
    }

    public ParticleEffect(Effect effect, float offsetX, float offsetY, float offsetZ, float speed, int particleCount) {
        this(effect, 0, 0, offsetX, offsetY, offsetZ, speed, particleCount, 16);
    }

    public void display(Player player, Location location) {
        player.spigot().playEffect(location, effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, radius);
    }

    public void display(Location location) {
        for (Player player : location.getWorld().getPlayers()) {
            display(player, location);
        }
    }

    public static ParticleEffect fromConfig(ConfigurationSection config) {
        final Effect effect = Enums.parseEnum(Effect.class, config.getString("Effect"), Effect.SMOKE);
        final int id = config.getInt("Id", 0);
        final int data = config.getInt("Data", 0);
        final List<Float> offset = config.getFloatList("Offset");
        final float offsetX = offset.size() > 0 ? offset.get(0) : 0.5f;
        final float offsetY = offset.size() > 1 ? offset.get(1) : 0.5f;
        final float offsetZ = offset.size() > 2 ? offset.get(2) : 0.5f;
        final float speed = (float)config.getDouble("Speed", 0.1);
        final int particleCount = config.getInt("ParticleCount", 1);
        final int radius = config.getInt("Radius", 16);
        return new ParticleEffect(effect, id,  data, offsetX, offsetY, offsetZ, speed, particleCount, radius);
    }
}
