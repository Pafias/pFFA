package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.Kit;
import me.pafias.putils.CC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArmorstandCommand extends BaseFFACommand {

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(CC.t("&c/" + mainCommand + " " + getName() + " " + getArgs()));
            return;
        }
        final Component name = CC.a(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        ArmorStand armorstand = null;
        if (player.getWorld().getEntities().stream().anyMatch(e -> (e instanceof ArmorStand) && e.getLocation().getBlock() == player.getLocation().getBlock())) {
            armorstand = (ArmorStand) player.getWorld().getEntities().stream()
                    .filter(e -> (e instanceof ArmorStand) && e.getLocation().getBlock() == player.getLocation().getBlock())
                    .findAny().orElse(null);
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

    private void prepAs(ArmorStand as, Component name) {
        as.setBasePlate(false);
        as.setArms(true);
        as.setCustomNameVisible(true);
        as.customName(name);
        final Kit kit = plugin.getSM().getKitManager().getKit(PlainTextComponentSerializer.plainText().serialize(name));
        if (kit != null) {
            final ItemStack mainHand = kit.getItems().get(0);
            if (mainHand != null)
                as.getEquipment().setItemInMainHand(mainHand);

            final ItemStack offHand = kit.getItems().get(40);
            if (offHand != null)
                as.getEquipment().setItemInOffHand(offHand);

            final ItemStack helmet = kit.getItems().get(39);
            if (helmet != null)
                as.getEquipment().setHelmet(helmet);

            final ItemStack chestplate = kit.getItems().get(38);
            if (chestplate != null)
                as.getEquipment().setChestplate(chestplate);

            final ItemStack leggings = kit.getItems().get(37);
            if (leggings != null)
                as.getEquipment().setLeggings(leggings);

            final ItemStack boots = kit.getItems().get(36);
            if (boots != null)
                as.getEquipment().setBoots(boots);
        }
    }

}
