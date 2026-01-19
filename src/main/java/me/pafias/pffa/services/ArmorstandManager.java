package me.pafias.pffa.services;

import me.pafias.pffa.listeners.ArmorstandListener;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import org.bukkit.entity.ArmorStand;

public class ArmorstandManager {

    private final pFFA plugin;

    private final GuiManager guiManager;

    public ArmorstandManager(pFFA plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        plugin.getServer().getPluginManager().registerEvents(new ArmorstandListener(plugin, this), plugin);
    }

    public void trigger(ArmorStand as, User user, boolean leftclick) throws NullPointerException {
        if (as.isCustomNameVisible() && as.getCustomName() != null && plugin.getSM().getKitManager().exists(as.getCustomName())) {
            // Clicked on Kit armorstand
            final Kit kit = plugin.getSM().getKitManager().getKit(as.getCustomName());
            if (!leftclick) {
                guiManager.openSpawnGui(user, kit);
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
                guiManager.openKitGui(user, spawn);
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
