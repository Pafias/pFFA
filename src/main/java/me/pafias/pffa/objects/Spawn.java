package me.pafias.pffa.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class Spawn {

    private String name;

    @Nullable
    private String permission;

    private ItemStack guiItem;

    private Location location;

    private double playerDetectionRadius;

    public void teleport(Player player) {
        player.teleport(location);
    }

    /**
     * Checks if the spawn has a permission set.
     *
     * @return true if the spawn has a permission, false otherwise.
     */
    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

}
