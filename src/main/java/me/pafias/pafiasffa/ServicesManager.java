package me.pafias.pafiasffa;

import me.pafias.pafiasffa.services.*;
import me.pafias.pafiasffa.util.PAPIExpansion;

public class ServicesManager {

    public ServicesManager(PafiasFFA plugin) {
        variables = new Variables(plugin);
        databaseManager = new DatabaseManager(plugin, variables);
        userManager = new UserManager(plugin, variables);
        kitManager = new KitManager(plugin);
        spawnManager = new SpawnManager(plugin);
        armorstandManager = new ArmorstandManager(plugin);
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            papiExpansion = new PAPIExpansion(plugin);
    }

    private final Variables variables;

    public Variables getVariables() {
        return variables;
    }

    private final DatabaseManager databaseManager;

    public DatabaseManager getDBManager() {
        return databaseManager;
    }

    private final UserManager userManager;

    public UserManager getUserManager() {
        return userManager;
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

    private PAPIExpansion papiExpansion;

    public PAPIExpansion getPAPIExpansion() {
        return papiExpansion;
    }

}
