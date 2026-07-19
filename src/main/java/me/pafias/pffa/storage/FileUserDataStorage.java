package me.pafias.pffa.storage;

import me.pafias.pffa.commands.subcommands.LeaderboardCommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.objects.exceptions.StorageException;
import me.pafias.pffa.services.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileUserDataStorage implements UserDataStorage {

    private final FileManager fileManager;

    public FileUserDataStorage(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    private UserData fromFile(String uuid, FileConfiguration config) {
        return new UserData(
                UUID.fromString(uuid),
                new FfaData(
                        config.getInt("ffa.kills", 0),
                        config.getInt("ffa.deaths", 0),
                        config.getInt("ffa.killstreak", 0)
                )
        );
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
        final File file = fileManager.getUserDataPath().resolve(uuid + ".yml").toFile();

        if (!file.exists())
            return null;

        return fromFile(uuid, YamlConfiguration.loadConfiguration(file));
    }

    @Override
    public void setUserData(UserData userData) {
        try {
            saveFile(userData);
        } catch (IOException e) {
            throw new StorageException("Failed to save data for " + userData.getUniqueId(), e);
        }
    }

    @Override
    public void setUserDataBatch(Collection<UserData> userData) {
        if (userData == null || userData.isEmpty())
            return;
        final List<Exception> failures = new ArrayList<>();
        for (UserData data : userData) {
            if (data == null)
                continue;
            try {
                saveFile(data);
            } catch (Exception e) {
                failures.add(new IOException("Failed to save " + data.getUniqueId(), e));
            }
        }

        if (!failures.isEmpty()) {
            final StorageException exception =
                    new StorageException(
                            "Failed to save "
                                    + failures.size()
                                    + " player files",
                            failures.getFirst()
                    );

            failures.stream()
                    .skip(1)
                    .forEach(exception::addSuppressed);

            throw exception;
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

        final Comparator<UserData> comparator = switch (statistic) {
            case KILLS -> Comparator.comparingInt(data -> data.getFfaData().getKills());
            case DEATHS -> Comparator.comparingInt(data -> data.getFfaData().getDeaths());
            case KILLSTREAK -> Comparator.comparingInt(data -> data.getFfaData().getKillstreak());
        };

        list.sort(comparator.reversed());
        return list.stream().limit(resultLimit).toList();
    }

    private void saveFile(UserData userData) throws IOException {
        final Path directory = fileManager.getUserDataPath();

        Files.createDirectories(directory);

        final String filename = userData.getUniqueId() + ".yml";

        final Path target = directory.resolve(filename);
        final Path temporary = directory.resolve(filename + ".tmp");

        toFile(userData).save(temporary.toFile());

        try {
            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

}
