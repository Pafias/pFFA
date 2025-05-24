package me.pafias.pffa.commands.commands;

import me.pafias.pffa.pFFA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnCommand implements CommandExecutor, TabExecutor {

    private final pFFA plugin;

    public SpawnCommand(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).performCommand("ffa lobby " + (args.length > 0 ? args[0] : ""));
        } else {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ffa lobby " + (args.length > 0 ? args[0] : ""));
        }
        return true;
    }

    // Copied from subcommands/LobbyCommand.java
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 2) return Collections.emptyList();
        if (args.length == 1 && sender.hasPermission("ffa.lobby.others"))
            return plugin.getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> ((Player) sender).canSee(p))
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        else
            return Collections.emptyList();
    }

}
