package me.pafias.pffa.npcs.local.wrapper;

import me.pafias.pffa.pFFA;

public enum ItemSlot {

    MAINHAND,
    OFFHAND,
    FEET,
    LEGS,
    CHEST,
    HEAD;

    public int toPacketSlot() {
        final double version = pFFA.get().parseVersion();
        if (version >= 9) {
            return this.ordinal();
        } else {
            // Account for OFFHAND which does not exist before 1.9
            return Math.max(0, this.ordinal() - 1);
        }
    }

}
