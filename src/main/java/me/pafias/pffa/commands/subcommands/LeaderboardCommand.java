package me.pafias.pffa.commands.subcommands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.storage.UserDataStorage;
import me.pafias.putils.BukkitPlayerManager;
import me.pafias.putils.CC;
import me.pafias.putils.Tasks;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LeaderboardCommand extends BaseFFACommand {

    private final UserDataStorage userDataStorage;

    public LeaderboardCommand() {
        super("leaderboard", "ffa.leaderboard", "lb");
        this.userDataStorage = plugin.getSM().getUserDataStorage();
    }

    @Override
    public String getArgs() {
        return "[kills/deaths/killstreak]";
    }

    @Override
    public String getDescription() {
        return "Statistics leaderboard";
    }

    private final Map<Statistic, Instant> lastFetches = new EnumMap<>(Statistic.class);

    private final Map<Statistic, Map<OfflinePlayer, Integer>> cachedLeaderboards = new EnumMap<>(Statistic.class);

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        final Statistic statistic;
        if (args.length >= 2) {
            statistic = Statistic.parse(args[1]);
        } else {
            statistic = Statistic.KILLS;
        }
        if (statistic == null) {
            sender.sendMessage(CC.t("&cInvalid statistic type. Use: kills, deaths, or killstreak."));
            return;
        }
        sender.sendMessage(CC.t("&6Fetching data..."));
        final CompletableFuture<Map<OfflinePlayer, Integer>> future = new CompletableFuture<>();
        Tasks.runAsync(() -> {
            if (cachedLeaderboards.containsKey(statistic)) { // Already cached
                final Instant lastFetch = lastFetches.get(statistic);
                if (Duration.between(lastFetch, Instant.now()).toSeconds() < 30) { // Too little time has passed, use cache
                    final Map<OfflinePlayer, Integer> cachedLeaderboard = cachedLeaderboards.get(statistic);
                    future.complete(cachedLeaderboard);
                    return;
                }
            }
            final List<UserData> userDataList = userDataStorage.getTopStatistic(statistic, 10);
            final Map<OfflinePlayer, Integer> map = new LinkedHashMap<>(userDataList.size());
            for (UserData userData : userDataList) {
                final OfflinePlayer player = BukkitPlayerManager.getOfflinePlayerByUUID(userData.getUniqueId(), false);
                final int value = switch (statistic) {
                    case KILLS -> userData.getFfaData().getKills();
                    case DEATHS -> userData.getFfaData().getDeaths();
                    case KILLSTREAK -> userData.getFfaData().getKillstreak();
                };
                map.put(player, value);
            }
            lastFetches.put(statistic, Instant.now());
            cachedLeaderboards.put(statistic, map);
            future.complete(map);
        });
        future.thenAccept(map -> {
            sender.sendMessage(CC.t("&7--------------- &3Top 10 " + statistic.getDisplayName() + " &7---------------"));
            int nr = 1;
            for (Map.Entry<OfflinePlayer, Integer> entry : map.entrySet()) {
                final OfflinePlayer offlinePlayer = entry.getKey();
                final Integer value = entry.getValue();
                final String name = offlinePlayer != null && offlinePlayer.getName() != null ? offlinePlayer.getName() : "&cUnknown";
                sender.sendMessage(CC.tf("&7[&e#%d&7] &b%s &7with &d%d &7%s", nr, name, value, statistic.getDisplayName()));
                nr++;
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            final List<String> list = new ArrayList<>();
            for (Statistic statistic : Statistic.values()) {
                if (statistic.name().startsWith(args[1].toUpperCase())) {
                    list.add(statistic.name());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    @Getter
    @AllArgsConstructor
    public enum Statistic {
        KILLS("kills", "kills"), DEATHS("deaths", "deaths"), KILLSTREAK("killstreak", "kill streak");

        private final String dbColumnName, displayName;

        public static Statistic parse(String input) {
            try {
                return valueOf(input);
            } catch (IllegalArgumentException e) {
                try {
                    return valueOf(input.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    for (Statistic statistic : Statistic.values()) {
                        if (statistic.name().equalsIgnoreCase(input)) {
                            return statistic;
                        }
                    }
                }
            }
            return null;
        }
    }

}
