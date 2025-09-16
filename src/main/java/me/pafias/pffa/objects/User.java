package me.pafias.pffa.objects;

import lombok.Data;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.Serializer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

@Data
public class User {

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
        final ConfigurationSection config = pFFA.get().getConfig().getConfigurationSection("lobby");
        if (config == null) return false;
        final String detectionMode = config.getString("detection_mode");
        if (detectionMode == null || detectionMode.equalsIgnoreCase("none")) return false;

        final Location lobbySpawn = Serializer.parseConfigLocation("lobby.spawn");
        if (lobbySpawn == null) return false;

        if (!player.getWorld().equals(lobbySpawn.getWorld())) return false;

        if (detectionMode.equalsIgnoreCase("ycoord")) {
            return player.getLocation().getY() >= lobbySpawn.getY() - 1;
        } else if (detectionMode.equalsIgnoreCase("bounds")) {
            final int x = player.getLocation().getBlockX();
            final int y = player.getLocation().getBlockY();
            final int z = player.getLocation().getBlockZ();
            final String xBounds = config.getString("x_bounds");
            final double xMin = Double.parseDouble(xBounds.split(",")[0]);
            final double xMax = Double.parseDouble(xBounds.split(",")[1]);
            final String yBounds = config.getString("y_bounds");
            final double yMin = Double.parseDouble(yBounds.split(",")[0]);
            final double yMax = Double.parseDouble(yBounds.split(",")[1]);
            final String zBounds = config.getString("z_bounds");
            final double zMin = Double.parseDouble(zBounds.split(",")[0]);
            final double zMax = Double.parseDouble(zBounds.split(",")[1]);
            return (x > xMin && x < xMax) && (y > yMin && y < yMax) && (z > zMin && z < zMax);
        } else {
            final double distance = player.getLocation().distance(lobbySpawn);
            final int hRadius = config.getInt("h_radius");
            final int vRadius = config.getInt("v_radius");
            final double yDiff = Math.abs(player.getLocation().getY() - lobbySpawn.getY());
            return distance <= hRadius && yDiff <= vRadius;
        }
    }

    public void heal(boolean clearPotionEffects) {
        if (clearPotionEffects)
            for (PotionEffect pe : player.getActivePotionEffects())
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
