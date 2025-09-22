package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.services.SpawnManager;
import me.pafias.putils.LCC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnCommand extends BaseFFACommand {

    public SpawnCommand() {
        super("spawn", "ffa.spawn");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "<name>";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Teleport to a spawnpoint";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LCC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(LCC.t("&c/" + mainCommand + " " + getName() + " " + getArgs() + (sender.hasPermission(getPermission() + ".others") ? " [player]" : "")));
            return;
        }
        final Player player = (Player) sender;
        final String name = args[1];
        Player target = player;
        if (args.length >= 3 && sender.hasPermission(getPermission() + ".others"))
            target = plugin.getServer().getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(LCC.t("&cPlayer not found."));
            return;
        }
        final SpawnManager spawnManager = plugin.getSM().getSpawnManager();
        if (!spawnManager.exists(name)) {
            sender.sendMessage(LCC.t("&cThat spawn does not exist!"));
            return;
        }
        final Spawn spawn = spawnManager.getSpawn(name);
        spawn.teleport(target);
        sender.sendMessage(LCC.t("&aTeleported" + (sender != target ? target.getName() : "")));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2)
            return plugin.getSM().getSpawnManager().getSpawns().keySet()
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        else if (args.length == 3)
            return plugin.getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> ((Player) sender).canSee(p))
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }
}
