package me.pafias.pafiasffa;

import me.pafias.pafiasffa.botfight.BotListener;
import me.pafias.pafiasffa.commands.commands.FFACommand;
import me.pafias.pafiasffa.commands.commands.KillCommand;
import me.pafias.pafiasffa.listeners.ArmorstandListener;
import me.pafias.pafiasffa.listeners.DeathListener;
import me.pafias.pafiasffa.listeners.JoinQuitListener;
import me.pafias.pafiasffa.listeners.ProtectionListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PafiasFFA extends JavaPlugin {

    private static PafiasFFA plugin;

    public static PafiasFFA get() {
        return plugin;
    }

    private ServicesManager servicesManager;

    public ServicesManager getSM() {
        return servicesManager;
    }

    @Override
    public void onEnable() {
        plugin = this;
        servicesManager = new ServicesManager(plugin);
        getServer().getOnlinePlayers().stream().filter(p -> !p.hasMetadata("NPC")).forEach(p -> servicesManager.getUserManager().addUser(p));
        register();
    }

    private void register() {
        PluginManager pm = getServer().getPluginManager();

        if (pm.getPlugin("PlaceholderAPI") != null)
            servicesManager.getPAPIExpansion().register();

        pm.registerEvents(new JoinQuitListener(plugin), plugin);
        pm.registerEvents(new ProtectionListener(plugin), plugin);
        pm.registerEvents(new ArmorstandListener(plugin), plugin);
        pm.registerEvents(new DeathListener(plugin), plugin);

        if (pm.isPluginEnabled("Citizens"))
            pm.registerEvents(new BotListener(plugin), plugin);

        FFACommand ffaCommand = new FFACommand(plugin);
        getCommand("ffa").setExecutor(ffaCommand);
        getCommand("ffa").setTabCompleter(ffaCommand);
        if (plugin.getSM().getVariables().overrideKillCommand)
            getCommand("kill").setExecutor(new KillCommand(plugin));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(plugin);
        getServer().getOnlinePlayers().forEach(player -> servicesManager.getUserManager().removeUser(player));
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            servicesManager.getPAPIExpansion().unregister();
        plugin = null;
    }

    public double parseVersion() {
        String version = getServer().getBukkitVersion();
        String[] var = version.split("\\.", 2);
        String[] var2 = var[1].split("-");
        String var3 = var2[0];
        double d = Double.parseDouble(var3);
        return d;
    }

}
