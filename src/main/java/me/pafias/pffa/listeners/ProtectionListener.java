package me.pafias.pffa.listeners;

import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;

import java.util.Set;

public class ProtectionListener implements Listener {

    private final pFFA plugin;

    public ProtectionListener(pFFA plugin) {
        this.plugin = plugin;
        this.ffaWorlds = Set.copyOf(plugin.getConfig().getStringList("ffa_worlds"));
    }

    private final Set<String> ffaWorlds;

    @EventHandler(ignoreCancelled = true)
    public void onItem(PlayerItemDamageEvent event) {
        if (!ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!ffaWorlds.contains(event.getEntity().getWorld().getName())) return;
        event.setFoodLevel(20);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!ffaWorlds.contains(player.getWorld().getName())) return;
        final User user = plugin.getSM().getUserManager().getUser(player);
        if (user == null) return;
        if (user.isInSpawn()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {
        if (event.getHook().getHookedEntity().getType() != EntityType.ARMOR_STAND) return;
        if (!ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        event.getHook().setHookedEntity(event.getPlayer());
    }

    // World protections

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE) && plugin.getConfig().getBoolean("world_protection.bypass_with_gm_creative"))
            return;
        if (
                (event.getClickedBlock().getType().name().contains("DOOR") && plugin.getConfig().getBoolean("world_protection.prevent_doors"))
                        || (event.getClickedBlock().getType().name().contains("GATE") && plugin.getConfig().getBoolean("world_protection.prevent_gates"))
                        || (event.getClickedBlock().getType().name().contains("BUTTON") && plugin.getConfig().getBoolean("world_protection.prevent_buttons"))
                        || ((event.getClickedBlock().getType().name().contains("SOIL") || event.getClickedBlock().getType().name().contains("CROPS")) && plugin.getConfig().getBoolean("world_protection.prevent_soil_dry"))
                        || (event.getClickedBlock().getType().name().contains("PLATE") && plugin.getConfig().getBoolean("world_protection.prevent_pressure_plates"))
                        || (event.getClickedBlock().getType().name().contains("JUKEBOX") && plugin.getConfig().getBoolean("world_protection.prevent_juke_box"))
                        || ((event.getClickedBlock().getType().name().contains("CHEST") || event.getClickedBlock().getType().name().contains("BARREL") || event.getClickedBlock().getType().name().contains("SHULKER")) && plugin.getConfig().getBoolean("world_protection.prevent_chests"))
                        || (event.getClickedBlock().getType().name().contains("DROPPER") && plugin.getConfig().getBoolean("world_protection.prevent_dropper"))
                        || (event.getClickedBlock().getType().name().contains("DISPENSER") && plugin.getConfig().getBoolean("world_protection.prevent_dispenser"))
                        || (plugin.getConfig().getStringList("world_protection.prevent_custom").stream().anyMatch(cs -> event.getClickedBlock().getType().name().toLowerCase().contains(cs.toLowerCase())))
        )
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE
                && plugin.getConfig().getBoolean("world_protection.bypass_with_gm_creative"))
            return;
        boolean shouldPrevent = false;
        if (event.getRightClicked() instanceof ItemFrame) {
            shouldPrevent = plugin.getConfig().getBoolean("world_protection.prevent_itemframes");
        } else if (event.getRightClicked() instanceof Vehicle) {
            shouldPrevent = plugin.getConfig().getBoolean("world_protection.prevent_vehicles");
        }
        if (!shouldPrevent) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageEntity(EntityDamageEvent event) {
        boolean shouldPrevent = false;
        if (event.getEntity() instanceof ItemFrame) {
            shouldPrevent = plugin.getConfig().getBoolean("world_protection.prevent_itemframes");
        } else if (event.getEntity() instanceof Vehicle) {
            shouldPrevent = plugin.getConfig().getBoolean("world_protection.prevent_vehicles");
        }
        if (!shouldPrevent) return;
        if (event instanceof EntityDamageByEntityEvent e
                && e.getDamager() instanceof Player damager
                && damager.getGameMode() == GameMode.CREATIVE
                && plugin.getConfig().getBoolean("world_protection.bypass_with_gm_creative"))
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractWithItemframe(HangingBreakEvent event) {
        Entity damager = null;
        if (event instanceof HangingBreakByEntityEvent)
            damager = ((HangingBreakByEntityEvent) event).getRemover();
        if (damager instanceof Player player && player.getGameMode().equals(GameMode.CREATIVE) && plugin.getConfig().getBoolean("world_protection.bypass_with_gm_creative"))
            return;
        if (event.getEntity() instanceof ItemFrame && plugin.getConfig().getBoolean("world_protection.prevent_itemframes"))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMove(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getView().getTopInventory().getType() != InventoryType.PLAYER && event.getView().getTopInventory().getType() != InventoryType.CRAFTING) {
            final Player player = (Player) event.getWhoClicked();
            if (!ffaWorlds.contains(player.getWorld().getName())) return;
            if (player.getGameMode().equals(GameMode.CREATIVE) && plugin.getConfig().getBoolean("world_protection.bypass_with_gm_creative"))
                return;
            event.setCancelled(true);
        }
    }

}
