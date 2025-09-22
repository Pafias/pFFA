package me.pafias.pffa.npcs.local.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import lombok.SneakyThrows;
import me.pafias.pffa.npcs.local.FakeNpc;
import me.pafias.pffa.npcs.local.wrapper.ItemSlot;
import me.pafias.pffa.util.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;

public class PacketHandler1_8 extends PacketHandler1_7 {

    @SneakyThrows
    public PacketHandler1_8() {
        super();
        try {
            enumGamemodeClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".WorldSettings$EnumGamemode");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    @SneakyThrows
    public void addToTab(Player player, FakeNpc npc) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(new PlayerInfoData(
                WrappedGameProfile.fromHandle(npc.getProfile().getHandle()),
                0, // Ping
                EnumWrappers.NativeGameMode.CREATIVE,
                WrappedChatComponent.fromText(npc.getNametag())
        )));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    @Override
    @SneakyThrows
    public void spawnNpc(Player player, FakeNpc npc) {
        PacketContainer spawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPacket.getIntegers().write(0, npc.getEntityId());
        spawnPacket.getModifier().write(1, npc.getProfile().getId());
        spawnPacket.getIntegers().write(1, (int) Math.floor(npc.getLocation().getX() * 32));
        spawnPacket.getIntegers().write(2, (int) Math.floor(npc.getLocation().getY() * 32));
        spawnPacket.getIntegers().write(3, (int) Math.floor(npc.getLocation().getZ() * 32));
        spawnPacket.getBytes().write(0, (byte) (npc.getLocation().getYaw() * (256.0F / 360.0F)));
        spawnPacket.getBytes().write(1, (byte) (npc.getLocation().getPitch() * (256.0F / 360.0F)));
        spawnPacket.getIntegers().write(0, 0); // Current item (short)
        spawnPacket.getDataWatcherModifier().writeDefaults(); // Metadata defaults

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);

        PacketContainer metadataPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, npc.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(10, (byte) 0x7F); // All skin layers
        metadataPacket.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, metadataPacket);

        PacketContainer rotationPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        rotationPacket.getIntegers().write(0, npc.getEntityId());
        rotationPacket.getBytes().write(0, (byte) (npc.getLocation().getYaw() * (256.0F / 360.0F)));

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, rotationPacket);
    }

    @Override
    @SneakyThrows
    public void removeFromTab(Player player, FakeNpc npc) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(new PlayerInfoData(
                WrappedGameProfile.fromHandle(npc.getProfile().getHandle()),
                0, // Ping
                EnumWrappers.NativeGameMode.CREATIVE,
                WrappedChatComponent.fromText(npc.getNametag())
        )));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    @Override
    @SneakyThrows
    public void destroyNpc(Player player, FakeNpc npc) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntegerArrays().write(0, new int[]{npc.getEntityId()});
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    @Override
    @SneakyThrows
    public void rotate(Player player, FakeNpc npc, float yaw, float pitch) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, npc.getEntityId());
        packet.getBytes().write(0, (byte) (yaw * (256.0F / 360.0F)));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

        PacketContainer packet2 = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_LOOK);
        packet2.getIntegers().write(0, npc.getEntityId());
        packet2.getBytes().write(0, (byte) (yaw * (256.0F / 360.0F)));
        packet2.getBytes().write(1, (byte) (pitch * (256.0F / 360.0F)));
        packet2.getBooleans().write(0, true); // On ground
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet2);
    }

    @Override
    @SneakyThrows
    public void changeEquipment(Player player, FakeNpc npc, Map<ItemSlot, ItemStack> equipment) {
        if (equipment == null || equipment.isEmpty()) return;

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, npc.getEntityId());
        Map.Entry<ItemSlot, ItemStack> pair = equipment.entrySet().iterator().next();
        packet.getItemSlots().write(0, EnumWrappers.ItemSlot.valueOf(pair.getKey().name()));
        packet.getItemModifier().write(0, pair.getValue());
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    @Override
    @SneakyThrows
    public void teleport(Player player, FakeNpc npc, Location location) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, npc.getEntityId());
        packet.getIntegers().write(1, (int) Math.floor(location.getX() * 32));
        packet.getIntegers().write(2, (int) Math.floor(location.getY() * 32));
        packet.getIntegers().write(3, (int) Math.floor(location.getZ() * 32));
        packet.getBytes().write(0, (byte) (location.getYaw() * (256.0F / 360.0F)));
        packet.getBytes().write(1, (byte) (location.getPitch() * (256.0F / 360.0F)));
        packet.getBooleans().write(0, true); // On ground
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

        PacketContainer packet2 = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet2.getIntegers().write(0, npc.getEntityId());
        packet2.getBytes().write(0, (byte) (location.getYaw() * (256.0F / 360.0F)));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet2);
    }

}
