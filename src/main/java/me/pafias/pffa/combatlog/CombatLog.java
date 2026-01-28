package me.pafias.pffa.combatlog;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

public class CombatLog {

    @Getter
    private final Player attacker;

    @Getter
    private final Player victim;

    private long endTime;

    @Getter
    private final int durationSeconds;

    public CombatLog(Player attacker, Player victim, int durationSeconds) {
        this.attacker = attacker;
        this.victim = victim;
        this.durationSeconds = durationSeconds;
        reset(durationSeconds);
    }

    public List<Player> getPlayers() {
        return ImmutableList.of(attacker, victim);
    }

    public boolean isExpired() {
        return getTimeLeftMillis() <= 0;
    }

    public long getTimeLeftMillis() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public int getTimeLeftSeconds() {
        return (int) Math.max(0, getTimeLeftMillis() / 1000);
    }

    public void reset(int durationSeconds) {
        endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
    }

}
