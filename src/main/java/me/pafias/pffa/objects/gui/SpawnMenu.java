package me.pafias.pffa.objects.gui;

import lombok.Getter;
import lombok.Setter;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.putils.CC;
import me.pafias.putils.InventoryUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnMenu extends GuiMenu {

    private final User user;
    @Getter
    @Setter
    private Kit kit;
    private final Collection<Spawn> spawns;

    public SpawnMenu(User user, Kit kit, Collection<Spawn> spawns) {
        super(user.getPlayer(), CC.t("&6Spawn selection"), InventoryUtils.parseSizeToInvSize(spawns.size()));
        this.user = user;
        this.kit = kit;
        this.spawns = spawns;
        update(spawns);
    }

    public void update(final Collection<Spawn> spawns) {
        int slot = 0;
        for (final Spawn spawn : spawns) {
            final ItemStack guiItem = spawn.getGuiItem().clone();
            final ItemMeta meta = guiItem.getItemMeta();

            // Set the nearby players in the lore, if applicable
            if (meta != null && meta.hasLore()) {
                int nearby = (int) spawn.getNearbyPlayers().stream().filter(p -> player.canSee(p)).count();

                final TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
                        .matchLiteral("{nearby}")
                        .replacement(String.valueOf(nearby))
                        .build();

                final List<Component> lore = meta.lore();
                assert lore != null;

                final List<Component> newLore = lore.stream()
                        .map(line -> line.replaceText(replacementConfig))
                        .toList();

                meta.lore(newLore);
                guiItem.setItemMeta(meta);
            }

            mapping.put(slot, spawn);
            getInventory().setItem(slot, guiItem);
            slot++;
        }
    }

    private final Map<Integer, Spawn> mapping = new HashMap<>();

    @Override
    public void clickHandler(@Nullable ItemStack item, int slot) {
        if (item == null) {
            setCloseOnClick(false);
            return;
        }
        final Spawn spawn = mapping.get(slot);
        if (spawn == null) {
            setCloseOnClick(false);
            return;
        }
        if (spawn.hasPermission() && !player.hasPermission(spawn.getPermission())) {
            player.sendMessage(CC.t("&cYou don't have permission to spawn here!"));
            setCloseOnClick(false);
            return;
        }
        user.heal(false);
        spawn.teleport(user.getPlayer());
        if (kit != null)
            kit.give(user.getPlayer());
        user.setLastSpawn(spawn);
        user.setLastKit(kit);
    }

}
