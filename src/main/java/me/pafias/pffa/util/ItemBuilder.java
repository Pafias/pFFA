package me.pafias.pffa.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    private Material material;
    private int amount = 1;
    private short data = -1;
    private String name;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;
    private ItemFlag[] itemflags;

    public ItemBuilder() {

    }

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder setData(short data) {
        this.data = data;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        this.lore = Arrays.asList(lore);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        if (enchantments == null)
            enchantments = new HashMap<>();
        enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder setFlags(ItemFlag... flags) {
        itemflags = flags;
        return this;
    }

    public ItemBuilder minimal() {
        setName("");
        setLore("");
        setFlags(ItemFlag.values());
        return this;
    }

    public ItemStack build() {
        ItemStack is = new ItemStack(material, amount);
        if (data != -1)
            is.setDurability(data);
        ItemMeta meta = is.getItemMeta();
        if (name != null)
            meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(lore);
        if (enchantments != null)
            enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
        if (itemflags != null)
            meta.addItemFlags(itemflags);
        is.setItemMeta(meta);
        return is;
    }

}
