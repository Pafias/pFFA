package me.pafias.pffa.npcs.local;

import me.pafias.putils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

public class VisibilityTask extends BukkitRunnable {

    private final LocalNpcManager npcManager;

    public VisibilityTask(LocalNpcManager npcManager) {
        this.npcManager = npcManager;
    }

    private static final double VIEW_DISTANCE = NumberConversions.square(32);

    @Override
    public void run() {
        for (final FakeNpc npc : npcManager.getNpcs().values()) {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(npc.getLocation().getWorld())) {
                    if (npc.getViewers().contains(player))
                        Tasks.runSync(() -> npc.destroy(player));
                    continue;
                }
                double distance = player.getLocation().distanceSquared(npc.getLocation());
                boolean inRange = distance <= VIEW_DISTANCE;
                if (!inRange && npc.getViewers().contains(player))
                    Tasks.runSync(() -> npc.destroy(player));
                else if (inRange && !npc.getViewers().contains(player) && isChunkLoadedForPlayer(player, npc.getLocation()))
                    Tasks.runSync(() -> npc.spawn(player));
            }
        }
    }

    private boolean isChunkLoadedForPlayer(Player player, Location location) {
        if (!player.getWorld().equals(location.getWorld()))
            return false;
        return location.getWorld().isChunkLoaded(location.getChunk());
    }

}
