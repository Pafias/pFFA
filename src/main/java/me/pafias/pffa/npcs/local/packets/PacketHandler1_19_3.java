package me.pafias.pffa.npcs.local.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import me.pafias.pffa.npcs.local.FakeNpc;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class PacketHandler1_19_3 extends PacketHandler1_17 {

    @Override
    public void addToTab(Player player, FakeNpc npc) {
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                new UserProfile(
                        npc.getProfile().getId(),
                        npc.getProfile().getName(),
                        npc.getProfile().getProperties().stream()
                                .map(property -> new TextureProperty(property.getName(), property.getValue(), property.getSignature()))
                                .toList()
                ),
                false,
                1,
                GameMode.CREATIVE,
                npc.getNametag(),
                null
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(
                        WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
                ),
                info,
                info
        ));
    }

    @Override
    public void removeFromTab(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerPlayerInfoRemove(
                npc.getProfile().getId()
        ));
    }

}
