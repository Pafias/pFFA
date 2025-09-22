package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.putils.LCC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends BaseFFACommand {

    public ReloadCommand() {
        super("reload", "ffa.reload", "reloadconfig");
    }

    @NotNull
    @Override
    public String getArgs() {
        return "";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Reload the config";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        plugin.reloadConfig();
        sender.sendMessage(LCC.t("&aConfig reloaded. &7Some changes may require a server restart to take effect:"));
        sender.sendMessage(LCC.t("&7- Combatlog: Changing the 'enabled' option"));
        sender.sendMessage(LCC.t("&7- Death messages: Changing anything in deathmessages.yml"));
        try {
            plugin.getSM().getKitManager().loadKits();
            sender.sendMessage(LCC.t("&aKits reloaded."));
        } catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage(LCC.t("&cFailed to reload kits."));
        }
        try {
            plugin.getSM().getSpawnManager().loadSpawns();
            sender.sendMessage(LCC.t("&aSpawns reloaded."));
        } catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage(LCC.t("&cFailed to reload spawns."));
        }
        try {
            for (RegisteredListener listener : HandlerList.getRegisteredListeners(plugin)) {
                HandlerList.unregisterAll(listener.getListener());
            }
            plugin.register();
        } catch (Exception ex) {
            ex.printStackTrace();
            sender.sendMessage(LCC.t("&cFailed to reload listeners."));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

}
