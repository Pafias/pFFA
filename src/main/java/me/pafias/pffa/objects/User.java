package me.pafias.pffa.objects;

import lombok.Data;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.LobbyManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

@Data
public class User {

    private final LobbyManager lobbyManager = pFFA.get().getSM().getLobbyManager();

    private final Player player;
    private UserData userData;

    private int currentKillstreak;

    private Kit lastKit;
    private Spawn lastSpawn;

    public long lastKillMillis = 0;

    private boolean spectating = false;

    public User(Player player, UserData userData) {
        this.player = player;
        this.userData = userData;
    }

    public boolean isInSpawn() {
        return lobbyManager.isInSpawn(player);
    }

    public void heal(boolean clearPotionEffects) {
        if (clearPotionEffects)
            for (final PotionEffect pe : player.getActivePotionEffects())
                player.removePotionEffect(pe.getType());
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(0);
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public String getName() {
        return player.getName();
    }

    public int getKills() {
        return userData.getFfaData().getKills();
    }

    public void addKill() {
        setKills(getKills() + 1);
        setCurrentKillstreak(getCurrentKillstreak() + 1);
        if (getCurrentKillstreak() > getBestKillstreak()) setBestKillstreak(getCurrentKillstreak());
    }

    public void setKills(int kills) {
        userData.getFfaData().setKills(kills);
    }

    public int getDeaths() {
        return userData.getFfaData().getDeaths();
    }

    public void addDeath() {
        setDeaths(getDeaths() + 1);
        setCurrentKillstreak(0);
    }

    public void setDeaths(int deaths) {
        userData.getFfaData().setDeaths(deaths);
    }

    public double getKDR() {
        return getKills() / (double) (getDeaths() == 0 ? 1 : getDeaths());
    }

    public int getBestKillstreak() {
        return userData.getFfaData().getKillstreak();
    }

    public void setBestKillstreak(int bestKillstreak) {
        userData.getFfaData().setKillstreak(bestKillstreak);
    }

}
