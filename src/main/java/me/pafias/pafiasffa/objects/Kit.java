package me.pafias.pafiasffa.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Kit {

    private String name;
    private ItemStack gui_item;
    private Map<Integer, ItemStack> items;

    public Kit(String name, ItemStack gui_item, Map<Integer, ItemStack> items) {
        this.name = name;
        this.gui_item = gui_item;
        this.items = items;
    }

    public void give(Player player) {
        getItems().keySet().forEach(slot -> {
            ItemStack item = getItems().get(slot);
            player.getInventory().setItem(slot, item);
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemStack getGUIItem() {
        return gui_item;
    }

    public void setGUIItem(ItemStack gui_item) {
        this.gui_item = gui_item;
    }

    public Map<Integer, ItemStack> getItems() {
        return items;
    }

    public void setItems(Map<Integer, ItemStack> items) {
        this.items = items;
    }

}
