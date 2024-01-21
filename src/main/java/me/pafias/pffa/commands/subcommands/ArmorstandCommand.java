package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.ICommand;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArmorstandCommand extends ICommand {

    public ArmorstandCommand() {
        super("armorstand", "ffa.armorstand", "as");
    }

    @Override
    public String getArgs() {
        return "<name>";
    }

    @Override
    public String getDescription() {
        return "Get kit or spawn armorstand";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        // ffa armorstand Beast
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " " + getName() + " " + getArgs()));
            return;
        }
        String name = CC.t(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        Player player = (Player) sender;
        ArmorStand armorstand = null;
        if (player.getWorld().getEntities().stream().anyMatch(e -> (e instanceof ArmorStand) && e.getLocation().getBlock() == player.getLocation().getBlock())) {
            armorstand = (ArmorStand) player.getWorld().getEntities().stream().filter(e -> (e instanceof ArmorStand) && e.getLocation().getBlock() == player.getLocation().getBlock()).findAny().orElse(null);
        }
        if (armorstand == null) {
            armorstand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
            prepAs(armorstand, name);
        } else
            prepAs(armorstand, name);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length >= 3) return Collections.emptyList();
        return Stream.concat(
                        plugin.getSM().getKitManager().getKits().keySet().stream(),
                        plugin.getSM().getSpawnManager().getSpawns().keySet().stream()
                )
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private void prepAs(ArmorStand as, String name) {
        as.setBasePlate(false);
        as.setArms(true);
        as.setCustomNameVisible(true);
        as.setCustomName(name);
        Kit kit = plugin.getSM().getKitManager().getKit(name);
        if (kit != null) {
            as.setItemInHand(kit.getItems().get(0));
            as.setHelmet(kit.getItems().get(39));
            as.setChestplate(kit.getItems().get(38));
            as.setLeggings(kit.getItems().get(37));
            as.setBoots(kit.getItems().get(36));
        }
    }

}
