package com.winthier.minigames.util;

import com.winthier.minigames.MinigamesPlugin;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class Metadata {
    public static void set(Metadatable table, String key, Object value) {
        table.setMetadata(key, new FixedMetadataValue(MinigamesPlugin.getInstance(), value));
    }

    public static Object get(Metadatable table, String key) {
        for (MetadataValue value : table.getMetadata(key)) {
            if (value.getOwningPlugin().equals(MinigamesPlugin.getInstance())) {
                return value.value();
            }
        }
        return null;
    }
}
