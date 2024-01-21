package me.pafias.pffa.objects;

import java.util.UUID;

public class OfflineUser {

    private UUID uuid;
    private int kills;
    private int deaths;
    private int killstreak;

    public OfflineUser(UUID uuid, int kills, int deaths, int killstreak) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
        this.killstreak = killstreak;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getKillstreak() {
        return killstreak;
    }

}
