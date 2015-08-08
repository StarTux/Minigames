package com.winthier.minigames.player;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.game.Game;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerManager {
    private final MinigamesPlugin plugin;
    private final Map<UUID, PlayerInfo> uuidMap = new HashMap<>();

    public PlayerManager(MinigamesPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerInfo getPlayerInfo(UUID uuid) {
        PlayerInfo result = uuidMap.get(uuid);
        if (result == null) {
            result = new PlayerInfo(uuid);
            uuidMap.put(uuid, result);
        }
        return result;
    }

    public PlayerInfo getPlayerInfo(OfflinePlayer player) {
        return getPlayerInfo(player.getUniqueId());
    }

    public Game getCurrentGame(UUID uuid) {
        final PlayerInfo info = uuidMap.get(uuid);
        if (info == null) return null;
        return info.getCurrentGame();
    }

    public Game getCurrentGame(OfflinePlayer player) {
        return getCurrentGame(player.getUniqueId());
    }

    public void setCurrentGame(UUID uuid, Game game) {
        getPlayerInfo(uuid).setCurrentGame(game);
    }

    public void setCurrentGame(OfflinePlayer player, Game game) {
        setCurrentGame(player.getUniqueId(), game);
    }

    public void unregisterGame(Game game) {
        for (PlayerInfo player : game.getPlayers()) {
            // PlayerInfo will remove player from game.
            player.setCurrentGame(null);
        }
    }

    public void clear() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.kickPlayer("Server shutting down");
        }
        for (PlayerInfo info : uuidMap.values()) {
            info.setCurrentGame(null);
        }
        uuidMap.clear();
    }
}
