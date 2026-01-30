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

public class StatsCommand implements CommandExecutor, TabExecutor {

    private final pFFA plugin;

    public StatsCommand(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).performCommand("ffa stats " + (args.length > 0 ? args[0] : ""));
        } else {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ffa stats " + (args.length > 0 ? args[0] : ""));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("ffa.stats.others")) {
            final List<String> list = new ArrayList<>();
            for (final Player p : plugin.getServer().getOnlinePlayers()) {
                if (!(sender instanceof Player player) || player.canSee(p)) {
                    if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        list.add(p.getName());
                    }
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

}
