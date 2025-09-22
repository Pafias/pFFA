package me.pafias.pffa.listeners;

import me.pafias.pffa.pFFA;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class ProtectionListener1_8 extends ProtectionListener {

    public ProtectionListener1_8(pFFA plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItem(PlayerItemDamageEvent event) {
        if (!ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        event.setCancelled(true);
    }

}
