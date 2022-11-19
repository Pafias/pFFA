package me.pafias.pafiasffa.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIExpansion extends PlaceholderExpansion {

    private final PafiasFFA plugin;

    public PAPIExpansion(PafiasFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ffa";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Pafias";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        User user = plugin.getSM().getUserManager().getUser(player);
        if (user == null) return "";
        switch (params) {
            case "name":
                return user.getName();
            case "kills":
                return String.valueOf(user.getKills());
            case "deaths":
                return String.valueOf(user.getDeaths());
            case "kdr":
                return String.valueOf(RandomUtils.getKDR(user.getKills(), user.getDeaths()));
        }
        return null;
    }

}