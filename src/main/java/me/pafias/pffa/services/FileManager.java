package me.pafias.pffa.services;

import lombok.Getter;
import me.pafias.pffa.pFFA;

import java.nio.file.Path;

@Getter
public class FileManager {

    private final pFFA plugin;

    public FileManager(pFFA plugin) {
        this.plugin = plugin;
        this.userDataPath = plugin.getDataFolder().toPath().resolve("userdata");
        if (!userDataPath.toFile().exists())
            userDataPath.toFile().mkdirs();
    }

    private final Path userDataPath;

}
