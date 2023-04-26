package me.pafias.pafiasffa.commands.subcommands;

import me.pafias.pafiasffa.commands.ICommand;
import me.pafias.pafiasffa.objects.User;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class KillCommand extends ICommand {

    public KillCommand() {
        super("kill", "ffa.kill", "suicide", "die");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Kill yourself";
    }

    private Map<UUID, BukkitTask> cooldown = new HashMap<>();

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(CC.t("&cOnly players."));
                return;
            }
            User user = plugin.getSM().getUserManager().getUser((Player) sender);
            if (cooldown.containsKey(user.getUUID()) || user.isInSpawn())
                return;
            int kc = plugin.getSM().getVariables().killCooldown;
            cooldown.put(user.getUUID(), new BukkitRunnable() {
                @Override
                public void run() {
                    user.getPlayer().setHealth(0);
                    cooldown.remove(user.getUUID());
                }
            }.runTaskLater(plugin, (kc * 20)));
            user.getPlayer().sendMessage(CC.t("&6You will die in " + kc + " seconds. Make sure you don't get hit!"));
        } else if (args.length > 1 && sender.hasPermission("ffa.kill.others")) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("Essentials")) {
                if (sender instanceof Player)
                    ((Player) sender).performCommand("essentials:kill " + args[1]);
                else
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "essentials:kill " + args[1]);
            } else {
                if (sender instanceof Player)
                    ((Player) sender).performCommand("minecraft:kill " + args[1]);
                else
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "minecraft:kill " + args[1]);
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 3) return Collections.emptyList();
        if (args.length == 2 && sender.hasPermission("ffa.kill.others"))
            return plugin.getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> ((Player) sender).canSee(p))
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        else
            return Collections.emptyList();
    }

}
