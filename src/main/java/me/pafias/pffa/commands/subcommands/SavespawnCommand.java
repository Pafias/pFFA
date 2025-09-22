package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.services.SpawnManager;
import me.pafias.putils.LCC;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SavespawnCommand extends BaseFFACommand {

    public SavespawnCommand() {
        super("savespawn", "ffa.savespawn", "spawnsave", "addspawn", "spawnadd");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "<name> [permission]";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Save a spawnpoint";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LCC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(LCC.t("&c/" + mainCommand + " " + getName() + " " + getArgs()));
            return;
        }
        final Player player = (Player) sender;
        if (player.getInventory().getItemInHand().getType().equals(Material.AIR)) {
            sender.sendMessage(LCC.t("&cYou have to have an item in your hand (will be the gui item)"));
            return;
        }
        final String name = args[1];
        final String permission = args.length >= 3 ? args[2] : null;
        final SpawnManager spawnManager = plugin.getSM().getSpawnManager();
        try {
            spawnManager.saveNewSpawn(player, name, permission);
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(LCC.t("&cFailed to save spawn."));
            return;
        }
        sender.sendMessage(LCC.t("&aSpawn saved."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 3) return Collections.emptyList();
        return Collections.singletonList("<spawn name>");
    }

}
