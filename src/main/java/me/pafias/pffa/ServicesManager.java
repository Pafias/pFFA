package me.pafias.pffa;

import me.pafias.pffa.combatlog.CombatLogManager;
import me.pafias.pffa.services.*;
import me.pafias.pffa.util.PAPIExpansion;

public class ServicesManager {

    public ServicesManager(pFFA plugin) {
        variables = new Variables(plugin);
        if (variables.preferred_db_type.toLowerCase().contains("mongo"))
            mongoManager = new MongoManager(plugin, variables);
        else if (variables.preferred_db_type.toLowerCase().contains("db") || variables.preferred_db_type.toLowerCase().contains("sql"))
            mysqlManager = new MysqlManager(plugin, variables);
        userManager = new UserManager(plugin, variables);
        kitManager = new KitManager(plugin);
        spawnManager = new SpawnManager(plugin);
        armorstandManager = new ArmorstandManager(plugin);
        combatLogManager = new CombatLogManager(plugin, variables);
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            papiExpansion = new PAPIExpansion(plugin);
    }

    private final Variables variables;

    public Variables getVariables() {
        return variables;
    }

    private MongoManager mongoManager;

    public MongoManager getMongoManager() {
        return mongoManager;
    }

    private MysqlManager mysqlManager;

    public MysqlManager getMysqlManager() {
        return mysqlManager;
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

    private final CombatLogManager combatLogManager;

    public CombatLogManager getCombatLogManager() {
        return combatLogManager;
    }

    private PAPIExpansion papiExpansion;

    public PAPIExpansion getPAPIExpansion() {
        return papiExpansion;
    }

}
