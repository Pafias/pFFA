package me.pafias.pffa.services;

import me.pafias.pffa.objects.*;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.RandomUtils;
import org.bukkit.entity.ArmorStand;

public class ArmorstandManager {

    private final pFFA plugin;

    public ArmorstandManager(pFFA plugin) {
        this.plugin = plugin;
    }

    public void trigger(ArmorStand as, User user, boolean leftclick) throws NullPointerException {
        if (as.isCustomNameVisible() && as.getCustomName() != null && plugin.getSM().getKitManager().exists(as.getCustomName())) {
            // Clicked on Kit armorstand
            Kit kit = plugin.getSM().getKitManager().getKit(as.getCustomName());
            if (!leftclick) {
                SpawnMenu menu = new SpawnMenu(user.getPlayer(), RandomUtils.parseSizeToInvSize(plugin.getSM().getSpawnManager().getSpawns().size()));
                menu.open((item, slot) -> {
                    Spawn spawn = plugin.getSM().getSpawnManager().getSpawn(item);
                    if (spawn == null) {
                        menu.setCloseOnClick(false);
                        return;
                    }
                    user.heal(false);
                    spawn.teleport(user.getPlayer());
                    if (kit != null)
                        kit.give(user.getPlayer());
                    user.setLastSpawn(spawn);
                    user.setLastKit(kit);
                });
            } else {
                kit.give(user.getPlayer());
                plugin.getSM().getSpawnManager().getDefaultSpawn().teleport(user.getPlayer());
                user.heal(false);
            }
            user.setLastKit(kit);
        } else if (as.isCustomNameVisible() && as.getCustomName() != null && plugin.getSM().getSpawnManager().exists(as.getCustomName())) {
            // Clicked on Spawn armorstand
            Spawn spawn = plugin.getSM().getSpawnManager().getSpawn(as.getCustomName());
            if (!leftclick) {
                KitMenu menu = new KitMenu(user.getPlayer(), RandomUtils.parseSizeToInvSize(plugin.getSM().getKitManager().getKits().size()));
                menu.open((item, slot) -> {
                    Kit kit = plugin.getSM().getKitManager().getKit(item);
                    if (kit == null) {
                        menu.setCloseOnClick(false);
                        return;
                    }
                    user.heal(false);
                    kit.give(user.getPlayer());
                    if (spawn != null)
                        spawn.teleport(user.getPlayer());
                    user.setLastSpawn(spawn);
                    user.setLastKit(kit);
                });
            } else {
                plugin.getSM().getKitManager().getDefaultKit().give(user.getPlayer());
                user.heal(false);
                spawn.teleport(user.getPlayer());
            }
            user.setLastSpawn(spawn);
        }
    }

}
