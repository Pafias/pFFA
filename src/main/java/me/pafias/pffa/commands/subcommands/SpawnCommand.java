package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.services.SpawnManager;
import me.pafias.putils.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SpawnCommand extends BaseFFACommand {

    public SpawnCommand() {
        super("spawn", "ffa.spawn");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "<spawn>";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Teleport to a spawnpoint";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " " + getName() + " " + getArgs() + (sender.hasPermission(getPermission() + ".others") ? " [player]" : "")));
            return;
        }
        final String name = args[1];
        Player target;
        if (args.length >= 3) {
            if (!sender.hasPermission(getPermission() + ".others"))
                return;
            target = plugin.getServer().getPlayer(args[2]);
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(CC.t("&cYou must specify a player when using this command from console: /" + mainCommand + " " + getName() + " " + getArgs() + " [player]"));
                return;
            }
            target = (Player) sender;
        }
        if (target == null) {
            sender.sendMessage(CC.t("&cPlayer not found."));
            return;
        }
        final SpawnManager spawnManager = plugin.getSM().getSpawnManager();
        if (!spawnManager.exists(name)) {
            sender.sendMessage(CC.t("&cThat spawn does not exist!"));
            return;
        }
        final Spawn spawn = spawnManager.getSpawn(name);
        spawn.teleport(target);
        sender.sendMessage(CC.t("&aTeleported" + (sender != target ? target.getName() : "")));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2)
            return plugin.getSM().getSpawnManager().getSpawns().keySet()
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        else if (args.length == 3 && sender.hasPermission(getPermission() + ".others"))
            return plugin.getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> ((Player) sender).canSee(p))
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        return Collections.emptyList();
    }
}
