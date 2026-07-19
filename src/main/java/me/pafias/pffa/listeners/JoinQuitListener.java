package me.pafias.pffa.listeners;

import me.pafias.pffa.pFFA;
import me.pafias.putils.CC;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        try {
            plugin.getSM().getUserManager().addUser(event.getPlayer());
        } catch (Exception ex) {
            ex.printStackTrace();
            event.getPlayer().kick(CC.a("An error occurred while loading your player/data. Please try again later."));
            return;
        }

        final Player player = event.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSM().getUserManager().removeUser(event.getPlayer());
    }

}
