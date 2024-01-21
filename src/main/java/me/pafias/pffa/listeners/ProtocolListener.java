package me.pafias.pffa.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;

public class ProtocolListener extends PacketAdapter {

    private final pFFA pl;

    public ProtocolListener(pFFA pl) {
        super(pl, ListenerPriority.HIGH, PacketType.Play.Server.HURT_ANIMATION);
        this.pl = pl;
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        User user = pl.getSM().getUserManager().getUser(event.getPlayer());
        if (!user.getSettings().isOldDamageTilt()) return;
        PacketContainer packet = event.getPacket();
        packet.getFloat().write(0, -180f);
        event.setPacket(packet);
    }

}
