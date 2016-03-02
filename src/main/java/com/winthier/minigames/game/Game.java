package com.winthier.minigames.game;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.event.game.GameChangeEvent;
import com.winthier.minigames.event.game.GameStateEvent;
import com.winthier.minigames.player.PlayerInfo;
import com.winthier.minigames.util.Console;
import com.winthier.minigames.util.Msg;
import com.winthier.minigames.util.Players;
import com.winthier.minigames.util.Title;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONValue;

/**
 * One instance of a game. Arbitrary amounts of instances can be
 * created and destroyed sporadically.
 */
public abstract class Game {
    private final Map<UUID, PlayerInfo> players = new HashMap<>();
    private final List<World> worlds = new ArrayList<>();
    private State state = State.INIT;
    private Logger logger = null;
    private UUID uuid;
    private final int id;
    private static int nextId = 0;
    private String name;
    private ConfigurationSection config;
    private Map<String, YamlConfiguration> configFiles = new HashMap<>();
    private Map<String, YamlConfiguration> worldConfigs = new HashMap<>();
    private int playerCount;
    private int maxPlayers = 16;

    public Game() {
        uuid = UUID.randomUUID();
        this.id = nextId++;
    }

    public void configure(ConfigurationSection config) {
        this.config = config;
    }

    public final void enable() {
        getLogger().info("Enabling");
        try {
            onEnable();
        } catch (Throwable e) {
            e.printStackTrace();
            disable();
        }
        getLogger().info("Enabled");
    }

    /**
     * Cancel this game. Games might call this several times
     * recursively if they are not careful, so make sure nothing
     * bad can happen.
     */
    private boolean cancelled = false;
    public void cancel() {
        if (cancelled) return;
        cancelled = true;
        setState(State.OVER);
    }

    public void ready() {
        setState(State.PLAY);
    }

    private final void disable() {
        onDisable();
        // Send players home
        for (PlayerInfo info : getPlayers()) {
            MinigamesPlugin.leavePlayer(info.getUuid());
        }
        MinigamesPlugin.getInstance().unregisterGame(this);
        players.clear();
        worlds.clear();
        getLogger().info("Disabled");
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getId() {
        return id;
    }

    public String getName() { return name; }
    /**
     * Called by GameManager.createGame().
     */
    public void setName(String name) { this.name = name; }

    public final List<Player> getOnlinePlayers() {
        List<Player> result = new ArrayList<Player>();
        for (PlayerInfo info : players.values()) {
            Player player = info.getPlayer();
            if (player != null) result.add(player);
        }
        return result;
    }

    public final List<PlayerInfo> getPlayers() {
        return new ArrayList<PlayerInfo>(players.values());
    }

    public final List<UUID> getPlayerUuids() {
        List<UUID> result = new ArrayList<>();
        result.addAll(players.keySet());
        return result;
    }

    public final PlayerInfo getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public final List<World> getWorlds() {
        return new ArrayList<World>(worlds);
    }

    public final void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public final int getMaxPlayers() {
        return maxPlayers;
    }

    public final int getPlayerCount() {
        return playerCount;
    }

    public <T> T getPlayerData(Class<T> clazz, UUID uuid) {
        PlayerInfo info = getPlayer(uuid);
        if (info == null) throw new IllegalArgumentException("Player is not in this game: " + uuid);
        return info.getCustomData(clazz);
    }

    public <T> T getPlayerData(Class<T> clazz, Player player) {
        PlayerInfo info = getPlayer(player.getUniqueId());
        if (info == null) throw new IllegalArgumentException("Player is not in this game: " + player.getName());
        return info.getCustomData(clazz);
    }

    /**
     * Allow players to join after initial setup.  Override this
     * to control joining.
     * This may only return true if all players can and have been
     * added.  Use MinigamesPlugin.addPlayer() to add them to this
     * game.
     * Any override is encouraged to do its own checks and then
     * call this default implementation to do the standard checks
     * and actions.
     */
    public boolean joinPlayers(List<UUID> uuids) {
        if (playerCount + uuids.size() >= maxPlayers) return false;
        for (UUID uuid : uuids) {
            if (MinigamesPlugin.getPlayerManager().getCurrentGame(uuid) != null) return false;
        }
        for (UUID uuid : uuids) {
            MinigamesPlugin.getInstance().addPlayer(this, uuid);
        }
        return true;
    }

    public boolean joinSpectators(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            if (MinigamesPlugin.getPlayerManager().getCurrentGame(uuid) != null) return false;
        }
        for (UUID uuid : uuids) {
            MinigamesPlugin.getInstance().addPlayer(this, uuid);
        }
        return true;
    }

