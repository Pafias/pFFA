package me.pafias.pffa.events;

import lombok.Getter;
import lombok.Setter;
import me.pafias.pffa.combatlog.CombatLog;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class CombatEndedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    private final CombatLog combatLog;

    @NotNull
    private final Player attacker, victim;

    public CombatEndedEvent(@NotNull CombatLog combatLog, @NotNull Player attacker, @NotNull Player victim) {
        this.combatLog = combatLog;
        this.attacker = attacker;
        this.victim = victim;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
