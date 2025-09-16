package me.pafias.pffa.storage;

import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.services.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

public class FileUserDataStorage implements UserDataStorage {

    private final FileManager fileManager;

    public FileUserDataStorage(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    private UserData fromFile(String uuid, FileConfiguration config) {
        int kills = config.getInt("ffa.kills", 0);
        int deaths = config.getInt("ffa.deaths", 0);
        int killstreak = config.getInt("ffa.killstreak", 0);
        return new UserData(false, UUID.fromString(uuid), new FfaData(kills, deaths, killstreak));
    }

    private FileConfiguration toFile(UserData userData) {
        FileConfiguration config = new YamlConfiguration();
        config.set("ffa.kills", userData.getFfaData().getKills());
        config.set("ffa.deaths", userData.getFfaData().getDeaths());
        config.set("ffa.killstreak", userData.getFfaData().getKillstreak());
        return config;
    }

    @Override
    public UserData getUserData(String uuid) {
        File file = new File(fileManager.getUserDataPath().toFile(), uuid + ".yml");
        if (!file.exists()) return null;
        return fromFile(uuid, YamlConfiguration.loadConfiguration(file));
    }

    @Override
    public void setUserData(UserData userData) {
        String uuid = userData.getUniqueId().toString();
        FileConfiguration config = toFile(userData);

        try {
            config.save(fileManager.getUserDataPath().resolve(uuid + ".yml").toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
