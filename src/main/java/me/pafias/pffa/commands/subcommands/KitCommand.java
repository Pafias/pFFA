package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.services.KitManager;
import me.pafias.putils.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class KitCommand extends BaseFFACommand {

    public KitCommand() {
        super("kit", "ffa.kit");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "<name>";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Get a kit";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " " + getName() + " " + getArgs() + (sender.hasPermission(getPermission() + ".others") ? " [player]" : "")));
            return;
        }
        final String name = args[1];
        Player target = player;
        if (args.length >= 3 && sender.hasPermission(getPermission() + ".others"))
            target = plugin.getServer().getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(CC.t("&cPlayer not found."));
            return;
        }
        final KitManager kitManager = plugin.getSM().getKitManager();
        if (!kitManager.exists(name)) {
            sender.sendMessage(CC.t("&cThat kit does not exist!"));
            return;
        }
        final Kit kit = kitManager.getKit(name);
        kit.give(target);
        sender.sendMessage(CC.t("&aKit given" + (sender != target ? " to " + target.getName() : "")));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2)
            return plugin.getSM().getKitManager().getKits().keySet()
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        else if (args.length == 3)
            return plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> ((Player) sender).canSee(p))
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        return Collections.emptyList();
    }

}
