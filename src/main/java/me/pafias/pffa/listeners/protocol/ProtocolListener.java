package me.pafias.pffa.listeners.protocol;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import me.pafias.pffa.pFFA;

import java.util.HashSet;
import java.util.Set;

public class ProtocolListener {

    public ProtocolListener(pFFA plugin) {
        listeners.add(new DamageTiltListener(plugin));
        listeners.add(new SprintglitchListener(plugin));
    }

    private final Set<PacketAdapter> listeners = new HashSet<>();

    public void shutdown() {
        listeners
                .forEach(listener ->
                        ProtocolLibrary.getProtocolManager().removePacketListener(listener)
                );
    }

}
