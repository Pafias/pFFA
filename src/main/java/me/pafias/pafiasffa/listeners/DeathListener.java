package me.pafias.pafiasffa.listeners;

import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.User;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.text.DecimalFormat;

public class DeathListener implements Listener {

    private final PafiasFFA plugin;

    public DeathListener(PafiasFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getEntity().getLocation().getWorld().getName()))
            return;
        if (plugin.parseVersion() <= 16.5)
            event.getEntity().setHealth(event.getEntity().getMaxHealth());
        Double killerHealth = null;
        if (event.getEntity().getKiller() != null && plugin.getSM().getVariables().healOnKill) {
            killerHealth = event.getEntity().getKiller().getHealth();
            event.getEntity().getKiller().setHealth(event.getEntity().getKiller().getMaxHealth());
        }
        event.getDrops().clear();
        event.getEntity().teleport(plugin.getSM().getVariables().lobby);
        event.getEntity().getInventory().clear();
        event.getEntity().getActivePotionEffects().forEach(pe -> event.getEntity().removePotionEffect(pe.getType()));
        if (event.getEntity().getKiller() != null) {
            User user = plugin.getSM().getUserManager().getUser(event.getEntity());
            if (user != null)
                user.addDeath();
            if (event.getEntity().hasMetadata("NPC"))
                event.setDeathMessage(CC.tf("%s was slain by %s %s", event.getEntity().getName(), event.getEntity().getKiller().getName(), CC.tf(plugin.getSM().getVariables().deathMessageSuffix, new DecimalFormat("#.##").format(killerHealth / 2))));
            else
                event.setDeathMessage(event.getDeathMessage() + CC.tf(" %s", CC.tf(plugin.getSM().getVariables().deathMessageSuffix, new DecimalFormat("#.##").format(killerHealth / 2))));
            if (plugin.getSM().getVariables().healOnKill)
                event.getEntity().getKiller().setHealth(event.getEntity().getKiller().getMaxHealth());
            User killer = plugin.getSM().getUserManager().getUser(event.getEntity().getKiller());
            if (killer != null)
                killer.addKill();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        if (!user.isInFFAWorld()) return;
        event.setRespawnLocation(plugin.getSM().getVariables().lobby);
    }

}
