package me.pafias.pafiasffa.objects;

import me.pafias.pafiasffa.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitMenu extends GuiMenu {

    private Spawn spawn;

    public KitMenu(Player player, int size, Spawn spawnToTp) {
        super(player, CC.t("&6Kit selection"), size);
        this.spawn = spawnToTp;
        plugin.getSM().getKitManager().getKits().forEach(kit -> getInventory().addItem(kit.getGUIItem()));
    }

    @Override
    public void clickHandler(ItemStack item, int slot) {
        Kit kit = plugin.getSM().getKitManager().getKit(item);
        if (kit == null) {
            setCloseOnClick(false);
            return;
        }
        kit.give(player);
        if (spawn != null)
            spawn.teleport(player);
    }

}
