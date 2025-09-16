package me.pafias.pffa.services;

import me.pafias.pffa.listeners.ArmorstandListener;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.gui.KitMenu;
import me.pafias.pffa.objects.gui.SpawnMenu;
import me.pafias.pffa.pFFA;
import org.bukkit.entity.ArmorStand;

public class ArmorstandManager {

    private final pFFA plugin;

    public ArmorstandManager(pFFA plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new ArmorstandListener(plugin, this), plugin);
    }

    public void trigger(ArmorStand as, User user, boolean leftclick) throws NullPointerException {
        if (as.isCustomNameVisible() && as.getCustomName() != null && plugin.getSM().getKitManager().exists(as.getCustomName())) {
            // Clicked on Kit armorstand
            final Kit kit = plugin.getSM().getKitManager().getKit(as.getCustomName());
            if (!leftclick) {
                new SpawnMenu(user, kit, plugin.getSM().getSpawnManager().getSpawns(user.getPlayer()).values())
                        .open();
            } else {
                kit.give(user.getPlayer());
                final Spawn spawn = plugin.getSM().getSpawnManager().getDefaultSpawn();
                spawn.teleport(user.getPlayer());
                user.heal(false);
                user.setLastSpawn(spawn);
            }
            user.setLastKit(kit);
        } else if (as.isCustomNameVisible() && as.getCustomName() != null && plugin.getSM().getSpawnManager().exists(as.getCustomName())) {
            // Clicked on Spawn armorstand
            final Spawn spawn = plugin.getSM().getSpawnManager().getSpawn(as.getCustomName());
            if (!leftclick) {
                new KitMenu(user, spawn, plugin.getSM().getKitManager().getKits(user.getPlayer()).values())
                        .open();
            } else {
                final Kit kit = plugin.getSM().getKitManager().getDefaultKit();
                kit.give(user.getPlayer());
                user.heal(false);
                spawn.teleport(user.getPlayer());
                user.setLastKit(kit);
            }
            user.setLastSpawn(spawn);
        }
    }

}
