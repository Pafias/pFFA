package me.pafias.pffa.objects;

import me.pafias.pffa.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpawnMenu extends GuiMenu {

    public SpawnMenu(Player player, int size) {
        super(player, CC.t("&6Spawn selection"), size);
        plugin.getSM().getSpawnManager().getSpawns().values().forEach(spawn -> getInventory().addItem(spawn.getGUIItem()));
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
