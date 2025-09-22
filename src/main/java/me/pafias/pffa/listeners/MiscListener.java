package me.pafias.pffa.listeners;

import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.putils.LCC;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumSet;
import java.util.Set;

public class MiscListener implements Listener {

    private final pFFA plugin;

    public MiscListener(pFFA plugin) {
        this.plugin = plugin;

        cleanArrows = plugin.getConfig().getBoolean("clean_arrows");
        interactivePlates = plugin.getConfig().getBoolean("interactive_pressureplates");

        final ConfigurationSection quickRespawnConfig = plugin.getConfig().getConfigurationSection("death.quick_respawn");
        quickRespawnEnabled = quickRespawnConfig.getBoolean("enabled");
        quickRespawnPermission = quickRespawnConfig.getString("permission");
        quickRespawnMaterial = Material.getMaterial(quickRespawnConfig.getString("item.material"));
        quickRespawnName = quickRespawnConfig.getString("item.name");
        quickRespawnSingleAction = quickRespawnConfig.getBoolean("single_action");
    }

    private static final Set<Material> PLATES = EnumSet.noneOf(Material.class);

    static {
        for (Material material : Material.values())
            if (material.name().endsWith("_PLATE"))
                PLATES.add(material);
    }

    private final boolean cleanArrows;
    private final boolean interactivePlates;

    private final boolean quickRespawnSingleAction;
    private final boolean quickRespawnEnabled;
    private final String quickRespawnPermission;
    private final String quickRespawnName;
    private final Material quickRespawnMaterial;

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.ARROW) return;
        if (!cleanArrows) return;
        event.getEntity().remove();
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        if (!PLATES.contains(event.getClickedBlock().getType())) return;
        if (!interactivePlates) return;
        final User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user != null && user.isInSpawn())
            event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(2));
    }

    @EventHandler
    public void onQuickRespawn(PlayerInteractEvent event) {
        if (!quickRespawnEnabled) return;
        if (!event.hasItem() || event.getAction() == Action.PHYSICAL) return;
        final ItemStack item = event.getItem();
        if (!item.hasItemMeta()) return;
        final ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return;
        if (!event.getPlayer().hasPermission(quickRespawnPermission)) return;
        if (event.getItem().getType() != quickRespawnMaterial)
            return;
        if (!meta.getDisplayName().equals(LCC.t(quickRespawnName)))
            return;
        final User user = plugin.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        // Left click = respawn with last kit and spawn
        // Right click = respawn with default kit and spawn unless the config option for single action is true
        final Kit kit;
        final Spawn spawn;
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && !quickRespawnSingleAction) {
            kit = plugin.getSM().getKitManager().getDefaultKit();
            spawn = plugin.getSM().getSpawnManager().getDefaultSpawn();
        } else {
            kit = user.getLastKit();
            spawn = user.getLastSpawn();
        }
        if (kit == null || spawn == null) return;
        kit.give(user.getPlayer());
        spawn.teleport(user.getPlayer());
        user.heal(false);
    }

}
