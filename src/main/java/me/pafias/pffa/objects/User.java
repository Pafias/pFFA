package me.pafias.pffa.objects;

import com.google.gson.JsonParser;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.UserManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User {

    private final pFFA plugin = pFFA.get();
    private final UserManager manager = plugin.getSM().getUserManager();

    private final Player player;
    private final UserConfig config;

    private int kills;
    private int deaths;

    private int killstreak;
    private int bestKillstreak;

    private Kit lastKit = plugin.getSM().getKitManager().getDefaultKit();
    private Spawn lastSpawn = plugin.getSM().getSpawnManager().getDefaultSpawn();

    public long lastKillMillis = 0;

    private UserSettings settings = new UserSettings();

    public User(Player player) {
        this.player = player;
        config = new UserConfig(player.getUniqueId());
        config.get("kills", "deaths", "killstreak").thenAccept(map -> {
            kills = (int) map.get("kills");
            deaths = (int) map.get("deaths");
            bestKillstreak = (int) map.get("killstreak");
        });
        config.get("settings").thenAccept(map -> settings = new UserSettings(JsonParser.parseString((String) map.get("settings")).getAsJsonObject()));
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUUID() {
        return player.getUniqueId();
    }

    public String getName() {
        return player.getName();
    }

    public UserConfig getConfig() {
        return config;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        setKills(getKills() + 1);
        setKillstreak(getCurrentKillstreak() + 1);
        if (getCurrentKillstreak() > getBestKillstreak()) setBestKillstreak(getCurrentKillstreak());
    }

    public void setKills(int kills) {
        this.kills = kills;
        manager.queueDataSave(this, false);
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        setDeaths(getDeaths() + 1);
        setKillstreak(0);
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
        manager.queueDataSave(this, false);
    }

    public double getKDR() {
        return kills / (double) (deaths == 0 ? 1 : deaths);
    }

    public int getCurrentKillstreak() {
        return killstreak;
    }

    public void setKillstreak(int killstreak) {
        this.killstreak = killstreak;
    }

    public int getBestKillstreak() {
        return bestKillstreak;
    }

    public void setBestKillstreak(int bestKillstreak) {
        this.bestKillstreak = bestKillstreak;
        manager.queueDataSave(this, false);
    }

    public Kit getLastKit() {
        return lastKit;
    }

    public void setLastKit(Kit lastKit) {
        this.lastKit = lastKit;
    }

    public Spawn getLastSpawn() {
        return lastSpawn;
    }

    public void setLastSpawn(Spawn lastSpawn) {
        this.lastSpawn = lastSpawn;
    }

    public UserSettings getSettings() {
        return settings;
    }

    public boolean isInFFAWorld() {
        return plugin.getSM().getVariables().ffaWorlds.contains(player.getLocation().getWorld().getName());
    }

    public boolean isInSpawn() {
        if (plugin.getSM().getVariables().lobbyDetection.equalsIgnoreCase("ycoord")) {
            return player.getLocation().getY() >= plugin.getSM().getVariables().lobby.getY() - 1;
        } else if (plugin.getSM().getVariables().lobbyDetection.equalsIgnoreCase("bounds")) {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();
            int xS = plugin.getSM().getVariables().lobby.getBlockX();
            int yS = plugin.getSM().getVariables().lobby.getBlockY();
            int zS = plugin.getSM().getVariables().lobby.getBlockZ();
            String xbounds = plugin.getSM().getVariables().lobbyXBounds;
            double xMin = Double.parseDouble(xbounds.split(",")[0]);
            double xMax = Double.parseDouble(xbounds.split(",")[1]);
            String ybounds = plugin.getSM().getVariables().lobbyYBounds;
            double yMin = Double.parseDouble(ybounds.split(",")[0]);
            double yMax = Double.parseDouble(ybounds.split(",")[1]);
            String zbounds = plugin.getSM().getVariables().lobbyZBounds;
            double zMin = Double.parseDouble(zbounds.split(",")[0]);
            double zMax = Double.parseDouble(zbounds.split(",")[1]);
            return (x > xMin && x < xMax) && (y > yMin && y < yMax) && (z > zMin && z < zMax);
        } else {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();
            int xS = plugin.getSM().getVariables().lobby.getBlockX();
            int yS = plugin.getSM().getVariables().lobby.getBlockY();
            int zS = plugin.getSM().getVariables().lobby.getBlockZ();
            double hrS = plugin.getSM().getVariables().lobbyHRadius;
            double vrS = plugin.getSM().getVariables().lobbyVRadius;
            return (x > xS - hrS && x < xS + hrS) && (y > yS - vrS && y < yS + vrS) && (z > zS - hrS && z < zS + hrS);
            // return Math.abs(x - xS) < hrS && Math.abs(z - zS) < hrS && Math.abs(y - yS) < vrS;
            // return (y == yS || y == yS + vrS || y == yS - 1) && ((x == xS || x == xS + hrS || x == xS - hrS) && (z == zS || z == zS + hrS || z == zS - hrS));
        }
    }

    public void heal(boolean clearPotionEffects) {
        if (clearPotionEffects)
            player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(0);
    }

}
