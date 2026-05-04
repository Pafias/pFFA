package me.pafias.pffa.objects.gui;

import lombok.Getter;
import lombok.Setter;
import me.pafias.pffa.pFFA;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiMenu implements Listener, InventoryHolder {

    public final pFFA plugin = pFFA.get();

    public Player player;

    @Getter
    private final Inventory inventory;

    @Getter
    private final String title;

    private final int size;
    private final List<ItemStack> items;

    @Setter
    private boolean closeOnClick = true;

    protected GuiMenu(Player player, String title, int size) {
        this(player, title, size, new ArrayList<>());
    }

    protected GuiMenu(Player player, String title, int size, List<ItemStack> items) {
        this.player = player;
        this.title = title;
        this.size = size;
        this.items = items;
        inventory = plugin.getServer().createInventory(this, size, title);
        items.forEach(inventory::addItem);
    }

    public void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    public abstract void clickHandler(@Nullable ItemStack item, int slot);

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        if (!event.getClickedInventory().equals(inventory)) return;
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        clickHandler(event.getCurrentItem(), event.getSlot());
        if (closeOnClick)
            player.closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory() == inventory)
            HandlerList.unregisterAll(this);
    }

}
