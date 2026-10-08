package me.pafias.pffa;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.Getter;
import me.pafias.pffa.commands.commands.*;
import me.pafias.pffa.listeners.*;
import me.pafias.pffa.tasks.ArmorstandBlockingTask;
import me.pafias.pffa.tasks.AutoUpdaterTask;
import me.pafias.pffa.util.Serializer;
import me.pafias.putils.CC;
import me.pafias.putils.pUtils;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class pFFA extends JavaPlugin {

    private static pFFA plugin;

    public static pFFA get() {
        return plugin;
    }

    private ServicesManager servicesManager;

    public ServicesManager getSM() {
        return servicesManager;
    }

    @Override
    public void onLoad() {
        plugin = this;
        pUtils.setPlugin(plugin);

        // Services Manager
        servicesManager = new ServicesManager(plugin);
        servicesManager.onLoad();
    }

    @Override
    public void onEnable() {
        if (getConfig().getBoolean("auto_update"))
            try {
                new AutoUpdaterTask(plugin).run();
            } catch (Throwable t) {
                t.printStackTrace();
            }

        ffaWorlds = getConfig().getStringList("ffa_worlds");
        lobbySpawn = Serializer.parseConfigLocation("lobby.spawn");

        servicesManager.onEnable();
        register();

        getServer().getOnlinePlayers()
                .stream()
                .filter(p -> !p.hasMetadata("NPC"))
                .forEach(p -> {
                    try {
                        servicesManager.getUserManager().loadUser(p.getUniqueId());
                        servicesManager.getUserManager().addUser(p);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        p.kickPlayer(CC.t("&cFailed to load your data."));
                    }
                });

        new ArmorstandBlockingTask(plugin).runTaskTimer(plugin, 100, 3 * 20L);
    }

    public void register() {
        // Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinQuitListener(plugin), plugin);

        try {
            // HOW IS THERE NO (NATIVE) WAY TO GET THE SERVER'S PROTOCOL VERSION NUMBER... SMH
            if (getServer().getPluginManager().isPluginEnabled("packetevents")) {
                final ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
                if (version.isNewerThanOrEquals(ServerVersion.V_1_21_9))
                    pm.registerEvents(new SpawnListener(getLobbySpawn()), plugin);
                else
                    pm.registerEvents(new SpawnListenerPre1_21_9(getLobbySpawn()), plugin);
            } else if (getServer().getPluginManager().isPluginEnabled("ViaVersion")) {
                final ProtocolVersion version = Via.getAPI().getServerVersion().highestSupportedProtocolVersion();
                if (version.newerThanOrEqualTo(ProtocolVersion.v1_21_9))
                    pm.registerEvents(new SpawnListener(getLobbySpawn()), plugin);
                else
                    pm.registerEvents(new SpawnListenerPre1_21_9(getLobbySpawn()), plugin);
            }
        } catch (Throwable t) {
            // Fallback, just in case any of that shit above hits the fan :sob:
            pm.registerEvents(new SpawnListenerPre1_21_9(getLobbySpawn()), plugin);
        }

        pm.registerEvents(new ProtectionListener(plugin), plugin);
        pm.registerEvents(new DeathListener(plugin), plugin);
        pm.registerEvents(new MiscListener(plugin), plugin);
        pm.registerEvents(new DeathMessagesHandler(plugin), plugin);

        // Commands
        FFACommand ffaCommand = new FFACommand();
        getCommand("ffa").setExecutor(ffaCommand);
        getCommand("ffa").setTabCompleter(ffaCommand);

        if (getConfig().getBoolean("commands.override_kill_command"))
            getCommand("kill").setExecutor(new KillCommand(plugin));
        if (getConfig().getBoolean("commands.override_stats_command"))
            getCommand("stats").setExecutor(new StatsCommand(plugin));
        if (getConfig().getBoolean("commands.override_spawn_command"))
            getCommand("spawn").setExecutor(new SpawnCommand(plugin));
        if (getConfig().getBoolean("commands.override_spectate_command"))
            getCommand("spectate").setExecutor(new SpectateCommand(plugin));
        if (getConfig().getBoolean("commands.override_leaderboard_command"))
            getCommand("leaderboard").setExecutor(new LeaderboardCommand(plugin));
        if (getConfig().getBoolean("commands.override_compare_command"))
            getCommand("compare").setExecutor(new CompareCommand(plugin));
    }

    @Override
    public void onDisable() {
        servicesManager.onDisable();
    }

    @Getter
    private List<String> ffaWorlds;
    @Getter
    private Location lobbySpawn;

}
