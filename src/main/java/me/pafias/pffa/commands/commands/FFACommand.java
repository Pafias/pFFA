package me.pafias.pffa.commands.commands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.commands.subcommands.*;
import me.pafias.pffa.commands.subcommands.KillCommand;
import me.pafias.pffa.commands.subcommands.SpawnCommand;
import me.pafias.pffa.commands.subcommands.StatsCommand;
import me.pafias.putils.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FFACommand implements CommandExecutor, TabExecutor {

    public FFACommand() {
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
        commands.add(new ToggleProtectionCommand());
        commands.add(new ReloadCommand());
        commands.add(new ConvertCommand());
        commands.add(new NpcCommand());
    }

    private final Set<BaseFFACommand> commands = new HashSet<>();

    private boolean help(CommandSender sender, String label) {
        sender.sendMessage(CC.t("&f-------------------- &bFFA &f--------------------"));
        for (BaseFFACommand command : commands)
            if (command.getPermission() == null || sender.hasPermission(command.getPermission()))
                sender.sendMessage(CC.tf("&3/%s %s %s &9- %s", label, command.getName(), command.getArgs(), command.getDescription()));
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return help(sender, label);
        else {
            final BaseFFACommand cmd = commands.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(args[0]) || c.getAliases().contains(args[0]))
                    .findFirst().orElse(null);
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 0) return null;
        if (args.length == 1)
            return commands.stream()
                    .map(BaseFFACommand::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        else {
            final BaseFFACommand cmd = commands.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(args[0]) || c.getAliases().contains(args[0]))
                    .findFirst().orElse(null);
            if (cmd == null) return null;
            if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission()))
                return Collections.emptyList();
            else
                return cmd.tabComplete(sender, command, alias, args);
        }
    }

}
