package me.pafias.pafiasffa.commands.subcommands;

import me.pafias.pafiasffa.commands.ICommand;
import me.pafias.pafiasffa.services.SpawnManager;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SavespawnCommand extends ICommand {

    public SavespawnCommand() {
        super("savespawn", "ffa.savespawn", "spawnsave", "addspawn", "spawnadd");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "<name>";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Save a spawnpoint";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " " + getName() + " " + getArgs()));
            return;
        }
        Player player = (Player) sender;
        String name = args[1];
        SpawnManager spawnManager = plugin.getSM().getSpawnManager();
        if (spawnManager.exists(name)) {
            sender.sendMessage(CC.t("&cA spawn with that name already exists!"));
            return;
        }
        if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
            sender.sendMessage(CC.t("&cYou have to have an item in your hand (will be the gui item)"));
            return;
        }
        try {
            spawnManager.saveNewSpawn(player, name);
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(CC.t("&cFailed to save spawn."));
            return;
        }
        sender.sendMessage(CC.t("&aSpawn saved."));
    }

}
