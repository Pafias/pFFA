package me.pafias.pffa.listeners;

import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MiscListener implements Listener {

    private final pFFA plugin;

    public MiscListener(pFFA plugin) {
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
    public void onPressurePlate(PlayerInteractEvent event) {
        if (!plugin.getSM().getVariables().interactivePressureplates) return;
        if (!event.getAction().equals(Action.PHYSICAL)) return;
        if (!event.getClickedBlock().getType().name().contains("PLATE")) return;
        User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user != null && user.isInSpawn())
            event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(2));
    }

    @EventHandler
    public void onQuickRespawn(PlayerInteractEvent event) {
        if (!plugin.getSM().getVariables().quickRespawn) return;
        if (!event.getAction().name().contains("CLICK")) return;
        if (!event.hasItem() || !event.getItem().getType().equals(Material.FEATHER)) return;
        ItemStack item = event.getItem();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().toLowerCase().contains("respawn"))
            return;
        User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        // Left click = respawn with last kit and spawn
        // Right click = respawn with default kit and spawn -> changed
        // CODE: 001-108
        Kit kit;
        Spawn spawn;
        if (event.getAction().name().contains("RIGHT") || event.getAction().name().contains("LEFT")) {
            kit = plugin.getSM().getKitManager().getDefaultKit();
            spawn = plugin.getSM().getSpawnManager().getDefaultSpawn();
        } else return;
        if (kit == null || spawn == null) return;
        kit.give(user.getPlayer());
        spawn.teleport(user.getPlayer());
        user.heal(false);
    }

}
