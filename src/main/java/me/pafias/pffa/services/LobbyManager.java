package me.pafias.pffa.services;

import lombok.Getter;
import me.pafias.pffa.LobbyDetectionMode;
import me.pafias.pffa.util.Serializer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class LobbyManager {

    public LobbyManager(FileConfiguration config) {
        ffaWorlds = config.getStringList("ffa_worlds");
        lobbyConfig = config.getConfigurationSection("lobby");
        lobbySpawn = Serializer.parseConfigLocation("lobby.spawn");
        lobbyDetectionMode = LobbyDetectionMode.parse(lobbyConfig.getString("detection_mode"));

        final String xBounds = lobbyConfig.getString("x_bounds");
        final double xMin = Double.parseDouble(xBounds.split(",")[0]);
        final double xMax = Double.parseDouble(xBounds.split(",")[1]);
        final String yBounds = lobbyConfig.getString("y_bounds");
        final double yMin = Double.parseDouble(yBounds.split(",")[0]);
        final double yMax = Double.parseDouble(yBounds.split(",")[1]);
        final String zBounds = lobbyConfig.getString("z_bounds");
        final double zMin = Double.parseDouble(zBounds.split(",")[0]);
        final double zMax = Double.parseDouble(zBounds.split(",")[1]);
        lobbyBounds = new double[][]{{xMin, xMax}, {yMin, yMax}, {zMin, zMax}};

        hRadiusSquared = Math.pow(lobbyConfig.getInt("h_radius"), 2);
        vRadius = lobbyConfig.getInt("v_radius");
    }

    @Getter
    private Location lobbySpawn;
    @Getter
    private List<String> ffaWorlds;
    private LobbyDetectionMode lobbyDetectionMode;
    private ConfigurationSection lobbyConfig;
    private double[][] lobbyBounds;
    private double hRadiusSquared;
    private int vRadius;

    public boolean isInSpawn(Player player) {
        if (lobbyDetectionMode == LobbyDetectionMode.OTHER) return false;
        if (lobbySpawn == null) return false;

        if (!player.getWorld().equals(lobbySpawn.getWorld())) return false;

        final Location playerLocation = player.getLocation();
        if (lobbyDetectionMode == LobbyDetectionMode.Y_COORD) {
            return playerLocation.getY() >= lobbySpawn.getY() - 1;
        } else if (lobbyDetectionMode == LobbyDetectionMode.BOUNDS) {
            final int x = playerLocation.getBlockX();
            final int y = playerLocation.getBlockY();
            final int z = playerLocation.getBlockZ();
            final double xMin = lobbyBounds[0][0];
            final double xMax = lobbyBounds[0][1];
            final double yMin = lobbyBounds[1][0];
            final double yMax = lobbyBounds[1][1];
            final double zMin = lobbyBounds[2][0];
            final double zMax = lobbyBounds[2][1];
            return (x > xMin && x < xMax) && (y > yMin && y < yMax) && (z > zMin && z < zMax);
        } else {
            final double distance = playerLocation.distanceSquared(lobbySpawn);
            final double yDiff = Math.abs(playerLocation.getY() - lobbySpawn.getY());
            return distance <= hRadiusSquared && yDiff <= vRadius;
        }
    }

}
