package me.pafias.pafiasffa;

import me.pafias.pafiasffa.services.*;
import me.pafias.pafiasffa.util.PAPIExpansion;

public class ServicesManager {

    private final PafiasFFA plugin;

    public ServicesManager(PafiasFFA plugin) {
        this.plugin = plugin;
        databaseManager = new DatabaseManager(plugin);
        userManager = new UserManager(plugin);
        variables = new Variables(plugin);
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            papiExpansion = new PAPIExpansion(plugin);
        kitManager = new KitManager(plugin);
        spawnManager = new SpawnManager(plugin);
        armorstandManager = new ArmorstandManager(plugin);
    }

    private DatabaseManager databaseManager;

    public DatabaseManager getDBManager() {
        return databaseManager;
    }

    private PAPIExpansion papiExpansion;

    public PAPIExpansion getPAPIExpansion() {
        return papiExpansion;
    }

    private final UserManager userManager;

    public UserManager getUserManager() {
        return userManager;
    }

    private final Variables variables;

    public Variables getVariables() {
        return variables;
    }

    private final KitManager kitManager;

    public KitManager getKitManager() {
        return kitManager;
    }

    private final SpawnManager spawnManager;

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    private final ArmorstandManager armorstandManager;

    public ArmorstandManager getArmorstandManager() {
        return armorstandManager;
    }

}
