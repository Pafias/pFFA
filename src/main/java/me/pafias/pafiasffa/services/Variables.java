package me.pafias.pafiasffa.services;

import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class Variables {

    private final PafiasFFA plugin;

    // config.yml
    public boolean useMysql;
    public String mysqlHost = "127.0.0.1";
    public int mysqlPort = 3306;
    public String mysqlDatabase = "minecraft";
    public String mysqlTable = "ffa_players";
    public String mysqlUsername = "root";
    public String mysqlPassword = "";
    public boolean mysqlSSL = false;

    public Set<String> ffaWorlds = new HashSet<>(Collections.singletonList("FFA"));
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

    public Variables(PafiasFFA plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getConfig().options().copyDefaults(true);
                plugin.saveConfig();
                plugin.reloadConfig();
                reloadConfigYML();
            }
        }.runTaskAsynchronously(plugin);
    }

    private void reloadConfigYML() {
        useMysql = plugin.getConfig().getBoolean("mysql.enabled");
        mysqlHost = plugin.getConfig().getString("mysql.host");
        mysqlPort = plugin.getConfig().getInt("mysql.port");
        mysqlDatabase = plugin.getConfig().getString("mysql.database");
        mysqlUsername = plugin.getConfig().getString("mysql.username");
        mysqlPassword = plugin.getConfig().getString("mysql.password");
        mysqlSSL = plugin.getConfig().getBoolean("mysql.ssl");

        ffaWorlds = new HashSet<>(plugin.getConfig().getStringList("ffa_worlds"));
        deathMessageSuffix = plugin.getConfig().getString("death_message_suffix");
        healOnKill = plugin.getConfig().getBoolean("heal_on_kill");
        disableFalldamage = plugin.getConfig().getBoolean("disable_falldamage");
        overrideKillCommand = plugin.getConfig().getBoolean("override_kill_command");
        killCooldown = plugin.getConfig().getInt("kill_command_cooldown");
        lobby = new Location(
                plugin.getServer().getWorld(plugin.getConfig().getString("lobby.world")),
                plugin.getConfig().getDouble("lobby.x"),
                plugin.getConfig().getDouble("lobby.y"),
                plugin.getConfig().getDouble("lobby.z"),
                (float) plugin.getConfig().getDouble("lobby.yaw"),
                (float) plugin.getConfig().getDouble("lobby.pitch")
        );
        lobbyHRadius = plugin.getConfig().getDouble("lobby.hradius");
        lobbyVRadius = plugin.getConfig().getDouble("lobby.vradius");
        lobbyXBounds = plugin.getConfig().getString("lobby.xbounds");
        lobbyYBounds = plugin.getConfig().getString("lobby.ybounds");
        lobbyZBounds = plugin.getConfig().getString("lobby.zbounds");
        lobbyDetection = plugin.getConfig().getString("lobby_detection");
    }

    // Langs

    public Map<String, Object> langEN;
    public Map<String, Object> langNL;
    public Map<String, Object> langTR;

    private void reloadLangs() {
        File dir = new File(plugin.getDataFolder() + "/lang");
        if (!dir.exists())
            dir.mkdirs();
        langEN = new HashMap<>(YamlConfiguration.loadConfiguration(new File(dir, "en.yml")).getValues(false));
        langNL = new HashMap<>(YamlConfiguration.loadConfiguration(new File(dir, "nl.yml")).getValues(false));
        langTR = new HashMap<>(YamlConfiguration.loadConfiguration(new File(dir, "tr.yml")).getValues(false));
    }

}
