package me.pafias.pafiasffa.commands.subcommands;

import me.pafias.pafiasffa.commands.ICommand;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

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
        String name = args[1];
        Player player = (Player) sender;
        ArmorStand armorstand = null;
        if (player.getWorld().getEntities().stream().anyMatch(e -> (e instanceof ArmorStand) && e.getLocation().getBlock() == player.getLocation().getBlock())) {
            armorstand = (ArmorStand) player.getWorld().getEntities().stream().filter(e -> (e instanceof ArmorStand) && e.getLocation().getBlock() == player.getLocation().getBlock()).findAny().orElse(null);
        }
        if (armorstand == null)
            armorstand = player.getWorld().spawn(player.getLocation(), ArmorStand.class, as -> prepAs(as, name));
        else
            prepAs(armorstand, name);
    }

    private void prepAs(ArmorStand as, String name) {
        as.setBasePlate(false);
        as.setArms(true);
        as.setCustomNameVisible(true);
        as.setCustomName(name);
    }

}
