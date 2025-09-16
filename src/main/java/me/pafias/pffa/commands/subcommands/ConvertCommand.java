package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;
import me.pafias.putils.CC;
import me.pafias.putils.Tasks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class ConvertCommand extends BaseFFACommand {

    public ConvertCommand() {
        super("convert", "ffa.admin");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "<folder>";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Convert file player data storage";
    }

    private final Map<CommandSender, Boolean> confirm = new WeakHashMap<>();

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(CC.t("&cFor security reasons, only console may execute this command."));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(CC.t("&c/ffa convert " + getArgs()));
            return;
        }
        final File playerdataDirectory = new File(args[1]);
        if (!playerdataDirectory.exists() || !playerdataDirectory.isDirectory()) {
            sender.sendMessage(CC.t("&cThe specified folder does not exist or is not a directory."));
            return;
        }
        if (playerdataDirectory.listFiles().length == 0) {
            sender.sendMessage(CC.t("&cThe specified folder is empty."));
            return;
        }
        if (!confirm.containsKey(sender)) {
            confirm.put(sender, true);
            sender.sendMessage("");
            sender.sendMessage(CC.t("&eThis will kick everyone from the server and convert all the player data in the specified folder to the new format set in the config (file or database)."));
            sender.sendMessage(CC.t("&eIf you are sure about this and want to proceed, run the command again."));
            sender.sendMessage("");
            Tasks.runLaterAsync(30 * 20, () -> {
                confirm.remove(sender);
                sender.sendMessage(CC.t("&cConversion confirmation expired."));
            });
            return;
        } else {
            confirm.remove(sender);
        }
        if (sender instanceof Player p)
            p.kickPlayer(CC.t("&cConverting player data... Please rejoin after a few seconds."));
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.kickPlayer(CC.t("&cPlease rejoin in a few seconds."));
        });
        int i = 0;
        for (final File file : playerdataDirectory.listFiles()) {
            if (file.isDirectory()) continue;
            String fileName = file.getName();
            UUID uuid = UUID.fromString(fileName.split(".yml")[0]);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            int kills = config.getInt("kills", 0);
            int deaths = config.getInt("deaths", 0);
            int bestKillstreak = config.getInt("killstreak", 0);
            FfaData ffaData = new FfaData(kills, deaths, bestKillstreak);
            UserData userData = new UserData(false, uuid, ffaData);
            plugin.getSM().getUserDataStorage().setUserData(userData);
            i++;
        }
        plugin.getLogger().info("Converted " + i + " player data files.");
        sender.sendMessage(CC.t("&aConverted " + i + " player data files."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

}
