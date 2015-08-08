package com.winthier.minigames.util;

import com.winthier.minigames.MinigamesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

public class Title {
    public static void showRaw(Player player, Object jsonTitle, Object jsonSubtitle) {
        String title, subtitle;
        try {
            title = JSONValue.toJSONString(jsonTitle);
        } catch (Exception e) {
            MinigamesPlugin.getInstance().getLogger().warning("Error printing JSON structure: Title");
            e.printStackTrace();
            return;
        }
        try {
            subtitle = JSONValue.toJSONString(jsonSubtitle);
        } catch (Exception e) {
            MinigamesPlugin.getInstance().getLogger().warning("Error printing JSON structure: Subtitle");
            e.printStackTrace();
            return;
        }
        Console.command("title " + player.getName() + " subtitle " + subtitle);
        Console.command("title " + player.getName() + " title " + title);
    }

    public static void show(Player player, String title, String subtitle) {
        showRaw(player, ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle));
    }
}
