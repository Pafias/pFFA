package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.UserData;
import me.pafias.putils.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        CompletableFuture.supplyAsync(() -> {
                    final OfflinePlayer offlinePlayer = BukkitPlayerManager.getOfflinePlayerByInput(targetName, false);
                    if (offlinePlayer != null && offlinePlayer.getName() != null) {
                        return new SimplePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName());
                    } else {
                        final MojangPlayer mojangPlayer = MojangUtils.getMojangPlayerByInput(targetName);
                        if (mojangPlayer != null) {
                            return new SimplePlayer(mojangPlayer.getUniqueId(), mojangPlayer.getName());
                        }
                    }
                    return null;
                })
                .thenAccept(player -> {
                    if (player == null) {
                        sender.sendMessage(CC.t("&cPlayer not found!"));
                        return;
                    }
                    final User user = plugin.getSM().getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
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
                        UserData userData = plugin.getSM().getUserDataStorage().getUserData(player.getUniqueId().toString());
                        if (userData == null) {
                            sender.sendMessage(CC.t("&cNo data found on this player."));
                            return;
                        }
                        FfaData ffaData = userData.getFfaData();
                        sender.sendMessage(CC.multiLine(
                                "",
                                CC.af("&3---------- &9FFA Stats for &d%s &3----------", player.getName()),
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
            final List<String> list = new ArrayList<>();
            final String arg = args[1].toLowerCase();
            for (final Player p : plugin.getServer().getOnlinePlayers()) {
                if (!(sender instanceof Player player) || player.canSee(p)) {
                    if (p.getName().toLowerCase().startsWith(arg)) {
                        list.add(p.getName());
                    }
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

}
