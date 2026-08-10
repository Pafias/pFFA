package me.pafias.pffa.npcs.local.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import me.pafias.pffa.npcs.local.FakeNpc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketHandler1_17 implements PacketHandler {

    @Override
    public void addToTab(Player player, FakeNpc npc) {
        WrapperPlayServerPlayerInfo.PlayerData playerData = new WrapperPlayServerPlayerInfo.PlayerData(
                npc.getNametag(),
                new UserProfile(npc.getProfile().getId(),
                        npc.getProfile().getName(),
                        npc.getProfile().getProperties()
                                .stream()
                                .map(property -> new TextureProperty(property.getName(), property.getValue(), property.getSignature()))
                                .toList()),
                GameMode.CREATIVE,
                null,
                1
        );
        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                Collections.singletonList(playerData)
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    @Override
    public void spawnNpc(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSpawnPlayer(
                npc.getEntityId(),
                npc.getProfile().getId(),
                new Vector3d(
                        npc.getLocation().getX(),
                        npc.getLocation().getY(),
                        npc.getLocation().getZ()
                ),
                npc.getLocation().getYaw(),
                npc.getLocation().getPitch(),
                Collections.emptyList()
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityHeadLook(
                npc.getEntityId(),
                npc.getLocation().getYaw()
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

    @Override
    public void removeFromTab(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER,
                new WrapperPlayServerPlayerInfo.PlayerData(null,
                        new UserProfile(npc.getProfile().getId(), null),
                        null,
                        -1
                )
        ));
    }

    @Override
    public void destroyNpc(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerDestroyEntities(
                npc.getEntityId()
        ));
    }

    @Override
    public void rotate(Player player, FakeNpc npc, float yaw, float pitch) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityHeadLook(
                npc.getEntityId(), yaw
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityRotation(
                npc.getEntityId(), yaw, pitch, true
        ));
    }

    @Override
    public void changeEquipment(Player player, FakeNpc npc, Equipment... equipment) {
        if (equipment == null || equipment.length == 0) return;
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityEquipment(
                npc.getEntityId(), Arrays.asList(equipment)
        ));
    }

    @Override
    public void teleport(Player player, FakeNpc npc, Location location) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityTeleport(
                npc.getEntityId(),
                new Vector3d(
                        location.getX(),
                        location.getY(),
                        location.getZ()
                ),
                location.getYaw(),
                location.getPitch(),
                true
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityHeadLook(
                npc.getEntityId(),
                location.getYaw()
        ));
    }

    /**
     * The nametag is rendered by a separate invisible armor stand riding along with the NPC, rather than the
     * NPC's own player entity because vanilla does not support colored and long nametags.
     * It's spawned with a small upward nudge for readability above the NPC's own nameplate.
     */
    @Override
    public void spawnNametag(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerSpawnEntity(
                npc.getNametagEntityId(),
                Optional.of(UUID.randomUUID()),
                EntityTypes.ARMOR_STAND,
                new Vector3d(
                        npc.getLocation().getX(),
                        npc.getLocation().getY() + 0.88,
                        npc.getLocation().getZ()
                ),
                0, 0, 0,
                0,
                Optional.empty()
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityMetadata(
                npc.getNametagEntityId(),
                List.of(
                        new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20), // invisible
                        new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(npc.getNametag())),
                        new EntityData<>(3, EntityDataTypes.BOOLEAN, true), // custom name visible
                        new EntityData<>(5, EntityDataTypes.BOOLEAN, true), // no gravity
                        new EntityData<>(15, EntityDataTypes.BYTE, (byte) (0x01 | 0x08)) // small, no base plate
                )
        ));
    }

    @Override
    public void destroyNametag(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerDestroyEntities(
                npc.getNametagEntityId()
        ));
    }

    /**
     * An empty profile name leaves a small empty nameplate background rendered,
     * so we'll send a scoreboard team packet with nametag visibility NEVER
     * to fully suppress the native nameplate, leaving only the custom nametag visible.
     */
    @Override
    public void hideNativeNameplate(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerTeams(
                "pffa_npc_" + npc.getEntityId(),
                WrapperPlayServerTeams.TeamMode.CREATE,
                new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                        Component.empty(),
                        null,
                        null,
                        WrapperPlayServerTeams.NameTagVisibility.NEVER,
                        WrapperPlayServerTeams.CollisionRule.NEVER,
                        NamedTextColor.WHITE,
                        WrapperPlayServerTeams.OptionData.NONE
                ),
                npc.getProfile().getName()
        ));
    }

    @Override
    public void showNativeNameplate(Player player, FakeNpc npc) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerTeams(
                "pffa_npc_" + npc.getEntityId(),
                WrapperPlayServerTeams.TeamMode.REMOVE,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null
        ));
    }

}
