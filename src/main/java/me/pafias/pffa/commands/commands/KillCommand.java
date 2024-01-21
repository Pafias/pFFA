package me.pafias.pffa.commands.commands;

import me.pafias.pffa.pFFA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KillCommand implements CommandExecutor {

    private final pFFA plugin;

    public KillCommand(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).performCommand("ffa kill " + (args.length > 0 ? args[0] : ""));
        } else {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ffa kill " + (args.length > 0 ? args[0] : ""));
        }
        return true;
    }

}
