package me.pafias.pffa.listeners.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import me.pafias.pffa.pFFA;

import java.util.List;

public class SprintglitchListener extends PacketAdapter {

    private final String maxHealthKey;
    private final String movementSpeedKey;

    public SprintglitchListener(pFFA pl) {
        super(pl, ListenerPriority.NORMAL, PacketType.Play.Server.UPDATE_ATTRIBUTES);
        if (pl.serverVersion() >= 17) {
            maxHealthKey = "generic.max_health";
            movementSpeedKey = "generic.movement_speed";
        } else {
            maxHealthKey = "generic.maxHealth";
            movementSpeedKey = "generic.movementSpeed";
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        int i = 0;
        for (List<WrappedAttribute> list : packet.getAttributeCollectionModifier().getValues()) {
            if (list.stream().anyMatch(attribute -> attribute.getAttributeKey().equals(maxHealthKey))
                    && list.removeIf(attribute -> attribute.getAttributeKey().equals(movementSpeedKey)))
                packet.getAttributeCollectionModifier().write(i, list);
            i++;
        }
    }

}
