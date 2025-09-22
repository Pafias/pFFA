package me.pafias.pffa.npcs.local;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import me.pafias.pffa.pFFA;
import me.pafias.putils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;

public class LocalNpcListener extends PacketAdapter implements Listener {

    private final pFFA plugin;

    private final LocalNpcManager npcManager;

    public LocalNpcListener(pFFA plugin, LocalNpcManager npcManager) {
        super(plugin, PacketType.Play.Client.USE_ENTITY);
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (final FakeNpc npc : npcManager.getNpcs().values())
            npc.getViewers().remove(event.getPlayer());
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        final Player player = event.getPlayer();
        if (!plugin.getConfig().getStringList("ffa_worlds").contains(player.getWorld().getName())) return;

        final PacketContainer packet = event.getPacket();

        int entityId = packet.getIntegers().read(0);
        boolean leftClick;
        if (plugin.parseVersion() < 8) {
            Object handle = packet.getHandle();
            try {
                Class<?> packetClass = handle.getClass();
                Field actionField = packetClass.getDeclaredField("action");
                actionField.setAccessible(true);
                Object action = actionField.get(handle);
                Enum<?> enumAction = (Enum<?>) action;
                leftClick = enumAction.name().equals("ATTACK");
            } catch (Exception ex) {
                ex.printStackTrace();
                leftClick = true;
            }
        } else
            try {
                leftClick = packet.getEnumEntityUseActions().read(0) == WrappedEnumEntityUseAction.attack();
            } catch (Throwable t) {
                leftClick = packet.getSpecificModifier(Enum.class).read(0).name().equals("ATTACK");
            }

        for (FakeNpc npc : npcManager.getNpcs().values()) {
            if (npc.getEntityId() == entityId) {
                boolean finalLeftClick = leftClick;
                Tasks.runSync(() -> npcManager.trigger(null, npc.getProfile().getName(), plugin.getSM().getUserManager().getUser(player), finalLeftClick));
            }
        }
    }

}
