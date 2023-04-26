package me.pafias.pafiasffa.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Map;

public class Kit {

    private String name;
    private ItemStack gui_item;
    private Map<Integer, ItemStack> items;
    private Collection<PotionEffect> potionEffects;

    public Kit(String name, ItemStack gui_item, Map<Integer, ItemStack> items, Collection<PotionEffect> potionEffects) {
        this.name = name;
        this.gui_item = gui_item;
        this.items = items;
        this.potionEffects = potionEffects;
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

    public Map<Integer, ItemStack> getItems() {
        return items;
    }

    public Collection<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

}
