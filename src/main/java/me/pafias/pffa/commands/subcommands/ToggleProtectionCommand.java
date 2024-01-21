package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.ICommand;
import me.pafias.pffa.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ToggleProtectionCommand extends ICommand {

    public ToggleProtectionCommand() {
        super("toggleprotection", "ffa.toggleprotection");
        config = plugin.getSM().getVariables().worldProtection;
    }

    private final ConfigurationSection config;

    @NotNull
    @Override
    public String getArgs() {
        return "<protection>";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Toggle protection config options";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CC.tf("&c/%s %s <protection>", mainCommand, args[0]));
            return;
        }
        if (!config.contains(args[1])) {
            sender.sendMessage(CC.t("&cInvalid protection option!"));
            return;
        }
        if(args.length == 2) {
            boolean oldState = config.getBoolean(args[1]);
            boolean newState = !config.getBoolean(args[1]);
            plugin.getConfig().set(config.getCurrentPath() + "." + args[1], newState);
            plugin.saveConfig();
            config.set(args[1], newState);
            sender.sendMessage(CC.tf("&aChanged &b%s &ato: &b%s", args[1], newState));
        } else {
            plugin.getConfig().set(config.getCurrentPath() + "." + args[1], args[2]);
            plugin.saveConfig();
            config.set(args[1], args[2]);
            sender.sendMessage(CC.tf("&aChanged &b%s &ato: &b%s", args[1], args[2]));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return config.getKeys(false).stream()
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
