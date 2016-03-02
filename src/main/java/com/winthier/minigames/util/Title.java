package com.winthier.minigames.util;

import com.winthier.minigames.MinigamesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

public class Title {
    public static void show(Player player, String title, String subtitle) {
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle));
    }
}
