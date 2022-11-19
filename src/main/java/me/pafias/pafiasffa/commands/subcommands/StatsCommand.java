package me.pafias.pafiasffa.commands.subcommands;

import me.pafias.pafiasffa.commands.ICommand;
import me.pafias.pafiasffa.objects.User;
import me.pafias.pafiasffa.objects.UserConfig;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

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
        if (args.length == 2)
            targetName = args[1];
        sender.sendMessage(CC.t("&6Fetching data..."));
        User target = plugin.getSM().getUserManager().getUser(targetName);
        if (target != null) {
            sender.sendMessage("");
            sender.sendMessage(CC.t(String.format("&3---------- &9FFA Stats for &d%s &3----------", target.getName())));
            sender.sendMessage(CC.t(String.format("&6Kills: &7%d", target.getKills())));
            sender.sendMessage(CC.t(String.format("&6Deaths: &7%d", target.getDeaths())));
            sender.sendMessage("");
        } else {
            UserConfig config = new UserConfig(targetName);
            config.exists().thenAccept(exists -> {
                if (!exists) {
                    sender.sendMessage(CC.t("&cPlayer not found!"));
                } else {
                    config.get("name", "kills", "deaths").thenAccept(list -> {
                        sender.sendMessage("");
                        sender.sendMessage(CC.t(String.format("&3---------- &9FFA Stats for &d%s &3----------", list.get(0))));
                        sender.sendMessage(CC.t(String.format("&6Kills: &7%d", list.get(1))));
                        sender.sendMessage(CC.t(String.format("&6Deaths: &7%d", list.get(2))));
                        sender.sendMessage("");
                    });
                }
            });
        }
    }

}
