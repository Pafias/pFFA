package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.util.Serializer;
import me.pafias.putils.LCC;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SetlobbyCommand extends BaseFFACommand {

    public SetlobbyCommand() {
        super("setlobby", "ffa.setlobby");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Set the lobby (spawn)location";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LCC.t("&cOnly players."));
            return;
        }
        final Player player = (Player) sender;
        final Location location = player.getLocation().clone();
        location.setX(player.getLocation().getBlockX() + 0.5);
        location.setY(player.getLocation().getBlockY() + 0.1);
        location.setZ(player.getLocation().getBlockZ() + 0.5);
        location.setPitch(player.getLocation().getPitch() > 10 || player.getLocation().getPitch() < -10 ? 0 : player.getLocation().getPitch());

        plugin.getConfig().set("lobby", Serializer.locationToConfig("spawn", location));
        plugin.saveConfig();
        sender.sendMessage(LCC.t("&aLobby set."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

}
