package me.pafias.pffa.services;

import me.pafias.pffa.events.FFAPlayerSpawnedEvent;
import me.pafias.pffa.events.FFAPlayerSpawnEvent;
import me.pafias.pffa.listeners.ArmorstandListener;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.putils.Tasks;
import org.bukkit.Bukkit;
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
            Kit kit = plugin.getSM().getKitManager().getKit(as.getCustomName());
            if (kit != null) { // Clicked on Kit armorstand
                if (!leftclick) {
                    guiManager.openSpawnGui(user, kit);
                    user.setLastKit(kit);
                } else {
                    Spawn spawn = plugin.getSM().getSpawnManager().getDefaultSpawn();

                    final FFAPlayerSpawnEvent event = new FFAPlayerSpawnEvent(user.getPlayer(), spawn, kit);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return;
                    spawn = event.getSpawn();
                    kit = event.getKit();
                    if (spawn != null) {
                        event.getSpawn().teleport(user.getPlayer());
                        user.setLastSpawn(spawn);
                    }
                    if (kit != null) {
                        kit.give(user.getPlayer());
                        user.setLastKit(kit);
                    }
                    user.heal(false);
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                    Bukkit.getPluginManager().callEvent(new FFAPlayerSpawnedEvent(user.getPlayer(), spawn, kit));
                }
                return;
            }
            Spawn spawn = plugin.getSM().getSpawnManager().getSpawn(as.getCustomName());
            if (spawn != null) { // Clicked on Spawn armorstand
                if (!leftclick) {
                    guiManager.openKitGui(user, spawn);
                    user.setLastSpawn(spawn);
                } else {
                    Kit k = plugin.getSM().getKitManager().getDefaultKit();

                    final FFAPlayerSpawnEvent event = new FFAPlayerSpawnEvent(user.getPlayer(), spawn, k);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return;
                    spawn = event.getSpawn();
                    k = event.getKit();
                    if (spawn != null) {
                        event.getSpawn().teleport(user.getPlayer());
                        user.setLastSpawn(spawn);
                    }
                    if (k != null) {
                        k.give(user.getPlayer());
                        user.setLastKit(k);
                    }
                    user.heal(false);
                    Bukkit.getPluginManager().callEvent(new FFAPlayerSpawnedEvent(user.getPlayer(), spawn, k));
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                }
            }
        }
    }

}
