package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.services.KitManager;
import me.pafias.putils.CC;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SavekitCommand extends BaseFFACommand {

    public SavekitCommand() {
        super("savekit", "ffa.savekit", "kitsave", "addkit", "kitadd");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "<name> [permission]";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Save a kit";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " " + getName() + " " + getArgs()));
            return;
        }
        if (player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            sender.sendMessage(CC.t("&cYou have to have an item in your hand (will be the gui item)"));
            return;
        }
        final String name = args[1];
        final String permission = args.length >= 3 ? args[2] : null;
        final KitManager kitManager = plugin.getSM().getKitManager();
        try {
            kitManager.saveNewKit(player, name, permission);
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(CC.t("&cFailed to save kit."));
            return;
        }
        sender.sendMessage(CC.t("&aKit saved."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 3) return Collections.emptyList();
        return Collections.singletonList("<kit name>");
    }

}
