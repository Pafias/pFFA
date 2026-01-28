package me.pafias.pffa.services;

import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.gui.KitMenu;
import me.pafias.pffa.objects.gui.SpawnMenu;
import me.pafias.pffa.pFFA;
import me.pafias.putils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager implements Listener {

    private final KitManager kitManager;
    private final SpawnManager spawnManager;

    public GuiManager(pFFA plugin, KitManager kitManager, SpawnManager spawnManager) {
        this.kitManager = kitManager;
        this.spawnManager = spawnManager;

        kitInvCache = new ConcurrentHashMap<>();
        spawnInvCache = new ConcurrentHashMap<>();

        // Task to periodically update the GUIs
        Tasks.runRepeatingAsync(150, 150, () -> {
            for (Map.Entry<Player, SpawnMenu> entry : spawnInvCache.entrySet()) {
                final Player player = entry.getKey();
                final SpawnMenu spawnMenu = entry.getValue();
                spawnMenu.update(spawnManager.getSpawns(player).values());
            }
            for (Map.Entry<Player, KitMenu> entry : kitInvCache.entrySet()) {
                final Player player = entry.getKey();
                final KitMenu kitMenu = entry.getValue();
                kitMenu.update(kitManager.getKits(player).values());
            }
        });

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private final Map<Player, KitMenu> kitInvCache;
    private final Map<Player, SpawnMenu> spawnInvCache;

    public void openKitGui(User user, Spawn spawn) {
        KitMenu kitMenu = kitInvCache.get(user.getPlayer());
        if (kitMenu == null) {
            kitMenu = new KitMenu(user, spawn, kitManager.getKits(user.getPlayer()).values());
            kitInvCache.put(user.getPlayer(), kitMenu);
        }
        kitMenu.setSpawn(spawn);
        kitMenu.open();
    }

    public void openSpawnGui(User user, Kit kit) {
        SpawnMenu spawnMenu = spawnInvCache.get(user.getPlayer());
        if (spawnMenu == null) {
            spawnMenu = new SpawnMenu(user, kit, spawnManager.getSpawns(user.getPlayer()).values());
            spawnInvCache.put(user.getPlayer(), spawnMenu);
        }
        spawnMenu.setKit(kit);
        spawnMenu.open();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        kitInvCache.remove(event.getPlayer());
        spawnInvCache.remove(event.getPlayer());
    }

    public void shutdown() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final Inventory inventory = player.getOpenInventory().getTopInventory();
            if (inventory instanceof KitMenu || inventory instanceof SpawnMenu)
                player.closeInventory();
        }
        kitInvCache.clear();
        spawnInvCache.clear();
    }

}
