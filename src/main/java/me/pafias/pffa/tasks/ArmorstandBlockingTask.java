package me.pafias.pffa.tasks;

import me.pafias.pffa.pFFA;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ArmorstandBlockingTask extends BukkitRunnable {

    private final pFFA plugin;

    public ArmorstandBlockingTask(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                for (final Entity nearby : player.getNearbyEntities(0.1, 0.1, 0.1)) {
                    if (nearby instanceof ArmorStand) {
                        if (plugin.getSM().getKitManager().exists(nearby.getCustomName()) || plugin.getSM().getSpawnManager().exists(nearby.getCustomName())) {
                            player.setVelocity(nearby.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(-0.5));
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

}
