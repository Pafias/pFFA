package me.pafias.pafiasffa.commands.commands;

import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.commands.ICommand;
import me.pafias.pafiasffa.commands.subcommands.KillCommand;
import me.pafias.pafiasffa.commands.subcommands.*;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FFACommand implements CommandExecutor, TabExecutor {

    private final PafiasFFA plugin;

    public FFACommand(PafiasFFA plugin) {
        this.plugin = plugin;
        commands.add(new EditstatsCommand());
        commands.add(new StatsCommand());
        commands.add(new SetlobbyCommand());
        commands.add(new LobbyCommand());
        commands.add(new KillCommand());
        commands.add(new SavekitCommand());
        commands.add(new KitCommand());
        commands.add(new SavespawnCommand());
        commands.add(new SpawnCommand());
        commands.add(new ArmorstandCommand());
    }

    private Set<ICommand> commands = new HashSet<>();

    private boolean help(CommandSender sender, String label) {
        sender.sendMessage(CC.t("&f-------------------- &bFFA &f--------------------"));
        commands.forEach(command -> {
            if (sender.hasPermission(command.getPermission()))
                sender.sendMessage(CC.t(String.format("&3/%s %s %s &9- %s", label, command.getName(), command.getArgs(), command.getDescription())));
        });
        /*
        sender.sendMessage(CC.translate("&3/" + label + " help &9- Shows this menu"));
        sender.sendMessage(CC.translate("&3/" + label + " editstats &9- Edit a player's statistics"));
        sender.sendMessage(CC.translate("&3/" + label + " stats [player] &9- See a player's statistics"));
        sender.sendMessage(CC.translate("&3/" + label + " setlobby &9-Set the lobby"));
        sender.sendMessage(CC.translate("&3/" + label + " lobby &9-Go to the lobby"));
        sender.sendMessage(CC.translate("&3/" + label + " kill &9-Kill yourself"));
        sender.sendMessage(CC.translate("&3/" + label + " savekit <name> &9-Save a kit"));
        sender.sendMessage(CC.translate("&3/" + label + " kit <name> &9-Get a kit"));
        sender.sendMessage(CC.translate("&3/" + label + " savespawn <name> &9-Save a spawn"));
        sender.sendMessage(CC.translate("&3/" + label + " spawn <name> &9-Teleport to a spawn"));
         */
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return help(sender, label);
        else {
            ICommand cmd = commands.stream().filter(c -> c.getName().equalsIgnoreCase(args[0]) || c.getAliases().contains(args[0])).findFirst().orElse(null);
            if (cmd == null) return help(sender, label);
            if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                cmd.noPermission(sender);
                return true;
            } else
                cmd.execute(label, sender, args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return null;
        if (args.length == 1)
            return commands.stream().map(ICommand::getName).filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        else {
            ICommand cmd = commands.stream().filter(c -> c.getName().equalsIgnoreCase(args[0]) || c.getAliases().contains(args[0])).findFirst().orElse(null);
            if (cmd == null) return null;
            if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission()))
                return Collections.emptyList();
            else
                return cmd.tabComplete(sender, command, alias, args);
        }
    }

}
