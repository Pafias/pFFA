package me.pafias.pffa.services;

import me.pafias.pffa.pFFA;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Variables {

    private final pFFA plugin;

    // config.yml
    public String preferred_db_type = "file";
    public ConfigurationSection mongo;
    public ConfigurationSection mysql;

    public double dataSaveIntervalMinutes = 5;
    public Set<String> ffaWorlds = new HashSet<>(Collections.singletonList("FFA"));
    public boolean preventPlayersBlockingArmorstands = true;
    public boolean cleanArrows = true;
    public String deathMessageSuffix = "&7(&c%s ❤&7)";
    public boolean healOnKill = true;
    public Location lobby;
    public double lobbyHRadius = 20;
    public double lobbyVRadius = 6;
    public String lobbyXBounds;
    public String lobbyYBounds;
    public String lobbyZBounds;
    public int killCooldown = 5;
    public boolean overrideKillCommand = true;
    public String lobbyDetection = "ycoord";
    public boolean disableFalldamage = false;
    public boolean interactivePressureplates = true;
    public boolean quickRespawn = true;
    public ConfigurationSection killstreakBroadcasts;
    public ConfigurationSection combatlog;
    public ConfigurationSection worldProtection;

    public Variables(pFFA plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();
        reloadConfigYML();
    }

    private void reloadConfigYML() {
        preferred_db_type = plugin.getConfig().getString("preferred_db_type", "file");
        mongo = plugin.getConfig().getConfigurationSection("mongo");
        mysql = plugin.getConfig().getConfigurationSection("mysql");

        dataSaveIntervalMinutes = plugin.getConfig().getDouble("data_save_interval_minutes", 5);
        ffaWorlds = new HashSet<>(plugin.getConfig().getStringList("ffa_worlds"));
        preventPlayersBlockingArmorstands = plugin.getConfig().getBoolean("prevent_players_blocking_armorstands", true);
        cleanArrows = plugin.getConfig().getBoolean("clean_arrows", true);
        deathMessageSuffix = plugin.getConfig().getString("death_message_suffix");
        healOnKill = plugin.getConfig().getBoolean("heal_on_kill", true);
        disableFalldamage = plugin.getConfig().getBoolean("disable_falldamage", true);
        overrideKillCommand = plugin.getConfig().getBoolean("override_kill_command", true);
        killCooldown = plugin.getConfig().getInt("kill_command_cooldown", 5);
        interactivePressureplates = plugin.getConfig().getBoolean("interactive_pressureplates", true);
        quickRespawn = plugin.getConfig().getBoolean("quick_respawn", true);
        killstreakBroadcasts = plugin.getConfig().getConfigurationSection("killstreak_broadcasts");
        combatlog = plugin.getConfig().getConfigurationSection("combatlog");
        worldProtection = plugin.getConfig().getConfigurationSection("world_protection");
        lobby = new Location(
                plugin.getServer().getWorld(plugin.getConfig().getString("lobby.world", "world")),
                plugin.getConfig().getDouble("lobby.x", 0.5),
                plugin.getConfig().getDouble("lobby.y", 60),
                plugin.getConfig().getDouble("lobby.z", 0.5),
                (float) plugin.getConfig().getDouble("lobby.yaw", 0),
                (float) plugin.getConfig().getDouble("lobby.pitch", 0)
        );
        lobbyHRadius = plugin.getConfig().getDouble("lobby.hradius");
        lobbyVRadius = plugin.getConfig().getDouble("lobby.vradius");
        lobbyXBounds = plugin.getConfig().getString("lobby.xbounds");
        lobbyYBounds = plugin.getConfig().getString("lobby.ybounds");
        lobbyZBounds = plugin.getConfig().getString("lobby.zbounds");
        lobbyDetection = plugin.getConfig().getString("lobby_detection");
    }

}
