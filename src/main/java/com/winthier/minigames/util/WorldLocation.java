package com.winthier.minigames.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * Utility class that stores location information except for the
 * world and comes with some handy methods to turn it into a
 * Bukkit location.
 *
 * When a game wants to store a world, storing it with a world
 * name makes no sense because worlds are generated with
 * unpredictable names. This object can be serialized and stored
 * in the world or game config and then turned into a real
 * location once the instance is running and worlds are loaded.
 *
 * Other utilities include:
 * - Immutable
 * - Serialize to String
 * - Load from String
 * - Teleport player within the same world
 * - Pitch and yaw are optional
 * - Teleport entities, maintaining pitch and yaw if not set here.
 */
public class WorldLocation {
    private final Vector vector;
    private final Float yaw, pitch;

    public WorldLocation(Vector vector, Float yaw, Float pitch) {
        this.vector = vector;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public WorldLocation(Vector vector) {
        this(vector, null, null);
    }

    public WorldLocation(double x, double y, double z) {
        this(new Vector(x, y, z));
    }

    public WorldLocation(double x, double y, double z, Float yaw, Float pitch) {
        this(new Vector(x, y, z), yaw, pitch);
    }

    public float getYaw() {
        return yaw == null ? 0.0f : yaw;
    }

    public WorldLocation add(double x, double y, double z) {
        return new WorldLocation(vector.getX() + x, vector.getY() + y, vector.getZ() + z, yaw, pitch);
    }

    public float getPitch() {
        return pitch == null ? 0.0f : pitch;
    }

    public WorldLocation stripView() {
        return new WorldLocation(vector);
    }

    public Location toLocation(World world) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ(), getYaw(), getPitch());
    }

    public Location toLocation(Entity entity) {
        Location old = entity.getLocation();
        float yaw = this.yaw == null ? old.getYaw() : this.yaw;
        float pitch = this.pitch == null ? old.getPitch() : this.pitch;
        return new Location(old.getWorld(), vector.getX(), vector.getY(), vector.getZ(), yaw, pitch);
    }

    public void teleport(Entity entity) {
        entity.teleport(toLocation(entity));
    }

    /**
     * For serialization
     */
    public String toString() {
        NumberFormat df = DecimalFormat.getInstance();
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(4);
        df.setRoundingMode(RoundingMode.FLOOR);
        StringBuilder sb = new StringBuilder()
            .append(df.format(vector.getX())).append(",")
            .append(df.format(vector.getY())).append(",")
            .append(df.format(vector.getZ()));
        if (yaw != null && pitch != null) {
            sb
                .append(",").append(df.format(yaw))
                .append(",").append(df.format(pitch));
        }
        return sb.toString();
    }

    public static WorldLocation fromString(String string) {
        String[] tokens = string.split(",");
        if (tokens.length < 3) return null;
        try {
            double x = Double.parseDouble(tokens[0]);
            double y = Double.parseDouble(tokens[1]);
            double z = Double.parseDouble(tokens[2]);
            if (tokens.length == 3) {
                return new WorldLocation(x, y, z);
            } else if (tokens.length == 5) {
                float yaw = Float.parseFloat(tokens[3]);
                float pitch = Float.parseFloat(tokens[4]);
                return new WorldLocation(x, y, z, yaw, pitch);
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return null;
    }

    public static WorldLocation fromLocation(Location location) {
        return new WorldLocation(location.toVector(), location.getYaw(), location.getPitch());
    }

    public void saveToConfig(ConfigurationSection config) {
        config.set("Vector", vector);
        if (yaw != null) config.set("Yaw", yaw);
        if (pitch != null) config.set("Pitch", pitch);
    }

    public static WorldLocation loadFromConfig(ConfigurationSection config) {
        Vector vector = (Vector)config.get("Vector");
        Float yaw = null, pitch = null;
        if (config.isSet("Yaw")) yaw = (float)config.getDouble("Yaw");
        if (config.isSet("Pitch")) pitch = (float)config.getDouble("Pitch");
        return new WorldLocation(vector, yaw, pitch);
    }
}
