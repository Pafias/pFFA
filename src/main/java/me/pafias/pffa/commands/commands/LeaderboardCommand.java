package me.pafias.pffa.commands.commands;

import me.pafias.pffa.pFFA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardCommand implements CommandExecutor, TabExecutor {

    private final pFFA plugin;

    public LeaderboardCommand(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).performCommand("ffa leaderboard " + (args.length > 0 ? args[0] : ""));
        } else {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ffa leaderboard " + (args.length > 0 ? args[0] : ""));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final List<String> list = new ArrayList<>();
            for (final me.pafias.pffa.commands.subcommands.LeaderboardCommand.Statistic statistic : me.pafias.pffa.commands.subcommands.LeaderboardCommand.Statistic.values()) {
                if (statistic.name().startsWith(args[0].toUpperCase())) {
                    list.add(statistic.name().toLowerCase());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

}
