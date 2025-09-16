package me.pafias.pffa.npcs.local.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import me.pafias.pffa.npcs.local.FakeNpc;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PacketHandler1_21_3 extends PacketHandler1_20_2 {

    @Override
    public void teleport(Player player, FakeNpc npc, Location location) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityTeleport(
                npc.getEntityId(),
                new EntityPositionData(
                        new Vector3d(
                                location.getX(),
                                location.getY(),
                                location.getZ()
                        ),
                        new Vector3d(0, 0, 0),
                        location.getYaw(),
                        location.getPitch()
                ),
                RelativeFlag.NONE,
                false
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityHeadLook(
                npc.getEntityId(),
                location.getYaw()
        ));
    }

}
