package me.pafias.pffa.util;

import me.pafias.pffa.pFFA;
import org.bukkit.scheduler.BukkitTask;

public class Tasks {

    private static pFFA plugin = pFFA.get();

    public static BukkitTask runSync(Runnable runnable) {
        return plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public static BukkitTask runAsync(Runnable runnable) {
        return plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static BukkitTask runLaterSync(long delay, Runnable runnable) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static BukkitTask runLaterAsync(long delay, Runnable runnable) {
        return plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static BukkitTask runLaterSync(Runnable runnable, long delay) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static BukkitTask runLaterAsync(Runnable runnable, long delay) {
        return plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static BukkitTask runRepeatingSync(long delay, long period, Runnable runnable) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    public static BukkitTask runRepeatingAsync(long delay, long period, Runnable runnable) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    public static BukkitTask runRepeatingSync(Runnable runnable, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    public static BukkitTask runRepeatingAsync(Runnable runnable, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

}
