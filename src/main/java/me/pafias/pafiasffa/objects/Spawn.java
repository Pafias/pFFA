package me.pafias.pafiasffa.objects;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Spawn {

    private String name;
    private ItemStack gui_item;
    private Location location;

    public Spawn(String name, ItemStack gui_item, Location location) {
        this.name = name;
        this.gui_item = gui_item;
        this.location = location;
    }

    public void teleport(Player player) {
        player.teleport(location);
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
