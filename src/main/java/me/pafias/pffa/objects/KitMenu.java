package me.pafias.pffa.objects;

import me.pafias.pffa.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitMenu extends GuiMenu {

    public KitMenu(Player player, int size) {
        super(player, CC.t("&6Kit selection"), size);
        plugin.getSM().getKitManager().getKits().values().forEach(kit -> getInventory().addItem(kit.getGUIItem()));
    }

    public void open(ClickInteraction interaction) {
        clickInteraction = interaction;
        open();
    }

    private ClickInteraction clickInteraction;

    @Override
    public void clickHandler(ItemStack item, int slot) {
        clickInteraction.clickHandler(item, slot);
    }

    public interface ClickInteraction {
        void clickHandler(ItemStack item, int slot);
    }

}
