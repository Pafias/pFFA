package me.pafias.pffa.tasks;

import me.pafias.pffa.pFFA;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArmorstandBlockingTask extends BukkitRunnable {

    private final pFFA plugin;

    public ArmorstandBlockingTask(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            try {
                p.getNearbyEntities(0.1, 0.1, 0.1).forEach(e -> {
                    if (e instanceof ArmorStand) {
                        ArmorStand as = ((ArmorStand) e);
                        if (as.isCustomNameVisible() && as.getCustomName() != null && (plugin.getSM().getKitManager().exists(as.getCustomName()) || plugin.getSM().getSpawnManager().exists(as.getCustomName()))) {
                            Vector vector = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(-0.5);
                            p.setVelocity(vector);
                        }
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

}
