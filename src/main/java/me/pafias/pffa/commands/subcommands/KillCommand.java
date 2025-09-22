package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.User;
import me.pafias.putils.LCC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KillCommand extends BaseFFACommand {

    public KillCommand() {
        super("kill", null, "suicide", "die");
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

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(LCC.t("&cOnly players."));
                return;
            }
            final User user = plugin.getSM().getUserManager().getUser((Player) sender);
            if (user.isInSpawn())
                return;
            final long killCooldownMillis = plugin.getConfig().getLong("commands.kill_command_cooldown") * 1000L;
            if (System.currentTimeMillis() - user.lastKillMillis < killCooldownMillis) {
                sender.sendMessage(LCC.t("&cPlease wait until using this command again"));
                return;
            }
            if (plugin.getSM().getCombatLogManager().isInCombat(user.getPlayer())) {
                sender.sendMessage(LCC.t("&cYou cannot do this while in combat"));
                return;
            }
            user.getPlayer().setHealth(0);
            user.lastKillMillis = System.currentTimeMillis();
        } else if (args.length > 1 && sender.hasPermission("ffa.kill.others")) {
            final Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(LCC.t("&cPlayer not found."));
                return;
            }
            target.setHealth(0);
            sender.sendMessage(LCC.t("&aKilled &b" + target.getName()));
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
