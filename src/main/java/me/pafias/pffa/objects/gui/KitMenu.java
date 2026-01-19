package me.pafias.pffa.objects.gui;

import lombok.Getter;
import lombok.Setter;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.putils.CC;
import me.pafias.putils.InventoryUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KitMenu extends GuiMenu {

    private final User user;
    @Getter
    @Setter
    private Spawn spawn;

    public KitMenu(User user, Spawn spawn, Collection<Kit> kits) {
        super(user.getPlayer(), CC.t("&6Kit selection"), InventoryUtils.parseSizeToInvSize(kits.size()));
        this.user = user;
        this.spawn = spawn;
        update(kits);
    }

    public void update(final Collection<Kit> kits) {
        int slot = 0;
        for (final Kit kit : kits) {
            mapping.put(slot, kit);
            getInventory().setItem(slot, kit.getGuiItem());
            slot++;
        }
    }

    private final Map<Integer, Kit> mapping = new HashMap<>();

    @Override
    public void clickHandler(@Nullable ItemStack item, int slot) {
        if (item == null) {
            setCloseOnClick(false);
            return;
        }
        final Kit kit = mapping.get(slot);
        if (kit == null) {
            setCloseOnClick(false);
            return;
        }
        if (kit.hasPermission() && !player.hasPermission(kit.getPermission())) {
            player.sendMessage(CC.t("&cYou don't have permission to use this kit!"));
            setCloseOnClick(false);
            return;
        }
        user.heal(false);
        kit.give(user.getPlayer());
        if (spawn != null)
            spawn.teleport(user.getPlayer());
        user.setLastSpawn(spawn);
        user.setLastKit(kit);
    }

}
