package me.pafias.pffa.migrations;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.FileManager;
import me.pafias.pffa.services.MongoManager;
import me.pafias.pffa.services.MysqlManager;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Migration1_0 extends Migration {

    public Migration1_0(pFFA plugin) {
        super(plugin, "0", "1.0");
    }

    @Override
    protected boolean execute(FileConfiguration config) {
        plugin.getLogger().info("Migrating config from 0.0 → 1.0...");

        // Database
        String databaseType = config.getString("preferred_db_type");
        if (databaseType != null && !databaseType.isEmpty()) {
            plugin.getLogger().info("Executing database migration... DO NOT stop the server while this is in progress.");

            if (databaseType.toLowerCase().contains("file")) {
                try {
                    FileManager fileManager = new FileManager(plugin);
                    Path playerdataDir = plugin.getDataFolder().toPath().resolve("playerdata");
                    if (Files.isDirectory(playerdataDir)) {
                        for (File file1 : playerdataDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"))) {
                            try {
                                FileConfiguration config1 = YamlConfiguration.loadConfiguration(file1);

                                File file2 = fileManager.getUserDataPath().resolve(file1.getName()).toFile();
                                file2.createNewFile();

                                FileConfiguration config2 = YamlConfiguration.loadConfiguration(file2);
                                config2.set("ffa.kills", config1.getInt("kills", 0));
                                config2.set("ffa.deaths", config1.getInt("deaths", 0));
                                config2.set("ffa.killstreak", config1.getInt("killstreak", 0));
                                try {
                                    config2.save(file2);
                                    file1.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                plugin.getLogger().warning("Failed to migrate file " + file1.getName());
                            }
                        }
                        try {
                            Files.delete(playerdataDir);
                        } catch (DirectoryNotEmptyException ignored) {
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    plugin.getLogger().warning("Failed to migrate playerdata files");
                }
            } else if (databaseType.toLowerCase().contains("sql")) {
                MysqlManager mysqlManager = null;
                try {
                    mysqlManager = new MysqlManager(
                            config.getString("mysql.host"),
                            config.getInt("mysql.port"),
                            config.getString("mysql.username"),
                            config.getString("mysql.password"),
                            config.getString("mysql.database"),
                            config.getBoolean("mysql.ssl", false)
                    );
                    var oldDs = mysqlManager.getDataSource();
                    try (var conn = oldDs.getConnection()) {
                        var stmt = conn.prepareStatement("SELECT uuid, kills, deaths, killstreak FROM ?;");
                        stmt.setString(1, config.getString("mysql.table"));
                        var rs = stmt.executeQuery();

                        try (var createStmt = conn.prepareStatement(
                                """
                                        CREATE TABLE IF NOT EXISTS ffa (
                                            uuid varchar(36) NOT NULL,
                                            kills INT DEFAULT 0 NOT NULL,
                                            deaths INT DEFAULT 0 NOT NULL,
                                            killstreak INT DEFAULT 0 NOT NULL,
                                            PRIMARY KEY (uuid)
                                        );
                                        """
                        )) {
                            createStmt.execute();
                        }

                        while (rs.next()) {
                            String uuid = rs.getString("uuid");
                            int kills = rs.getInt("kills");
                            int deaths = rs.getInt("deaths");
                            int killstreak = rs.getInt("killstreak");

                            try (var insertStmt = conn.prepareStatement(
                                    """
                                            INSERT INTO ffa (uuid, kills, deaths, killstreak)
                                            VALUES (?, ?, ?, ?)
                                            ON DUPLICATE KEY UPDATE kills = ?, deaths = ?, killstreak = ?;
                                            """
                            )) {
                                insertStmt.setString(1, uuid);
                                insertStmt.setInt(2, kills);
                                insertStmt.setInt(3, deaths);
                                insertStmt.setInt(4, killstreak);
                                insertStmt.setInt(5, kills);
                                insertStmt.setInt(6, deaths);
                                insertStmt.setInt(7, killstreak);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        mysqlManager.closePool();
                    } catch (Exception ignored) {
                    }
                }
            } else if (databaseType.toLowerCase().contains("mongo")) {
                MongoManager mongoManager = null;
                try {
                    mongoManager = new MongoManager(
                            config.getString("mongo.host"),
                            config.getInt("mongo.port"),
                            config.getString("mongo.username"),
                            config.getString("mongo.password"),
                            config.getString("mongo.database"),
                            config.getString("mongo.options")
                    );

                    MongoCollection<Document> collection = mongoManager.getDatabase().getCollection(config.getString("mongo.collection"));
                    collection.renameCollection(new MongoNamespace("ffa"));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        mongoManager.shutdown();
                    } catch (Exception ignored) {
                    }
                }
            }

            config.set("database.type", config.getString("preferred_db_type"));
            config.set("preferred_db_type", null);
            config.set("database.mongo", config.getConfigurationSection("mongo"));
            config.set("database.mongo.collection", null);
            config.set("database.mysql", config.getConfigurationSection("mysql"));
            config.set("database.mysql.table", null);
            config.set("mongo", null);
            config.set("mysql", null);

            plugin.getLogger().info("Database migration completed.");
        }

        // Death stuff
        config.set("death.heal_killer", config.getBoolean("heal_on_kill", true));
        config.set("death.killstreak_broadcasts", config.getConfigurationSection("killstreak_broadcasts"));
        config.set("heal_on_kill", null);
        config.set("killstreak_broadcasts", null);

        config.set("death.quick_respawn.enabled", config.getBoolean("quick_respawn", true));
        config.set("death.quick_respawn.single_action", config.getBoolean("quick_respawn_single_action", false));
        config.set("quick_respawn", null);
        config.set("quick_respawn_single_action", null);

        config.set("death.quick_respawn.permission", "ffa.quickrespawn");
        config.set("death.quick_respawn.item.material", "FEATHER");
        config.set("death.quick_respawn.item.name", "&bQuick Respawn &7&o(Check lore for info)");
        config.set("death.quick_respawn.item.lore", new String[]{
                "",
                "&7Left-click to respawn with &l&nlast chosen&r &7kit & spawn",
                "&7Right-click to respawn with &l&ndefault&r &7kit & spawn",
                ""
        });

        // Commands section
        config.set("commands.override_spawn_command", config.get("override_spawn_command"));
        config.set("commands.override_stats_command", config.get("override_stats_command"));
        config.set("commands.override_kill_command", config.get("override_kill_command"));
        config.set("commands.kill_command_cooldown", config.get("kill_command_cooldown"));
        config.set("override_spawn_command", null);
        config.set("override_stats_command", null);
        config.set("override_kill_command", null);
        config.set("kill_command_cooldown", null);
        config.set("commands.override_spectate_command", true);

        // Combatlog
        if (config.getBoolean("combatlog.display.action_bar", true))
            config.set("combatlog.display.action_bar", "&aYou are in combat. &cDo not log out!");
        else
            config.set("combatlog.display.action_bar", "");
        config.set("combatlog.attacker_message", "&aYou attacked &b%s&c. Do not log out");
        config.set("combatlog.victim_message", "&cYou got attacked by &b%s&c. Do not log out");
        config.set("combatlog.logout_broadcast", "&b%s &clogged out while in combat!");

        // Lobby
        config.set("lobby.detection_mode", config.getString("lobby_detection", "ycoord"));
        config.set("lobby_detection", null);

        config.set("lobby.spawn.world", config.getString("lobby.world"));
        config.set("lobby.spawn.x", config.getDouble("lobby.x"));
        config.set("lobby.spawn.y", config.getDouble("lobby.y"));
        config.set("lobby.spawn.z", config.getDouble("lobby.z"));
        config.set("lobby.spawn.yaw", config.getDouble("lobby.yaw"));
        config.set("lobby.spawn.pitch", config.getDouble("lobby.pitch"));
        config.set("lobby.world", null);
        config.set("lobby.x", null);
        config.set("lobby.y", null);
        config.set("lobby.z", null);
        config.set("lobby.yaw", null);
        config.set("lobby.pitch", null);

        config.set("lobby.h_radius", config.getDouble("lobby.hradius"));
        config.set("lobby.v_radius", config.getDouble("lobby.vradius"));
        config.set("lobby.x_bounds", config.getString("lobby.xbounds"));
        config.set("lobby.y_bounds", config.getString("lobby.ybounds"));
        config.set("lobby.z_bounds", config.getString("lobby.zbounds"));
        config.set("lobby.hradius", null);
        config.set("lobby.vradius", null);
        config.set("lobby.xbounds", null);
        config.set("lobby.ybounds", null);
        config.set("lobby.zbounds", null);

        return true;
    }

}
