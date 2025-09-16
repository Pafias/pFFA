package me.pafias.pffa.listeners.protocol;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

import java.util.ArrayList;
import java.util.List;

public class SprintglitchListener extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Server.UPDATE_ATTRIBUTES)) return;
        final WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(event);
        final List<WrapperPlayServerUpdateAttributes.Property> list = new ArrayList<>(packet.getProperties());
        if (list.stream().anyMatch(attribute -> attribute.getAttribute().equals(Attributes.MAX_HEALTH))
                && list.removeIf(attribute -> attribute.getAttribute().equals(Attributes.MOVEMENT_SPEED)))
            packet.setProperties(list);
    }

}
