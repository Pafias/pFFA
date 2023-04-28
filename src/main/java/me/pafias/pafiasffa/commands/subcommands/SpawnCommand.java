package me.pafias.pafiasffa.commands.subcommands;

import me.pafias.pafiasffa.commands.ICommand;
import me.pafias.pafiasffa.objects.Spawn;
import me.pafias.pafiasffa.services.SpawnManager;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnCommand extends ICommand {

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
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " " + getName() + " " + getArgs() + (sender.hasPermission("ffa.spawn.others") ? " [player]" : "")));
            return;
        }
        Player player = (Player) sender;
        String name = args[1];
        Player target = player;
        if (args.length >= 3 && sender.hasPermission("ffa.spawn.others"))
            target = plugin.getServer().getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(CC.t("&cPlayer not found."));
            return;
        }
        SpawnManager spawnManager = plugin.getSM().getSpawnManager();
        if (!spawnManager.exists(name)) {
            sender.sendMessage(CC.t("&cThat spawn does not exist!"));
            return;
        }
        Spawn spawn = spawnManager.getSpawn(name);
        spawn.teleport(target);
        sender.sendMessage(CC.t("&aTeleported" + (sender != target ? target.getName() : "")));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 3) return Collections.emptyList();
        return plugin.getSM().getSpawnManager().getSpawns().keySet()
                .stream()
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
