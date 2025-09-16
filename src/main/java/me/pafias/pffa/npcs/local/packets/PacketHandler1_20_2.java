package me.pafias.pffa.npcs.local.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.pafias.pffa.npcs.local.FakeNpc;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Optional;

public class PacketHandler1_20_2 extends PacketHandler1_19_3 {

    @Override
    public void spawnNpc(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSpawnEntity(
                npc.getEntityId(),
                Optional.of(npc.getProfile().getId()),
                EntityTypes.PLAYER,
                new Vector3d(
                        npc.getLocation().getX(),
                        npc.getLocation().getY(),
                        npc.getLocation().getZ()
                ),
                npc.getLocation().getPitch(),
                npc.getLocation().getYaw(),
                npc.getLocation().getYaw(),
                0,
                Optional.of(new Vector3d())
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityHeadLook(
                npc.getEntityId(), npc.getLocation().getYaw()
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityMetadata(
                npc.getEntityId(),
                Collections.singletonList(new EntityData<>(
                        17,
                        EntityDataTypes.BYTE,
                        (byte) 0x7F
                ))
        ));
    }

}
