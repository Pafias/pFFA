package me.pafias.pafiasffa.listeners;

import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.User;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.IOException;

public class JoinQuitListener implements Listener {

    private final PafiasFFA plugin;

    public JoinQuitListener(PafiasFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        plugin.getSM().getUserManager().addUser(event.getPlayer());
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(plugin.getSM().getVariables().lobby);
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
                    if (user != null) user.getConfig().update("name", event.getPlayer().getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            event.getPlayer().kickPlayer(CC.t("&cData error. Please try again later."));
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskLaterAsynchronously(plugin, 20);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSM().getUserManager().removeUser(event.getPlayer());
    }

}
