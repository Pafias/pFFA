package me.pafias.pffa;

import lombok.Getter;
import me.pafias.pffa.combatlog.CombatLogManager;
import me.pafias.pffa.listeners.protocol.ProtocolListener;
import me.pafias.pffa.npcs.NpcManager;
import me.pafias.pffa.npcs.citizens.CitizensNpcManager;
import me.pafias.pffa.npcs.local.LocalNpcManager;
import me.pafias.pffa.services.*;
import me.pafias.pffa.storage.FileUserDataStorage;
import me.pafias.pffa.storage.MongoUserDataStorage;
import me.pafias.pffa.storage.MysqlUserDataStorage;
import me.pafias.pffa.storage.UserDataStorage;
import me.pafias.pffa.util.PAPIExpansion;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ServicesManager {

    private final pFFA plugin;

    public ServicesManager(pFFA plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        FileConfiguration config = plugin.getConfig();

        // Database initialization
        try {
            String preferredDbType = config.getString("database.type").toLowerCase();
            if (preferredDbType.contains("mongo")) {
                fileManager = null;
                mysqlManager = null;
                plugin.getLogger().info("Loading MongoDB Manager...");
                mongoManager = new MongoManager(
                        config.getString("database.mongo.host"),
                        config.getInt("database.mongo.port"),
                        config.getString("database.mongo.username"),
                        config.getString("database.mongo.password"),
                        config.getString("database.mongo.database"),
                        config.getString("database.mongo.options")
                );
                userDataStorage = new MongoUserDataStorage(mongoManager.getDatabase().getCollection("ffa"));
                plugin.getLogger().info("MongoDB Manager loaded.");
            } else if (preferredDbType.contains("sql")) {
                fileManager = null;
                mongoManager = null;
                plugin.getLogger().info("Loading MySQL Manager...");
                mysqlManager = new MysqlManager(
                        config.getString("database.mysql.host"),
                        config.getInt("database.mysql.port"),
                        config.getString("database.mysql.username"),
                        config.getString("database.mysql.password"),
                        config.getString("database.mysql.database"),
                        config.getBoolean("database.mysql.ssl")
                );
                userDataStorage = new MysqlUserDataStorage(mysqlManager.getDataSource());
                plugin.getLogger().info("MySQL Manager loaded.");
            } else {
                mongoManager = null;
                mysqlManager = null;
                plugin.getLogger().info("Loading File Manager...");
                fileManager = new FileManager(plugin);
                userDataStorage = new FileUserDataStorage(fileManager);
                plugin.getLogger().info("File Manager loaded.");
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to initialize the database/storage. Disabling plugin.");
            ex.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // User Manager initialization
        try {
            plugin.getLogger().info("Loading User Manager...");
            userManager = new UserManager(
                    plugin,
                    userDataStorage,
                    config.getInt("data_save_interval_minutes", 5)
            );
            plugin.getLogger().info("User Manager loaded.");
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to initialize the UserManager. Disabling plugin.");
            ex.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Kit and Spawn manager initialization
        try {
            plugin.getLogger().info("Loading Kits Manager...");
            kitManager = new KitManager(plugin);
            plugin.getLogger().info("Kits Manager loaded.");

            plugin.getLogger().info("Loading Spawns Manager...");
            spawnManager = new SpawnManager(plugin);
            plugin.getLogger().info("Spawns Manager loaded.");
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to initialize the KitManager or SpawnManager. Disabling plugin.");
            ex.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Gui manager initialization
        try {
            plugin.getLogger().info("Loading GUI Manager...");
            guiManager = new GuiManager(plugin, kitManager, spawnManager);
            plugin.getLogger().info("GUI Manager loaded.");
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to initialize the GuiManager. Disabling plugin.");
            ex.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Armorstand manager initialization; simple constructor, should never fail
        plugin.getLogger().info("Loading Armorstand Manager...");
        armorstandManager = new ArmorstandManager(plugin, guiManager);
        plugin.getLogger().info("Armorstand Manager loaded.");

        // NPC Manager initialization
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("Citizens")) {
                plugin.getLogger().info("Loading Citizens NPC Manager...");
                npcManager = new CitizensNpcManager(plugin, kitManager, spawnManager);
                plugin.getLogger().info("Citizens NPC Manager loaded.");
            } else if (plugin.parseVersion() < 8 || plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
                plugin.getLogger().info("Loading Local NPC Manager...");
                npcManager = new LocalNpcManager(plugin, kitManager, spawnManager, guiManager);
                plugin.getLogger().info("Local NPC Manager loaded.");
            } else {
                npcManager = null;
                plugin.getLogger().info("No NPC Manager loaded: Neither Citizens nor PacketEvents is present.");
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to initialize the NPC Manager.");
            ex.printStackTrace();
        }

        // Combat log manager initialization; simple constructor, should not fail
        plugin.getLogger().info("Loading Combatlog Manager...");
        combatLogManager = new CombatLogManager(plugin);
        plugin.getLogger().info("Combatlog Manager loaded.");

        // PlaceholderAPI expansion registration
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            plugin.getLogger().info("Registering PlaceholderAPI Expansion...");
            papiExpansion = new PAPIExpansion(plugin);
            papiExpansion.register();
            plugin.getLogger().info("PlaceholderAPI Expansion registered.");
        } else {
            papiExpansion = null;
            plugin.getLogger().info("PlaceholderAPI not found, skipping expansion registration.");
        }

        // Protocol listener initialization
        if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            plugin.getLogger().info("ProtocolLib found, registering packet listeners...");
            protocolListener = new ProtocolListener(plugin);
            plugin.getLogger().info("Packet listeners registered.");
        } else {
            protocolListener = null;
            plugin.getLogger().info("ProtocolLib not found, skipping packet listener registration.");
        }
    }

    public void onDisable() {
        if (userManager != null) {
            plugin.getLogger().info("Shutting down UserManager...");
            try {
                userManager.shutdown();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to shutdown User Manager:");
                e.printStackTrace();
            }
        }
        if (guiManager != null) {
            plugin.getLogger().info("Shutting down GUI Manager...");
            try {
                guiManager.shutdown();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to shutdown GUI Manager:");
                e.printStackTrace();
            }
        }
        if (papiExpansion != null) {
            plugin.getLogger().info("Unregistering PlaceholderAPI Expansion...");
            try {
                papiExpansion.unregister();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to unregister PlaceholderAPI Expansion:");
                e.printStackTrace();
            }
        }
        if (protocolListener != null) {
            plugin.getLogger().info("Shutting down protocol listeners...");
            try {
                protocolListener.shutdown();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to shutdown protocol listeners:");
                e.printStackTrace();
            }
        }
        if (npcManager != null) {
            plugin.getLogger().info("Shutting down NPC Manager...");
            try {
                npcManager.shutdown();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to shutdown NPC Manager:");
                e.printStackTrace();
            }
        }
        if (mongoManager != null) {
            plugin.getLogger().info("Shutting down Mongo Manager...");
            try {
                mongoManager.shutdown();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to shutdown Mongo Manager:");
                e.printStackTrace();
            }
        }
        if (mysqlManager != null) {
            plugin.getLogger().info("Shutting down MySQL Manager...");
            try {
                mysqlManager.closePool();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to shutdown MySQL Manager:");
                e.printStackTrace();
            }
        }
    }

    private MongoManager mongoManager;
    private MysqlManager mysqlManager;
    private FileManager fileManager;

    private UserDataStorage userDataStorage;

    private UserManager userManager;
    private KitManager kitManager;
    private SpawnManager spawnManager;
    private GuiManager guiManager;
    private ArmorstandManager armorstandManager;
    private NpcManager npcManager;
    private CombatLogManager combatLogManager;

    private PAPIExpansion papiExpansion;
    private ProtocolListener protocolListener;

}
