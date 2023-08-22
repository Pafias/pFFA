package me.pafias.pafiasffa.objects;

import me.pafias.pafiasffa.services.ArmorstandManager;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpawnMenu extends GuiMenu {

    private Kit kit;

    public SpawnMenu(Player player, int size, Kit kitToGive) {
        super(player, CC.t("&6Spawn selection"), size);
        this.kit = kitToGive;
        plugin.getSM().getSpawnManager().getSpawns().values().forEach(spawn -> getInventory().addItem(spawn.getGUIItem()));
    }

    @Override
    public void clickHandler(ItemStack item, int slot) {
        Spawn spawn = plugin.getSM().getSpawnManager().getSpawn(item);
        if (spawn == null) {
            setCloseOnClick(false);
            return;
        }
        spawn.teleport(player);
        ArmorstandManager.handlePlayer(player);
        if (kit != null)
            kit.give(player);
    }

}
