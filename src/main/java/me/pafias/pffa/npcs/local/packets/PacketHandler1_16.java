package me.pafias.pffa.npcs.local.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import lombok.SneakyThrows;
import me.pafias.pffa.npcs.local.FakeNpc;
import me.pafias.pffa.npcs.local.wrapper.ItemSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.stream.Collectors;

public class PacketHandler1_16 extends PacketHandler1_15 {

    public PacketHandler1_16() {
        SKIN_FLAGS_INDEX = 17;
    }

    @Override
    @SneakyThrows
    public void changeEquipment(Player player, FakeNpc npc, Map<ItemSlot, ItemStack> equipment) {
        if (equipment == null || equipment.isEmpty()) return;

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT, true);
        packet.getIntegers().write(0, npc.getEntityId());
        packet.getSlotStackPairLists().write(0, equipment.entrySet().stream()
                .map(entry -> new Pair<>(EnumWrappers.ItemSlot.valueOf(entry.getKey().name()), entry.getValue()))
                .collect(Collectors.toList()));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

}
