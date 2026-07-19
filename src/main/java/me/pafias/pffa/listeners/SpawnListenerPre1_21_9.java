package me.pafias.pffa.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class SpawnListenerPre1_21_9 implements Listener {

    private final Location spawnLocation;

    public SpawnListenerPre1_21_9(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(spawnLocation);
    }

}
