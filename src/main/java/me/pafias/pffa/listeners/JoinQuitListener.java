package me.pafias.pffa.listeners;

import me.pafias.pffa.pFFA;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class JoinQuitListener implements Listener {

    private final pFFA plugin;

    public JoinQuitListener(pFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            plugin.getSM().getUserManager().loadUser(event.getUniqueId());
        } catch (Exception ex) {
            ex.printStackTrace();
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "An error occurred while loading your data. Please try again later.");
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        try {
            plugin.getSM().getUserManager().addUser(event.getPlayer());
        } catch (Exception ex) {
            ex.printStackTrace();
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "An error occurred while loading your player. Please try again later.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
        event.getPlayer().getActivePotionEffects().forEach(pe -> event.getPlayer().removePotionEffect(pe.getType()));
        event.getPlayer().setHealth(event.getPlayer().getMaxHealth());
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setSaturation(0);
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(plugin.getLobbySpawn());
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSM().getUserManager().removeUser(event.getPlayer());
    }

}
