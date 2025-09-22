package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.npcs.NpcManager;
import me.pafias.pffa.objects.Kit;
import me.pafias.putils.LCC;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NpcCommand extends BaseFFACommand {

    public NpcCommand() {
        super("npc", "ffa.npc");
    }

    @Override
    public String getArgs() {
        return "<subcommand>";
    }

    @Override
    public String getDescription() {
        return "Manage NPCs";
    }

    private void help(CommandSender sender, String label) {
        sender.sendMessage(LCC.t("&f------------------ &bFFA NPCs &f------------------"));
        sender.sendMessage(LCC.tf("&3/%s npc create <name> [kit] &9- Create a new NPC at your location", label));
        sender.sendMessage(LCC.tf("&3/%s npc remove &9- Remove the nearest NPC", label));
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LCC.t("&cOnly players."));
            return;
        }
        final Player player = (Player) sender;
        if (args.length < 2) {
            help(sender, mainCommand);
            return;
        }
        final String subCommand = args[1].toLowerCase();
        final NpcManager npcManager = plugin.getSM().getNpcManager();
        if (npcManager == null) {
            sender.sendMessage(LCC.t("&cNo NPC provider available. Make sure you have ProtocolLib installed."));
            return;
        }
        switch (subCommand) {
            case "create":
                if (args.length < 3) {
                    help(sender, mainCommand);
                    return;
                }
                final String name = LCC.t(args[2]);
                final String skinName = args[3];
                final Location location = player.getLocation();
                final String kitName = args.length >= 5 ? args[4] : null;
                final Kit kit = kitName == null ? null : plugin.getSM().getKitManager().getKit(kitName);
                npcManager.createNpc(name, skinName, location, kit);
                sender.sendMessage(LCC.t("&aNPC created successfully at your location."));
                break;
            case "remove":
                try {
                    npcManager.removeNpc(player.getLocation());
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage(LCC.t("&c" + ex.getMessage()));
                    return;
                }
                sender.sendMessage(LCC.t("&aNPC removed successfully."));
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2)
            return Stream.of("create", "remove")
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());

        else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("create")) {
                if (args[2].isEmpty())
                    return Collections.singletonList("<name>");
                return Collections.singletonList(LCC.t(args[2]));
            }
        } else if (args.length == 4) {
            if (args[1].equalsIgnoreCase("create"))
                return Arrays.stream(plugin.getServer().getOfflinePlayers())
                        .map(OfflinePlayer::getName)
                        .filter(Objects::nonNull)
                        .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
        } else if (args.length == 5 && args[1].equalsIgnoreCase("create"))
            return plugin.getSM().getKitManager().getKits()
                    .keySet()
                    .stream()
                    .filter(name -> name.toLowerCase().startsWith(args[4].toLowerCase()))
                    .collect(Collectors.toList());

        return Collections.emptyList();
    }

}
