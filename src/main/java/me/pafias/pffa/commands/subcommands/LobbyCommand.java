package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.User;
import me.pafias.putils.CC;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class LobbyCommand extends BaseFFACommand {

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
            if (!sender.hasPermission(getPermission() + ".others")) {
                noPermission(sender);
                return;
            }
            final String targetName = args[1];
            if (plugin.getServer().getPlayer(targetName) == null) {
                sender.sendMessage(CC.t("&cTarget not online."));
                return;
            }
            try {
                plugin.getServer().getPlayer(targetName).teleport(plugin.getLobbySpawn());
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                sender.sendMessage(CC.t("&cUnable to teleport: Lobby not set."));
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(CC.t("&cOnly players."));
                return;
            }
            final User user = plugin.getSM().getUserManager().getUser(player);
            if (plugin.getFfaWorlds().contains(player.getWorld().getName()) && !user.isInSpawn()
                    && (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR))) {
                sender.sendMessage(CC.t("&cYou cannot do that here."));
                return;
            }
            try {
                player.teleport(plugin.getLobbySpawn());
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                player.sendMessage(CC.t("&cUnable to teleport: Lobby not set."));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2 && sender.hasPermission(getPermission() + ".others"))
            return plugin.getServer().getOnlinePlayers()
                    .stream()
                    .filter(p -> ((Player) sender).canSee(p))
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        return Collections.emptyList();
    }

}
