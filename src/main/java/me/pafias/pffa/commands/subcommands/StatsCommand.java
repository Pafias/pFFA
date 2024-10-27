package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.ICommand;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.UserConfig;
import me.pafias.pffa.util.CC;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class StatsCommand extends ICommand {

    public StatsCommand() {
        super("stats", "ffa.stats", "statistics");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "[player]";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "See your/someone's statistics";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        String targetName = sender.getName();
        if (args.length == 2 && sender.hasPermission("ffa.stats.others"))
            targetName = args[1];
        sender.sendMessage(CC.t("&6Fetching data..."));
        User target = plugin.getSM().getUserManager().getUser(targetName);
        if (target != null) {
            sender.sendMessage("");
            sender.sendMessage(CC.t(String.format("&3---------- &9FFA Stats for &d%s &3----------", target.getName())));
            sender.sendMessage(CC.t(String.format("&6Kills: &7%d", target.getKills())));
            sender.sendMessage(CC.t(String.format("&6Deaths: &7%d", target.getDeaths())));
            sender.sendMessage(CC.t(String.format("&6KDR: &7%.2f", target.getKDR())));
            sender.sendMessage(CC.t(String.format("&6Current killstreak: &7%d", target.getCurrentKillstreak())));
            sender.sendMessage(CC.t(String.format("&6Best killstreak: &7%d", target.getBestKillstreak())));
            sender.sendMessage("");
        } else {
            String finalTargetName = targetName;
            CompletableFuture.supplyAsync(() -> plugin.getServer().getOfflinePlayer(finalTargetName)).thenAccept(offlinePlayer -> {
                if (!offlinePlayer.hasPlayedBefore())
                    sender.sendMessage(CC.t("&cPlayer not found!"));
                else {
                    UserConfig config = new UserConfig(offlinePlayer.getUniqueId());
                    config.exists().thenAccept(exists -> {
                        if (!exists) {
                            sender.sendMessage(CC.t("&cPlayer not found!"));
                        } else {
                            config.get("kills", "deaths", "killstreak").thenAccept(list -> {
                                int kills = (int) list.get("kills");
                                int deaths = (int) list.get("deaths");
                                int ks = (int) list.get("killstreak");
                                double kdr = kills / (double) (deaths == 0 ? 1 : deaths);
                                sender.sendMessage("");
                                sender.sendMessage(CC.t(String.format("&3---------- &9FFA Stats for &d%s &3----------", offlinePlayer.getName())));
                                sender.sendMessage(CC.t(String.format("&6Kills: &7%d", kills)));
                                sender.sendMessage(CC.t(String.format("&6Deaths: &7%d", deaths)));
                                sender.sendMessage(CC.t(String.format("&6KDR: &7%.2f", kdr)));
                                sender.sendMessage(CC.t(String.format("&6Best killstreak: &7%d", ks)));

                                sender.sendMessage("");
                            });
                        }
                    });
                }
            });
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 3) return Collections.emptyList();
        if (!sender.hasPermission("ffa.stats.others")) return Collections.emptyList();
        if (args[1].length() < 4) return Collections.singletonList("Type at least 4 letters to auto-complete");
        return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