    /**
     * Internal use only.
     */
    public final void addPlayer(PlayerInfo player) {
        players.put(player.getUuid(), player);
    }

    /**
     * Internal use only.
     */
    public final void removePlayer(PlayerInfo player) {
        players.remove(player.getUuid());
    }

    /**
     * Internal use only.
     */
    public final void addWorld(World world) {
        worlds.add(world);
    }

    /**
     * Internal use only.
     */
    public final void removeWorld(World world) {
        worlds.remove(world.getName());
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = new GameLogger(this);
        }
        return logger;
    }

    public State getState() {
        return state;
    }

    /**
     * A game must set the state to PLAY when it wants players to
     * join and OVER when it's over.
     */
    protected final void setState(State state) {
        // Call the event
        GameStateEvent event = new GameStateEvent(this, state);
        Bukkit.getServer().getPluginManager().callEvent(event);
        // Call hooks
        switch (state) {
        case PLAY:
            for (PlayerInfo info : getPlayers()) {
                Player player = info.getPlayer();
                if (player != null) {
                    info.hasJoinedBefore(true);
                    Location loc = getSpawnLocation(player);
                    if (loc == null && !worlds.isEmpty()) loc = worlds.get(0).getSpawnLocation();
                    if (loc != null) player.teleport(loc);
                    Players.reset(player);
                    onPlayerReady(player);
                }
            }
            break;
        case OVER:
            disable();
            break;
        }
        // Set the state
        this.state = state;
    }

    protected void callGameChangeEvent() {
        GameChangeEvent event = new GameChangeEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    /**
     * A game should set this to communicate how many of the
     * registered players are actually playing the game, rather
     * than just spectating.
     */
    protected void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public void announce(String message, Object... args) {
        String msg = Msg.format(message, args);
        getLogger().info(msg);
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public void announceRaw(Object json) {
        String js;
        try {
            js = JSONValue.toJSONString(json);
            //System.out.println("JS: " + js);
        } catch (Exception e) {
            getLogger().warning("Error printing JSON structure");
            e.printStackTrace();
            return;
        }
        for (Player player : getOnlinePlayers()) {
            Console.command("tellraw " + player.getName() + " " + js);
        }
    }

    public void announceTitle(String title, String subtitle) {
        for (Player player : getOnlinePlayers()) {
            Title.show(player, Msg.format(title), Msg.format(subtitle));
        }
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public File getConfigFolder() {
        return new File(MinigamesPlugin.getConfigFolder(), getName());
    }

    public ConfigurationSection getConfigFile(String name) {
        YamlConfiguration result = configFiles.get(name);
        if (result == null) {
            result = new YamlConfiguration();
            File file = new File(getConfigFolder(), name + ".yml");
            try {
                result.load(file);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InvalidConfigurationException ice) {
                ice.printStackTrace();
            }
        }
        return result;
    }

    public ConfigurationSection getWorldConfig(World world, String name) {
        final String key = world.getName() + "/" + name;
        YamlConfiguration result = worldConfigs.get(key);
        if (result == null) {
            try {
                File file;
                file = new File(world.getWorldFolder(), "config");
                file = new File(file, getName());
                file = new File(file, name + ".yml");
                result = YamlConfiguration.loadConfiguration(file);
            } catch (Exception e) {
                e.printStackTrace();
                result = new YamlConfiguration();
            }
            worldConfigs.put(key, result);
        }
        return result;
    }

    public void onEnable() {}
    public void onDisable() {}
    /**
     * Override to handle signed up players that join the server
     * or are on the server while this game becomes ready.
     */
    public void onPlayerReady(Player player) {
    }

    /**
     * Custom command handling. Override to implement game
     * commands.
     *
     * @return true if this game wants to handle the command,
     * false if it should be ignored and treated like a normal
     * command by Bukkit.
     */
    public boolean onCommand(Player player, String command, String[] args) {
        return false;
    }

    /**
     * Override if you wish to teleport newly joined players to a
     * specific spot.
     */
    public Location getSpawnLocation(Player player)
    {
        return null;
    }

    /**
     */
    public static enum State {
        INIT, // Initial state, setting up. Players may not join yet.
        PLAY, // People playing the game. Players may join some of the time.
        OVER, // Game over. Players may not join anymore.
        ;
    }
}
