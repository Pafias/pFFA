package me.pafias.pafiasffa.botfight;

import me.pafias.pafiasffa.PafiasFFA;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.SpawnReason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class BotListener implements Listener {

    private final PafiasFFA plugin;

    public BotListener(PafiasFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(NPCSpawnEvent event) {
        if (!event.getReason().equals(SpawnReason.RESPAWN) && !event.getReason().equals(SpawnReason.TIMED_RESPAWN) && !event.getReason().equals(SpawnReason.PLUGIN))
            return;
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getNPC().getEntity().getLocation().getWorld().getName()))
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSM().getKitManager().getDefaultKit().getItems().forEach(((Player) event.getNPC().getEntity()).getInventory()::setItem);
            }
        }.runTaskLater(plugin, 40);
    }

}
