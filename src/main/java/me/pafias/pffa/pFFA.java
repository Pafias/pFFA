package me.pafias.pffa;

import me.pafias.pffa.botfight.BotListener;
import me.pafias.pffa.commands.commands.FFACommand;
import me.pafias.pffa.commands.commands.KillCommand;
import me.pafias.pffa.listeners.*;
import me.pafias.pffa.tasks.ArmorstandBlockingTask;
import me.pafias.pffa.tasks.AutoUpdaterTask;
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

    @Override
    public void onEnable() {
        plugin = this;
        version = parseVersion();
        servicesManager = new ServicesManager(plugin);
        new AutoUpdaterTask(plugin).run();
        getServer().getOnlinePlayers().stream().filter(p -> !p.hasMetadata("NPC")).forEach(p -> servicesManager.getUserManager().addUser(p));
        register();
        new ArmorstandBlockingTask(plugin).runTaskTimer(plugin, 100, 3 * 20L);
    }

    private void register() {
        PluginManager pm = getServer().getPluginManager();

        if (pm.getPlugin("PlaceholderAPI") != null)
            servicesManager.getPAPIExpansion().register();

        pm.registerEvents(new JoinQuitListener(plugin), plugin);
        pm.registerEvents(new ProtectionListener(plugin), plugin);
        pm.registerEvents(new ArmorstandListener(plugin), plugin);
        pm.registerEvents(new DeathListener(plugin), plugin);
        pm.registerEvents(new MiscListener(plugin), plugin);
        pm.registerEvents(new DeathMessagesHandler(plugin), plugin);

        if (pm.isPluginEnabled("Citizens"))
            pm.registerEvents(new BotListener(plugin), plugin);

        if (pm.isPluginEnabled("ProtocolLib"))
            new ProtocolListener(plugin);

        FFACommand ffaCommand = new FFACommand(plugin);
        getCommand("ffa").setExecutor(ffaCommand);
        getCommand("ffa").setTabCompleter(ffaCommand);
        if (plugin.getSM().getVariables().overrideKillCommand)
            getCommand("kill").setExecutor(new KillCommand(plugin));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(plugin);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            servicesManager.getPAPIExpansion().unregister();
        if (servicesManager.getMongoManager() != null)
            servicesManager.getMongoManager().shutdown();
        if (servicesManager.getMysqlManager() != null)
            servicesManager.getMysqlManager().closePool();
        getServer().getOnlinePlayers().forEach(player -> servicesManager.getUserManager().removeUser(player));
    }

    private double version;

    public double serverVersion() {
        return version;
    }

    private double parseVersion() {
        String version = getServer().getBukkitVersion();
        String[] var = version.split("\\.", 2);
        String[] var2 = var[1].split("-");
        String var3 = var2[0];
        double d = Double.parseDouble(var3);
        return d;
    }

}
