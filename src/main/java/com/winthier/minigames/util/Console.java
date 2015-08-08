package com.winthier.minigames.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

public class Console {
    public static boolean command(String cmd) {
        //System.out.println("Command: " + cmd);
        final CommandSender sender = Bukkit.getServer().getConsoleSender();
        try {
            return Bukkit.getServer().dispatchCommand(sender, cmd);
        } catch (CommandException ce) {
            System.err.println("Error executing command line: " + cmd);
            ce.printStackTrace();
            return true;
        }
    }
}
