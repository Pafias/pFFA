package me.pafias.pffa.listeners;

import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.ArmorstandManager;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.HashSet;
import java.util.Set;

public class ArmorstandListener implements Listener {

    private final pFFA plugin;

    private final ArmorstandManager armorstandManager;

    public ArmorstandListener(pFFA plugin, ArmorstandManager armorstandManager) {
        this.plugin = plugin;
        this.armorstandManager = armorstandManager;

        ffaWorlds = new HashSet<>(plugin.getConfig().getStringList("ffa_worlds"));
    }

    private final Set<String> ffaWorlds;

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        if (!ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        final User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        event.setCancelled(true);
        armorstandManager.trigger((ArmorStand) event.getRightClicked(), user, false);
    }

    @EventHandler
    public void onClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (((Player) event.getDamager()).getGameMode() == GameMode.CREATIVE)
            return;
        if (!(event.getEntity() instanceof ArmorStand)) return;
        if (!ffaWorlds.contains(event.getDamager().getWorld().getName())) return;
        final User user = plugin.getSM().getUserManager().getUser(((Player) event.getDamager()));
        if (user == null) return;
        event.setCancelled(true);
        event.setDamage(0);
        armorstandManager.trigger((ArmorStand) event.getEntity(), user, true);
    }

}
