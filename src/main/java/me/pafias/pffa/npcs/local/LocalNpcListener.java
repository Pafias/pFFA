package me.pafias.pffa.npcs.local;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.pafias.pffa.pFFA;
import me.pafias.putils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LocalNpcListener extends SimplePacketListenerAbstract implements Listener {

    private final pFFA plugin;

    private final LocalNpcManager npcManager;

    public LocalNpcListener(pFFA plugin, LocalNpcManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (final FakeNpc npc : npcManager.getNpcs().values())
            npc.getViewers().remove(event.getPlayer());
    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        final Player player = event.getPlayer();
        if (!plugin.getFfaWorlds().contains(player.getWorld().getName())) return;

        final WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);

        final FakeNpc npc = npcManager.getNpcs().get(packet.getEntityId());
        if (npc != null) {
            final boolean leftClick = packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK;
            Tasks.runSync(() -> npcManager.trigger(null, npc.getProfile().getName(), plugin.getSM().getUserManager().getUser(player), leftClick));
        }

    }

}
