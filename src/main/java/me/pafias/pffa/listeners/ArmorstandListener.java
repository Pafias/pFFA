package me.pafias.pffa.listeners;

import me.pafias.pffa.pFFA;
import me.pafias.pffa.objects.User;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class ArmorstandListener implements Listener {

    private final pFFA plugin;

    public ArmorstandListener(pFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        if (event.getPlayer().getGameMode() != null && event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
            return;
        User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        if (!user.isInFFAWorld()) return;
        event.setCancelled(true);
        plugin.getSM().getArmorstandManager().trigger((ArmorStand) event.getRightClicked(), user, false);
    }

    @EventHandler
    public void onClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof ArmorStand)) return;
        if (((Player) event.getDamager()).getGameMode() != null && ((Player) event.getDamager()).getGameMode().equals(GameMode.CREATIVE))
            return;
        User user = plugin.getSM().getUserManager().getUser((Player) event.getDamager());
        if (user == null) return;
        if (!user.isInFFAWorld()) return;
        event.setCancelled(true);
        event.setDamage(0);
        plugin.getSM().getArmorstandManager().trigger((ArmorStand) event.getEntity(), user, true);
    }

}
