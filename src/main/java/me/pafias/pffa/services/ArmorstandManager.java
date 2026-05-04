package me.pafias.pffa.services;

import me.pafias.pffa.listeners.ArmorstandListener;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.putils.Tasks;
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
        if (as.isCustomNameVisible() && as.getCustomName() != null) {
            final Kit kit = plugin.getSM().getKitManager().getKit(as.getCustomName());
            if (kit != null) { // Clicked on Kit armorstand
                if (!leftclick) {
                    guiManager.openSpawnGui(user, kit);
                } else {
                    kit.give(user.getPlayer());
                    final Spawn spawn = plugin.getSM().getSpawnManager().getDefaultSpawn();
                    spawn.teleport(user.getPlayer());
                    user.heal(false);
                    user.setLastSpawn(spawn);
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                }
                user.setLastKit(kit);
                return;
            }
            final Spawn spawn = plugin.getSM().getSpawnManager().getSpawn(as.getCustomName());
            if (spawn != null) { // Clicked on Spawn armorstand
                if (!leftclick) {
                    guiManager.openKitGui(user, spawn);
                } else {
                    final Kit k = plugin.getSM().getKitManager().getDefaultKit();
                    k.give(user.getPlayer());
                    user.heal(false);
                    spawn.teleport(user.getPlayer());
                    user.setLastKit(k);
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                }
                user.setLastSpawn(spawn);
            }
        }
    }

}
