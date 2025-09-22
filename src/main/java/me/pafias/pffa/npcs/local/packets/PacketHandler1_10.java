package me.pafias.pffa.npcs.local.packets;

import lombok.SneakyThrows;
import me.pafias.pffa.util.Reflection;

public class PacketHandler1_10 extends PacketHandler1_9 {

    @SneakyThrows
    public PacketHandler1_10() {
        super();
        try {
            enumGamemodeClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".EnumGamemode");
        } catch (ClassNotFoundException ignored) {
        }

        SKIN_FLAGS_INDEX = 13;
    }

}
