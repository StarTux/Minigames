package com.winthier.minigames.util;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SoundEffect {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundEffect(Sound sound) {
        this(sound, 1.0f, 1.0f);
    }

    public void play(Player player, Location location) {
        player.playSound(location, sound, volume, pitch);
    }

    public void play(Location location) {
        for (Player player : location.getWorld().getPlayers()) {
            play(player, location);
        }
    }

    public static SoundEffect fromConfig(ConfigurationSection config) {
        final Sound sound = Enums.parseEnum(Sound.class, config.getString("Sound"), Sound.BLOCK_LEVER_CLICK);
        final float volume = (float)config.getDouble("Volume", 1.0);
        final float pitch = (float)config.getDouble("Pitch", 1.0);
        return new SoundEffect(sound, volume, pitch);
    }
}
