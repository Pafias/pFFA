package me.pafias.pffa;

import me.pafias.pffa.commands.commands.*;
import me.pafias.pffa.listeners.*;
import me.pafias.pffa.tasks.ArmorstandBlockingTask;
import me.pafias.pffa.tasks.AutoUpdaterTask;
import me.pafias.pffa.util.Serializer;
import me.pafias.putils.pUtils;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class pFFA extends JavaPlugin {

    private static pFFA plugin;

    public static pFFA get() {
        return plugin;
    }

    private ServicesManager servicesManager;

    public ServicesManager getSM() {
        return servicesManager;
    }

    public Location getLobbySpawn() {
        return Serializer.parseConfigLocation("lobby.spawn");
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
        try {
            new AutoUpdaterTask(plugin).run();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        servicesManager.onEnable();
        register();

        getServer().getOnlinePlayers()
                .stream()
                .filter(p -> !p.hasMetadata("NPC"))
                .forEach(p -> servicesManager.getUserManager().addUser(p));

        new ArmorstandBlockingTask(plugin).runTaskTimer(plugin, 100, 3 * 20L);
    }

    public void register() {
        // Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinQuitListener(plugin), plugin);
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
        if (getConfig().getBoolean("commands.override_spectate_command")) {
            getCommand("spectate").setExecutor(new SpectateCommand(plugin));
        }
    }

    @Override
    public void onDisable() {
        servicesManager.onDisable();
    }

}
