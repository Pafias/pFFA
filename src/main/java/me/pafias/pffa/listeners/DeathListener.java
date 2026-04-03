package me.pafias.pffa.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.putils.LCC;
import me.pafias.putils.Tasks;
import me.pafias.putils.builders.LegacyItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class DeathListener implements Listener {

    private final pFFA plugin;

    public DeathListener(pFFA plugin) {
        this.plugin = plugin;

        ffaWorlds = new HashSet<>(plugin.getConfig().getStringList("ffa_worlds"));
        healKillerOnDeath = plugin.getConfig().getBoolean("death.heal_killer");

        killstreakBroadcasts = new HashMap<>();
        try {
            final ConfigurationSection config = plugin.getConfig().getConfigurationSection("death.killstreak_broadcasts");
            if (config.getBoolean("enabled")) {
                final ConfigurationSection data = config.getConfigurationSection("data");
                final Set<String> list = data.getKeys(false);
                for (String s : list) {
                    try {
                        int kills = Integer.parseInt(s);
                        List<String> messages = data.getStringList(s);
                        killstreakBroadcasts.put(kills, messages);
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                        plugin.getLogger().warning("Invalid killstreak number: " + s);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            plugin.getLogger().warning("Failed to load killstreak broadcasts, disabling feature.");
        }

        try {
            final ConfigurationSection config = plugin.getConfig().getConfigurationSection("death.quick_respawn");
            quickRespawnEnabled = config.getBoolean("enabled");
            quickRespawnPermission = config.getString("permission");
            quickRespawnItem = new LegacyItemBuilder(Material.getMaterial(config.getString("item.material")))
                    .setName(LCC.t(config.getString("item.name")))
                    .setLore(LCC.tf(config.getStringList("item.lore")))
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
            plugin.getLogger().warning("Failed to load quick respawn item, disabling feature.");
        }
    }

    private final Set<String> ffaWorlds;

    private final Map<Integer, List<String>> killstreakBroadcasts;

    private void handleKillstreakBroadcast(User user) {
        final List<String> messages = killstreakBroadcasts.get(user.getCurrentKillstreak());
        if (messages == null || messages.isEmpty())
            return;
        for (String message : messages) {
            message = PlaceholderAPI.setPlaceholders(user.getPlayer(), message);
            plugin.getServer().broadcastMessage(LCC.tf(message, user.getName()));
        }
    }

    private final boolean healKillerOnDeath;

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!ffaWorlds.contains(event.getEntity().getLocation().getWorld().getName()))
            return;
        event.getDrops().clear();

        if(plugin.parseVersion() < 11) {
            // Immediate respawn for versions without the gamerule
            if (event.getEntity().getWorld().getGameRuleValue("doImmediateRespawn") == null)
                event.getEntity().setHealth(event.getEntity().getMaxHealth());
            Tasks.runLaterSync(2, () -> {
                event.getEntity().teleport(plugin.getLobbySpawn());
            });
        } else {
            event.getEntity().setHealth(event.getEntity().getMaxHealth());
            event.getEntity().teleport(plugin.getLobbySpawn());
        }

        event.getEntity().getInventory().clear();
        for (PotionEffect pe : event.getEntity().getActivePotionEffects())
            event.getEntity().removePotionEffect(pe.getType());
        event.getEntity().setFoodLevel(20);
        event.getEntity().setSaturation(0);
        if (event.getEntity().getKiller() != null) {
            if (healKillerOnDeath)
                event.getEntity().getKiller().setHealth(event.getEntity().getKiller().getMaxHealth());
            final User user = plugin.getSM().getUserManager().getUser(event.getEntity());
            if (user != null)
                user.addDeath();
            final User killer = plugin.getSM().getUserManager().getUser(event.getEntity().getKiller());
            if (killer != null) {
                killer.addKill();
                handleKillstreakBroadcast(killer);
                if (killer.getLastKit() != null)
                    killer.getLastKit().give(killer.getPlayer());
            }
        }
    }

    private boolean quickRespawnEnabled;
    private String quickRespawnPermission;
    private ItemStack quickRespawnItem = null;

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!ffaWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
            return;
        event.setRespawnLocation(plugin.getLobbySpawn());
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setSaturation(0);
        if (quickRespawnItem != null) {
            if (quickRespawnEnabled && event.getPlayer().hasPermission(quickRespawnPermission))
                Tasks.runLaterSync(5, () -> event.getPlayer().getInventory().addItem(quickRespawnItem));
        }
    }

}
