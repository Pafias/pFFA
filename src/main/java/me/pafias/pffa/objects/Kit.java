package me.pafias.pffa.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

@Getter
@AllArgsConstructor
public class Kit {

    @Setter
    private String name;

    @Nullable
    private final String permission;

    private final ItemStack guiItem;
    private final Map<Integer, ItemStack> items;
    private final Collection<PotionEffect> potionEffects;

    public void give(Player player) {
        player.getInventory().clear();
        for (final Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue());
        }
        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect, true);
        }
    }

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    public ItemStack[] getArmor() {
        return new ItemStack[]{
                getItems().getOrDefault(39, null), // Helmet
                getItems().getOrDefault(38, null), // Chestplate
                getItems().getOrDefault(37, null), // Leggings
                getItems().getOrDefault(36, null)  // Boots
        };
    }

}
