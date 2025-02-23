package me.pafias.pffa.listeners;

import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.CC;
import me.pafias.pffa.util.ItemBuilder;
import me.pafias.pffa.util.Tasks;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

public class DeathListener implements Listener {

    private final pFFA plugin;

    public DeathListener(pFFA plugin) {
        this.plugin = plugin;
        boolean quickRespawnSingleAction = plugin.getSM().getVariables().quickRespawnSingleAction;
        QUICK_RESPAWN_FEATHER = new ItemBuilder(Material.FEATHER)
                .setName(CC.t("&bQuick Respawn &7&o(Check lore for info)"))
                .setLore("",
                        CC.t("&7Left-click to respawn with &l&nlast chosen&r &7kit & spawn"),
                        quickRespawnSingleAction ?
                                CC.t("&7Right-click to respawn with &l&nlast chosen&r &7kit & spawn")
                                : CC.t("&7Right-click to respawn with &l&ndefault&r &7kit & spawn"),
                        "")
                .build();
    }

    private void handleKillstreakBroadcast(User user) {
        ConfigurationSection config = plugin.getSM().getVariables().killstreakBroadcasts;
        if (!config.getBoolean("enabled")) return;
        ConfigurationSection data = config.getConfigurationSection("data");
        Set<String> list = data.getKeys(false);
        int killstreak = user.getCurrentKillstreak();
        if (!list.contains(String.valueOf(killstreak))) return;
        List<String> messages = data.getStringList(String.valueOf(killstreak));
        messages.forEach(message -> plugin.getServer().broadcastMessage(CC.tf(message, user.getName())));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getEntity().getLocation().getWorld().getName()))
            return;
        if (plugin.serverVersion() <= 16.5)
            event.getEntity().setHealth(event.getEntity().getMaxHealth());
        Double killerHealth = null;
        if (event.getEntity().getKiller() != null && plugin.getSM().getVariables().healOnKill) {
            killerHealth = event.getEntity().getKiller().getHealth();
            event.getEntity().getKiller().setHealth(event.getEntity().getKiller().getMaxHealth());
        }
        event.getDrops().clear();
        event.getEntity().teleport(plugin.getSM().getVariables().lobby);
        event.getEntity().getInventory().clear();
        event.getEntity().getActivePotionEffects().forEach(pe -> event.getEntity().removePotionEffect(pe.getType()));
        event.getEntity().setFoodLevel(20);
        event.getEntity().setSaturation(0);
        if (event.getEntity().getKiller() != null) {
            User user = plugin.getSM().getUserManager().getUser(event.getEntity());
            if (user != null)
                user.addDeath();
            if (event.getEntity().hasMetadata("NPC"))
                event.setDeathMessage(CC.tf("%s was slain by %s %s", event.getEntity().getName(), event.getEntity().getKiller().getName(), CC.tf(plugin.getSM().getVariables().deathMessageSuffix, new DecimalFormat("#.##").format(killerHealth / 2))));
            else
                event.setDeathMessage(event.getDeathMessage() + " " + CC.tf(plugin.getSM().getVariables().deathMessageSuffix, new DecimalFormat("#.##").format(killerHealth / 2)));
            if (plugin.getSM().getVariables().healOnKill)
                event.getEntity().getKiller().setHealth(event.getEntity().getKiller().getMaxHealth());
            User killer = plugin.getSM().getUserManager().getUser(event.getEntity().getKiller());
            if (killer != null) {
                killer.addKill();
                handleKillstreakBroadcast(killer);
            }
        }
    }

    private final ItemStack QUICK_RESPAWN_FEATHER;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        if (!user.isInFFAWorld()) return;
        event.setRespawnLocation(plugin.getSM().getVariables().lobby);
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setSaturation(0);
        if (plugin.getSM().getVariables().quickRespawn && event.getPlayer().hasPermission("ffa.quickrespawn")) {
            Tasks.runLaterSync(20, () -> event.getPlayer().getInventory().addItem(QUICK_RESPAWN_FEATHER));
        }
    }

}
