package me.pafias.pffa.storage;

import me.pafias.pffa.commands.subcommands.LeaderboardCommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.services.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public List<UserData> getTopStatistic(LeaderboardCommand.Statistic statistic, int resultLimit) {
        if (statistic == null || resultLimit <= 0)
            return Collections.emptyList();

        final File[] files = fileManager.getUserDataPath().toFile().listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0)
            return Collections.emptyList();

        final List<UserData> list = new ArrayList<>();
        for (final File file : files) {
            final String fileName = file.getName();
            final String uuid = fileName.substring(0, fileName.length() - 4);

            try {
                list.add(fromFile(uuid, YamlConfiguration.loadConfiguration(file)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        Comparator<UserData> comparator = null;
        switch (statistic) {
            case KILLS:
                comparator = Comparator.comparingInt(data -> data.getFfaData().getKills());
                break;
            case DEATHS:
                comparator = Comparator.comparingInt(data -> data.getFfaData().getDeaths());
                break;
            case KILLSTREAK:
                comparator = Comparator.comparingInt(data -> data.getFfaData().getKillstreak());
                break;
        }

        list.sort(comparator.reversed());
        return list.stream().limit(resultLimit).collect(Collectors.toList());
    }

}
