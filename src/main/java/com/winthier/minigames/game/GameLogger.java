package com.winthier.minigames.game;

import com.winthier.minigames.MinigamesPlugin;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Adapted from org.bukkit.plugin.PluginLogger, licensed under GPLv3
 */
public class GameLogger extends Logger {
    private final String prefix;

    public GameLogger(Game game) {
        super(game.getName(), null);
        setParent(MinigamesPlugin.getInstance().getLogger());
        setLevel(Level.ALL);
        prefix = "[" + game.getName() + " #" + game.getId() + "] ";
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage(prefix + record.getMessage());
        super.log(record);
    }
}
