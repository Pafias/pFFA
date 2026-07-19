package me.pafias.pffa.listeners;

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpawnListener implements Listener {

    private final Location spawnLocation;

    public SpawnListener(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    @EventHandler
    public void onSpawn(AsyncPlayerSpawnLocationEvent event) {
        event.setSpawnLocation(spawnLocation);
    }

}
