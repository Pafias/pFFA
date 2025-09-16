package me.pafias.pffa.migrations;

import me.pafias.pffa.pFFA;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public abstract class Migration {

    protected final pFFA plugin;
    private final String fromVersion;
    private final String toVersion;

    public Migration(pFFA plugin, String fromVersion, String toVersion) {
        this.plugin = plugin;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    public boolean shouldRun(String currentVersion, String targetVersion) {
        return (currentVersion.equals(fromVersion) && targetVersion.equals(toVersion));
    }

    public boolean migrate(FileConfiguration config, File configFile, String targetVersion) {
        boolean result = false;

        if (targetVersion.equals(toVersion)) {
            result = execute(config);
            if (result) config.set("config_version", toVersion);
        }

        if (result)
            try {
                config.save(configFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save config after migration: " + e.getMessage());
                return false;
            }

        return result;
    }

    protected abstract boolean execute(FileConfiguration config);

}
