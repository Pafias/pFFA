package me.pafias.pffa.npcs.local.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import lombok.SneakyThrows;
import me.pafias.pffa.npcs.local.FakeNpc;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;

public class PacketHandler1_9 extends PacketHandler1_8 {

    protected static int SKIN_FLAGS_INDEX = 12;

    @Override
    @SneakyThrows
    public void spawnNpc(Player player, FakeNpc npc) {
        PacketContainer spawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPacket.getIntegers().write(0, npc.getEntityId());
        spawnPacket.getModifier().write(1, npc.getProfile().getId());
        spawnPacket.getDoubles().write(0, npc.getLocation().getX());
        spawnPacket.getDoubles().write(1, npc.getLocation().getY());
        spawnPacket.getDoubles().write(2, npc.getLocation().getZ());
        spawnPacket.getBytes().write(0, (byte) (npc.getLocation().getYaw() * (256.0F / 360.0F)));
        spawnPacket.getBytes().write(1, (byte) (npc.getLocation().getPitch() * (256.0F / 360.0F)));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);

        PacketContainer metadataPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, npc.getEntityId());
        metadataPacket.getWatchableCollectionModifier().write(0, Collections.singletonList(new WrappedWatchableObject(
                new WrappedDataWatcher.WrappedDataWatcherObject(
                        SKIN_FLAGS_INDEX,
                        WrappedDataWatcher.Registry.get(Byte.class)
                ),
                (byte) 0x7F // All skin layers
        )));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, metadataPacket);

        PacketContainer rotationPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        rotationPacket.getIntegers().write(0, npc.getEntityId());
        rotationPacket.getBytes().write(0, (byte) (npc.getLocation().getYaw() * (256.0F / 360.0F)));
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, rotationPacket);
    }

    @Override
    @SneakyThrows
    public void teleport(Player player, FakeNpc npc, Location location) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, npc.getEntityId());
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
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
