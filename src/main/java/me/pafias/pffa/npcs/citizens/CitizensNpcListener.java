package me.pafias.pffa.npcs.citizens;

import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class CitizensNpcListener implements Listener {

    private final pFFA plugin;

    private final CitizensNpcManager npcManager;

    public CitizensNpcListener(pFFA plugin, CitizensNpcManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!plugin.getFfaWorlds().contains(event.getPlayer().getWorld().getName())) return;
        User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        boolean cancel = npcManager.trigger(event.getRightClicked(), event.getRightClicked().getCustomName(), user, false);
        if (cancel)
            event.setCancelled(true);
    }

    @EventHandler
    public void onClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!plugin.getFfaWorlds().contains(damager.getWorld().getName())) return;
        User user = plugin.getSM().getUserManager().getUser(damager);
        if (user == null) return;
        boolean cancel = npcManager.trigger(event.getEntity(), event.getEntity().getCustomName(), user, true);
        if (cancel) {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }

}
