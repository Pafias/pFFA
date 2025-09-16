package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.UserData;
import me.pafias.putils.BukkitPlayerManager;
import me.pafias.putils.CC;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class StatsCommand extends BaseFFACommand {

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
        if (args.length == 1 && !(sender instanceof Player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        final String targetName;
        if (args.length == 2 && sender.hasPermission(getPermission() + ".others"))
            targetName = args[1];
        else
            targetName = sender.getName();
        sender.sendMessage(CC.t("&6Fetching data..."));
        CompletableFuture.supplyAsync(() -> BukkitPlayerManager.getOfflinePlayerByInput(targetName))
                .thenAccept(offlinePlayer -> {
                    if (offlinePlayer == null) {
                        sender.sendMessage(CC.t("&cPlayer not found!"));
                        return;
                    }
                    if (offlinePlayer.isOnline()) {
                        User user = plugin.getSM().getUserManager().getUser(offlinePlayer.getUniqueId());
                        if (user == null) {
                            sender.sendMessage(CC.t("&cPlayer not found!"));
                            return;
                        }
                        sender.sendMessage(CC.multiLine(
                                "",
                                CC.af("&3---------- &9FFA Stats for &d%s &3----------", user.getName()),
                                CC.af("&6Kills: &7%d", user.getKills()),
                                CC.af("&6Deaths: &7%d", user.getDeaths()),
                                CC.af("&6KDR: &7%.2f", user.getKDR()),
                                CC.af("&6Current killstreak: &7%d", user.getCurrentKillstreak()),
                                CC.af("&6Best killstreak: &7%d", user.getBestKillstreak()),
                                ""
                        ));
                    } else {
                        UserData userData = plugin.getSM().getUserDataStorage().getUserData(offlinePlayer.getUniqueId().toString());
                        if (userData == null) {
                            sender.sendMessage(CC.t("&cNo data found on this player."));
                            return;
                        }
                        FfaData ffaData = userData.getFfaData();
                        sender.sendMessage(CC.multiLine(
                                "",
                                CC.af("&3---------- &9FFA Stats for &d%s &3----------", offlinePlayer.getName()),
                                CC.af("&6Kills: &7%d", ffaData.getKills()),
                                CC.af("&6Deaths: &7%d", ffaData.getDeaths()),
                                CC.af("&6KDR: &7%.2f", ffaData.getKDR()),
                                CC.af("&6Best killstreak: &7%d", ffaData.getKillstreak()),
                                ""
                        ));
                    }
                });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2 && sender.hasPermission(getPermission() + ".others")) {
            if (args[1].length() < 4)
                return Collections.singletonList("Type at least 4 letters to auto-complete");
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }

}
