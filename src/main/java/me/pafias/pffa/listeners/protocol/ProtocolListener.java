package me.pafias.pffa.listeners.protocol;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import me.pafias.pffa.pFFA;

import java.util.HashSet;
import java.util.Set;

public class ProtocolListener {

    public ProtocolListener(pFFA plugin) {
        if (plugin.parseVersion() < 21.4)
            listeners.add(new SprintglitchListener(plugin));

        for (final PacketAdapter listener : listeners)
            ProtocolLibrary.getProtocolManager().addPacketListener(listener);
    }

    private final Set<PacketAdapter> listeners = new HashSet<>();

    public void shutdown() {
        for (final PacketAdapter listener : listeners)
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
    }

}
