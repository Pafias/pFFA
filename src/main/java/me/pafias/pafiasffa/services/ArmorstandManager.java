package me.pafias.pafiasffa.services;

import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.Kit;
import me.pafias.pafiasffa.objects.KitMenu;
import me.pafias.pafiasffa.objects.Spawn;
import me.pafias.pafiasffa.objects.SpawnMenu;
import me.pafias.pafiasffa.util.RandomUtils;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorstandManager {

    private final PafiasFFA plugin;

    public ArmorstandManager(PafiasFFA plugin) {
        this.plugin = plugin;
    }

    private Map<UUID, KitMenu> kitMenuCache = new HashMap<>();
    private Map<UUID, SpawnMenu> spawnMenuCache = new HashMap<>();

    public void trigger(ArmorStand as, Player player, boolean leftclick) throws NullPointerException {
        if (as.isCustomNameVisible() && as.getCustomName() != null && plugin.getSM().getKitManager().exists(as.getCustomName())) {
            Kit kit = plugin.getSM().getKitManager().getKit(as.getCustomName());
            if (!leftclick) {
                SpawnMenu menu;
                if (!spawnMenuCache.containsKey(player.getUniqueId())) {
                    menu = new SpawnMenu(player, RandomUtils.parseSizeToInvSize(plugin.getSM().getSpawnManager().getSpawns().size()), kit);
                    spawnMenuCache.put(player.getUniqueId(), menu);
                } else
                    menu = spawnMenuCache.get(player.getUniqueId());
                menu.open();
            } else {
                kit.give(player);
                plugin.getSM().getSpawnManager().getDefaultSpawn().teleport(player);
            }
        } else if (as.isCustomNameVisible() && as.getCustomName() != null && plugin.getSM().getSpawnManager().exists(as.getCustomName())) {
            Spawn spawn = plugin.getSM().getSpawnManager().getSpawn(as.getCustomName());
            if (!leftclick) {
                KitMenu menu = new KitMenu(player, RandomUtils.parseSizeToInvSize(plugin.getSM().getKitManager().getKits().size()), spawn);
                menu.open();
            } else {
                plugin.getSM().getKitManager().getDefaultKit().give(player);
                spawn.teleport(player);
            }
        }
    }

}
