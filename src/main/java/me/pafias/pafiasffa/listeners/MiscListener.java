package me.pafias.pafiasffa.listeners;

import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class MiscListener implements Listener {

    private final PafiasFFA plugin;

    public MiscListener(PafiasFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!plugin.getSM().getVariables().cleanArrows) return;
        if (event.getEntity() != null) {
            if (event.getEntity() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getEntity();
                arrow.remove();
            }
        }
    }

    @EventHandler
    public void onBlockArmorstand(PlayerMoveEvent event) {
        if (!plugin.getSM().getVariables().preventPlayersBlockingArmorstands) return;
        for (Entity entity : event.getPlayer().getNearbyEntities(0.1, 0.1, 0.1)) {
            if (entity instanceof ArmorStand) {
                ArmorStand as = ((ArmorStand) entity);
                if (as.isCustomNameVisible() && as.getCustomName() != null && (plugin.getSM().getKitManager().exists(as.getCustomName()) || plugin.getSM().getSpawnManager().exists(as.getCustomName()))) {
                    Vector vector = entity.getLocation().toVector().subtract(event.getPlayer().getLocation().toVector()).normalize().multiply(-0.5);
                    event.getPlayer().setVelocity(vector);
                }
            }
        }
    }

}
