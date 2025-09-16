package me.pafias.pffa.services;

import me.pafias.pffa.migrations.Migration;
import me.pafias.pffa.migrations.Migration1_0;
import me.pafias.pffa.pFFA;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public class ConfigManager {

    private final pFFA plugin;
    private final File configFile;
    private FileConfiguration config;

    public ConfigManager(pFFA plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists())
            plugin.saveDefaultConfig();

        this.config = YamlConfiguration.loadConfiguration(configFile);

        migrations = Set.of(
                new Migration1_0(plugin)
        );

        runMigrations();

        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    private final Set<Migration> migrations;

    private void runMigrations() {
        String currentVersion = config.getString("config_version", "0"); // fallback for old configs

        FileConfiguration newConfig;
        try (InputStreamReader reader = new InputStreamReader(plugin.getResource("config.yml"))) {
            newConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default config.yml", e);
        }
        String targetVersion = newConfig.getString("config_version");

        plugin.getLogger().info("Current config version: " + currentVersion + ", target: " + targetVersion);

        for (Migration migration : migrations) {
            if (migration.shouldRun(currentVersion, targetVersion)) {
                boolean success = migration.migrate(config, configFile, targetVersion);
                if (!success)
                    plugin.getLogger().warning("Failed to migrate config.");
            }
        }
    }
}
