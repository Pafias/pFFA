package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.npcs.NpcManager;
import me.pafias.pffa.objects.Kit;
import me.pafias.putils.CC;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        sender.sendMessage(CC.t("&f------------------ &bFFA NPCs &f------------------"));
        sender.sendMessage(CC.tf("&3/%s npc create <name/id> \"<nametag>\" <skin> [kit] &9- Create a new NPC at your location", label));
        sender.sendMessage(CC.tf("&3/%s npc remove &9- Remove the nearest NPC", label));
    }

    /**
     * Finds the index of the arg that closes the quoted nametag starting at args[3], scanning up to
     * (but not including) scanEndExclusive. Returns -1 if the nametag isn't quoted or isn't closed yet.
     */
    private int findNametagEndIndex(String[] args, int scanEndExclusive) {
        if (args.length <= 3 || !args[3].startsWith("\""))
            return -1;
        for (int i = 3; i < scanEndExclusive; i++) {
            final boolean isFirstToken = i == 3;
            if (args[i].endsWith("\"") && (!isFirstToken || args[i].length() > 1))
                return i;
        }
        return -1;
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return;
        }
        if (args.length < 2) {
            help(sender, mainCommand);
            return;
        }
        final String subCommand = args[1].toLowerCase();
        final NpcManager npcManager = plugin.getSM().getNpcManager();
        if (npcManager == null) {
            sender.sendMessage(CC.t("&cNo NPC provider available. Make sure you have packetevents installed."));
            return;
        }
        switch (subCommand) {
            case "create":
                handleCreate(mainCommand, sender, player, npcManager, args);
                break;
            case "remove":
                try {
                    npcManager.removeNpc(player.getLocation());
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage(CC.t("&c" + ex.getMessage()));
                    return;
                }
                sender.sendMessage(CC.t("&aNPC removed successfully."));
                break;
            default:
                help(sender, mainCommand);
                break;
        }
    }

    private void handleCreate(String mainCommand, CommandSender sender, Player player, NpcManager npcManager, String[] args) {
        if (args.length < 4) {
            help(sender, mainCommand);
            return;
        }
        final String name = args[2];
        if (!args[3].startsWith("\"")) {
            sender.sendMessage(CC.t("&cThe nametag must be wrapped in quotes."));
            return;
        }
        final int nametagEndIndex = findNametagEndIndex(args, args.length);
        if (nametagEndIndex == -1) {
            sender.sendMessage(CC.t("&cUnclosed quote in nametag."));
            return;
        }
        final StringBuilder rawNametag = new StringBuilder();
        for (int i = 3; i <= nametagEndIndex; i++) {
            if (i > 3) rawNametag.append(' ');
            rawNametag.append(args[i]);
        }
        final Component nametag = CC.a(rawNametag.substring(1, rawNametag.length() - 1));

        final int skinIndex = nametagEndIndex + 1;
        if (args.length <= skinIndex) {
            help(sender, mainCommand);
            return;
        }
        final String skinName = args[skinIndex];
        final Location location = player.getLocation();
        final String kitName = args.length > skinIndex + 1 ? args[skinIndex + 1] : null;
        final Kit kit = kitName == null ? null : plugin.getSM().getKitManager().getKit(kitName);
        npcManager.createNpc(name, nametag, skinName, location, kit);
        sender.sendMessage(CC.t("&aNPC created successfully at your location."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2)
            return Stream.of("create", "remove")
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();

        if (!args[1].equalsIgnoreCase("create"))
            return Collections.emptyList();

        if (args.length == 3)
            return Collections.singletonList("<name>");

        if (args.length == 4) {
            if (args[3].isBlank())
                return Collections.singletonList("\"<nametag>\"");
            return Collections.singletonList(CC.t(args[3]));
        }

        // Beyond the name and the start of the nametag, completions only make sense once the
        // quoted nametag (everything but the currently-typed last token) has actually closed.
        final int nametagEndIndex = findNametagEndIndex(args, args.length - 1);
        if (nametagEndIndex == -1)
            return Collections.emptyList();

        final int skinIndex = nametagEndIndex + 1;
        final int currentIndex = args.length - 1;
        if (currentIndex == skinIndex)
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .filter(name -> name.toLowerCase().startsWith(args[currentIndex].toLowerCase()))
                    .toList();
        if (currentIndex == skinIndex + 1)
            return plugin.getSM().getKitManager().getKits()
                    .keySet()
                    .stream()
                    .filter(name -> name.toLowerCase().startsWith(args[currentIndex].toLowerCase()))
                    .toList();

        return Collections.emptyList();
    }

}
