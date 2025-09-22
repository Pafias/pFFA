package me.pafias.pffa.objects.gui;

import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.putils.InventoryUtils;
import me.pafias.putils.LCC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpawnMenu extends GuiMenu {

    private final User user;
    private final Kit kit;

    public SpawnMenu(User user, Kit kit, Collection<Spawn> spawns) {
        super(user.getPlayer(), LCC.t("&6Spawn selection"), InventoryUtils.parseSizeToInvSize(spawns.size()));

        this.user = user;
        this.kit = kit;

        int slot = 0;
        for (final Spawn spawn : spawns) {
            final ItemStack guiItem = spawn.getGuiItem().clone();
            final ItemMeta meta = guiItem.getItemMeta();

            // Set the nearby players in the lore, if applicable
            if (meta != null && meta.hasLore()) {
                final Predicate<Player> predicate = e -> player.canSee(e) && !plugin.getSM().getUserManager().getUser(e).isInSpawn();
                final Collection<Player> nearby;
                nearby = spawn.getLocation().getWorld().getEntitiesByClass(Player.class)
                        .stream()
                        .filter(predicate)
                        .filter(e -> e.getLocation().distanceSquared(spawn.getLocation()) <= Math.exp(spawn.getPlayerDetectionRadius() * 2))
                        .collect(Collectors.toList());
                final int nearbyCount = (int) nearby.stream().filter(predicate).count();

                final List<String> lore = meta.getLore();
                assert lore != null;

                final List<String> newLore = lore.stream()
                        .map(line -> line.replace("{nearby}", String.valueOf(nearbyCount)))
                        .collect(Collectors.toList());

                meta.setLore(newLore);
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
        user.heal(false);
        spawn.teleport(user.getPlayer());
        if (kit != null)
            kit.give(user.getPlayer());
        user.setLastSpawn(spawn);
        user.setLastKit(kit);
    }

}
