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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompareCommand extends BaseFFACommand {

    public CompareCommand() {
        super("compare", "ffa.compare");
    }

    @Override
    public String getArgs() {
        return "<player>";
    }

    @Override
    public String getDescription() {
        return "Compare your stats with another player";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " compare <player>"));
            return;
        }
        if (!(sender instanceof Player playerSelf)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        sender.sendMessage(CC.t("&6Fetching data..."));
        final User selfUser = plugin.getSM().getUserManager().getUser(playerSelf);
        final String targetName = args[1];
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
                .thenAccept(targetPlayer -> {
                    if (targetPlayer == null) {
                        sender.sendMessage(CC.t("&cPlayer not found!"));
                        return;
                    }
                    final User targetUser = plugin.getSM().getUserManager().getUser(targetPlayer.getUniqueId());
                    if (targetUser != null) {
                        sender.sendMessage(CC.multiLine(
                                "",
                                CC.af("&3---------- &9FFA Stats: &d%s &9vs &d%s &3----------", selfUser.getName(), targetUser.getName()),
                                CC.af("&6Kills: &b%d &7(%d vs %d)", Math.abs(selfUser.getKills() - targetUser.getKills()), selfUser.getKills(), targetUser.getKills()),
                                CC.af("&6Deaths: &b%d &7(%d vs %d)", Math.abs(selfUser.getDeaths() - targetUser.getDeaths()), selfUser.getDeaths(), targetUser.getDeaths()),
                                CC.af("&6KDR: &b%.2f &7(%.2f vs %.2f)", Math.abs(selfUser.getKDR() - targetUser.getKDR()), selfUser.getKDR(), targetUser.getKDR()),
                                CC.af("&6Current killstreak: &b%d &7(%d vs %d)", Math.abs(selfUser.getCurrentKillstreak() - targetUser.getCurrentKillstreak()), selfUser.getCurrentKillstreak(), targetUser.getCurrentKillstreak()),
                                CC.af("&6Best killstreak: &b%d &7(%d vs %d)", Math.abs(selfUser.getBestKillstreak() - targetUser.getBestKillstreak()), selfUser.getBestKillstreak(), targetUser.getBestKillstreak()),
                                ""
                        ));
                    } else {
                        UserData targetData = plugin.getSM().getUserDataStorage().getUserData(targetPlayer.getUniqueId().toString());
                        if (targetData == null) {
                            sender.sendMessage(CC.t("&cNo data found on this player."));
                            return;
                        }
                        FfaData targetFfaData = targetData.getFfaData();
                        sender.sendMessage(CC.multiLine(
                                "",
                                CC.af("&3---------- &9FFA Stats: &d%s &9vs &d%s &3----------", selfUser.getName(), targetPlayer.getName()),
                                CC.af("&6Kills: &b%d &7(%d vs %d)", Math.abs(selfUser.getKills() - targetFfaData.getKills()), selfUser.getKills(), targetFfaData.getKills()),
                                CC.af("&6Deaths: &b%d &7(%d vs %d)", Math.abs(selfUser.getDeaths() - targetFfaData.getDeaths()), selfUser.getDeaths(), targetFfaData.getDeaths()),
                                CC.af("&6KDR: &b%.2f &7(%.2f vs %.2f)", Math.abs(selfUser.getKDR() - targetFfaData.getKDR()), selfUser.getKDR(), targetFfaData.getKDR()),
                                CC.af("&6Best killstreak: &b%d &7(%d vs %d)", Math.abs(selfUser.getBestKillstreak() - targetFfaData.getKillstreak()), selfUser.getBestKillstreak(), targetFfaData.getKillstreak()),
                                ""
                        ));
                    }
                });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
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
