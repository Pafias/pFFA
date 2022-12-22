package me.pafias.pafiasffa.listeners;

import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class JoinQuitListener implements Listener {

    private final PafiasFFA plugin;

    public JoinQuitListener(PafiasFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        plugin.getSM().getUserManager().addUser(event.getPlayer());
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(plugin.getSM().getVariables().lobby);
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSM().getUserManager().removeUser(event.getPlayer());
    }

}
