package me.pafias.pffa.listeners;

import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ProtectionListener implements Listener {

    private final pFFA plugin;

    private final ConfigurationSection config;

    public ProtectionListener(pFFA plugin) {
        this.plugin = plugin;
        config = plugin.getSM().getVariables().worldProtection;
    }

    @EventHandler
    public void onItem(PlayerItemDamageEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        if (plugin.getSM().getVariables().disableFalldamage)
            event.setDamage(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getEntity().getWorld().getName())) return;
        event.setFoodLevel(20);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getEntity().getWorld().getName())) return;
        User user = plugin.getSM().getUserManager().getUser((Player) event.getEntity());
        if (user == null) return;
        if (user.isInSpawn()) event.setCancelled(true);
    }

    @EventHandler
    public void onChangeFeather(InventoryClickEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getWhoClicked().getWorld().getName())) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().getType().equals(Material.FEATHER)) return;
        ItemStack item = event.getCurrentItem();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().toLowerCase().contains("respawn"))
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        if (event.getHook().getHookedEntity() instanceof ArmorStand)
            event.getHook().setHookedEntity(event.getPlayer());
    }

    // World protections

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE) && config.getBoolean("bypass_with_gm_creative"))
            return;
        if (event.getClickedBlock() == null) return;
        if (
                (event.getClickedBlock().getType().name().contains("DOOR") && config.getBoolean("prevent_doors"))
                        || (event.getClickedBlock().getType().name().contains("GATE") && config.getBoolean("prevent_gates"))
                        || (event.getClickedBlock().getType().name().contains("BUTTON") && config.getBoolean("prevent_buttons"))
                        || ((event.getClickedBlock().getType().name().contains("SOIL") || event.getClickedBlock().getType().name().contains("CROPS")) && config.getBoolean("prevent_soil_dry"))
                        || (event.getClickedBlock().getType().name().contains("PLATE") && config.getBoolean("prevent_pressure_plates"))
                        || (event.getClickedBlock().getType().name().contains("JUKEBOX") && config.getBoolean("prevent_juke_box"))
                        || ((event.getClickedBlock().getType().name().contains("CHEST") || event.getClickedBlock().getType().name().contains("BARREL") || event.getClickedBlock().getType().name().contains("SHULKER")) && config.getBoolean("prevent_chests"))
                        || (event.getClickedBlock().getType().name().contains("DROPPER") && config.getBoolean("prevent_dropper"))
                        || (event.getClickedBlock().getType().name().contains("DISPENSER") && config.getBoolean("prevent_dispenser"))
                        || (config.getStringList("prevent_custom").stream().anyMatch(cs -> event.getClickedBlock().getType().name().toLowerCase().contains(cs.toLowerCase())))
        )
            event.setCancelled(true);
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE) && config.getBoolean("bypass_with_gm_creative"))
            return;
        if (
                (event.getRightClicked() instanceof ItemFrame && config.getBoolean("prevent_itemframes"))
                        || (event.getRightClicked() instanceof Vehicle && config.getBoolean("prevent_vehicles"))
        )
            event.setCancelled(true);
    }

    @EventHandler
    public void onDamageEntity(EntityDamageEvent event) {
        Entity damager = null;
        if (event instanceof EntityDamageByEntityEvent)
            damager = ((EntityDamageByEntityEvent) event).getDamager();
        if (damager instanceof Player && ((Player) damager).getGameMode().equals(GameMode.CREATIVE) && config.getBoolean("bypass_with_gm_creative"))
            return;
        if (
                (event.getEntity() instanceof Vehicle && config.getBoolean("prevent_vehicles"))
        )
            event.setCancelled(true);
    }

    @EventHandler
    public void onInteractWithItemframe(HangingBreakEvent event) {
        Entity damager = null;
        if (event instanceof HangingBreakByEntityEvent)
            damager = ((HangingBreakByEntityEvent) event).getRemover();
        if (damager instanceof Player && ((Player) damager).getGameMode().equals(GameMode.CREATIVE) && config.getBoolean("bypass_with_gm_creative"))
            return;
        if (event.getEntity() instanceof ItemFrame && config.getBoolean("prevent_itemframes"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!(event.getInitiator() instanceof PlayerInventory)) return;
        PlayerInventory inv = (PlayerInventory) event.getInitiator();
        Player player = (Player) inv.getHolder();
        if (player == null || player.getGameMode().equals(GameMode.CREATIVE) && config.getBoolean("bypass_with_gm_creative"))
            return;
        // Prevent players from putting kit items in chests and such
        if (plugin.getSM().getKitManager().getKits().values().stream().anyMatch(kit -> kit.getItems().containsValue(event.getItem())))
            event.setCancelled(true);
    }

}
