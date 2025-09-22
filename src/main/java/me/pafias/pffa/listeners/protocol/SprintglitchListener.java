package me.pafias.pffa.listeners.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import me.pafias.pffa.pFFA;

import java.util.List;

public class SprintglitchListener extends PacketAdapter {

    public SprintglitchListener(pFFA plugin) {
        super(plugin, PacketType.Play.Server.UPDATE_ATTRIBUTES);
        if (plugin.parseVersion() >= 17) {
            maxHealthKey = "generic.max_health";
            movementSpeedKey = "generic.movement_speed";
        } else {
            maxHealthKey = "generic.maxHealth";
            movementSpeedKey = "generic.movementSpeed";
        }
    }

    private final String maxHealthKey;
    private final String movementSpeedKey;

    @Override
    public void onPacketSending(PacketEvent event) {
        final PacketContainer packet = event.getPacket();
        int i = 0;
        for (List<WrappedAttribute> list : packet.getAttributeCollectionModifier().getValues()) {
            if (list.stream().anyMatch(attribute -> attribute.getAttributeKey().equals(maxHealthKey))
                    && list.removeIf(attribute -> attribute.getAttributeKey().equals(movementSpeedKey)))
                packet.getAttributeCollectionModifier().write(i, list);
            i++;
        }
    }

}
