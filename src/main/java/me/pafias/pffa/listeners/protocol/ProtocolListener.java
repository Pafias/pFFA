package me.pafias.pffa.listeners.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import me.pafias.pffa.pFFA;

import java.util.HashSet;
import java.util.Set;

public class ProtocolListener {

    public ProtocolListener(pFFA plugin) {
        if (Double.parseDouble(plugin.getServer().getMinecraftVersion().split("\\.", 2)[1]) < 21.4)
            listeners.add(new SprintglitchListener());

        for (final SimplePacketListenerAbstract listener : listeners)
            PacketEvents.getAPI().getEventManager().registerListener(listener);
    }

    private final Set<SimplePacketListenerAbstract> listeners = new HashSet<>();

    public void shutdown() {
        for (final SimplePacketListenerAbstract listener : listeners)
            PacketEvents.getAPI().getEventManager().unregisterListener(listener);
    }

}
