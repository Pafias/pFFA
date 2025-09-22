package me.pafias.pffa.npcs.local.packets;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class PacketHandler1_14 extends PacketHandler1_10 {

    public PacketHandler1_14() {
        SKIN_FLAGS_INDEX = 15;
    }

    @Override
    @SneakyThrows
    public int getNextEntityId() {
        Field field = null;
        for (Field f : entityClass.getDeclaredFields()) {
            if (f.getType().getName().endsWith("AtomicInteger")) {
                field = f;
                break;
            }
        }
        if (field == null) throw new IllegalStateException("Could not find AtomicInteger field in Entity class");
        field.setAccessible(true);
        Object atomicInteger = field.get(null);
        return (int) atomicInteger.getClass().getMethod("incrementAndGet").invoke(atomicInteger);
    }

}
