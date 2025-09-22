package me.pafias.pffa.listeners;

import me.pafias.pffa.pFFA;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class JoinQuitListener1_8 extends JoinQuitListener {

    public JoinQuitListener1_8(pFFA plugin) {
        super(plugin);
    }

    @Override
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

}
