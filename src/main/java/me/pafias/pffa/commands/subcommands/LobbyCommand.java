package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.ICommand;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.util.CC;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyCommand extends ICommand {

    public LobbyCommand() {
        super("lobby", "ffa.lobby");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Go to the lobby";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (args.length >= 2) {
            if (!sender.hasPermission("ffa.lobby.others")) {
                noPermission(sender);
                return;
            }
            String targetName = args[1];
            if (plugin.getServer().getPlayer(targetName) == null) {
                sender.sendMessage(CC.t("&cTarget not online."));
                return;
            }
            try {
                plugin.getServer().getPlayer(targetName).teleport(plugin.getSM().getVariables().lobby);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                sender.sendMessage(CC.t("&cUnable to teleport: Lobby not set."));
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(CC.t("&cOnly players."));
                return;
            }
            Player player = (Player) sender;
            User user = plugin.getSM().getUserManager().getUser(player);
            if (plugin.getSM().getVariables().ffaWorlds.contains(player.getWorld().getName()) && !user.isInSpawn()
                    && (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR))) {
                sender.sendMessage(CC.t("&cYou cannot do that here."));
                return;
            }
            try {
                player.teleport(plugin.getSM().getVariables().lobby);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                player.sendMessage(CC.t("&cUnable to teleport: Lobby not set."));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 3) return Collections.emptyList();
        if (args.length == 2 && sender.hasPermission("ffa.lobby.others"))
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
