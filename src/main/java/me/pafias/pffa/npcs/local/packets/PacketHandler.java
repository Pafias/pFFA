package me.pafias.pffa.npcs.local.packets;

import me.pafias.pffa.npcs.local.FakeNpc;
import me.pafias.pffa.npcs.local.wrapper.ItemSlot;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface PacketHandler {

    // Essential
    int getNextEntityId();

    void addToTab(Player player, FakeNpc npc);

    void spawnNpc(Player player, FakeNpc npc);

    void removeFromTab(Player player, FakeNpc npc);

    void destroyNpc(Player player, FakeNpc npc);

    // Extras

    void rotate(Player player, FakeNpc npc, float yaw, float pitch);

    void changeEquipment(Player player, FakeNpc npc, Map<ItemSlot, ItemStack> equipment);

    void teleport(Player player, FakeNpc npc, Location location);

}
