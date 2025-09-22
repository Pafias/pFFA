package me.pafias.pffa.npcs.citizens;

import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import org.bukkit.entity.LivingEntity;
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
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        if (!plugin.getConfig().getStringList("ffa_worlds").contains(event.getPlayer().getWorld().getName())) return;
        User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        boolean cancel = npcManager.trigger((LivingEntity) event.getRightClicked(), event.getRightClicked().getCustomName(), user, false);
        if (cancel)
            event.setCancelled(true);
    }

    @EventHandler
    public void onClick(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!plugin.getConfig().getStringList("ffa_worlds").contains(event.getDamager().getWorld().getName())) return;
        User user = plugin.getSM().getUserManager().getUser((Player) event.getDamager());
        if (user == null) return;
        boolean cancel = npcManager.trigger((LivingEntity) event.getEntity(), event.getEntity().getCustomName(), user, true);
        if (cancel) {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }

}
