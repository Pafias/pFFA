package me.pafias.pffa.listeners.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import java.util.ArrayList;
import java.util.List;

public class ProtocolListener {

    public ProtocolListener() {
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21_4))
            listeners.add(new SprintglitchListener());

        for (final SimplePacketListenerAbstract listener : listeners)
            PacketEvents.getAPI().getEventManager().registerListener(listener);
    }

    private final List<SimplePacketListenerAbstract> listeners = new ArrayList<>();

    public void shutdown() {
        for (final SimplePacketListenerAbstract listener : listeners)
            PacketEvents.getAPI().getEventManager().unregisterListener(listener);
    }

}
