package me.pafias.pffa.tasks;

import me.pafias.pffa.pFFA;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArmorstandBlockingTask extends BukkitRunnable {

    private final pFFA plugin;

    public ArmorstandBlockingTask(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.parseVersion() < 8)
            return;
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            try {
                for (Entity nearby : p.getNearbyEntities(0.1, 0.1, 0.1)) {
                    if (nearby instanceof ArmorStand) {
                        final ArmorStand as = (ArmorStand) nearby;
                        if (as.isCustomNameVisible() && as.getCustomName() != null && (plugin.getSM().getKitManager().exists(as.getCustomName()) || plugin.getSM().getSpawnManager().exists(as.getCustomName()))) {
                            Vector vector = nearby.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(-0.5);
                            p.setVelocity(vector);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

}
