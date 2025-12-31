package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.putils.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends BaseFFACommand {

    public ReloadCommand() {
        super("reload", "ffa.reload");
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
        boolean error = false;
        try {
            for (RegisteredListener listener : HandlerList.getRegisteredListeners(plugin)) {
                HandlerList.unregisterAll(listener.getListener());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error = true;
        }
        try {
            plugin.getSM().onLoad();
            plugin.getSM().onEnable();
        } catch (Exception ex) {
            ex.printStackTrace();
            error = true;
        }
        try {
            plugin.register();
        } catch (Exception ex) {
            ex.printStackTrace();
            error = true;
        }
        plugin.getServer().getOnlinePlayers()
                .stream()
                .filter(p -> !p.hasMetadata("NPC"))
                .forEach(p -> plugin.getSM().getUserManager().addUser(p));
        if (error) {
            sender.sendMessage(CC.t("&cSomething went wrong while reloading the plugin. Restarting the server is recommended to avoid issues."));
        } else {
            sender.sendMessage(CC.t("&aPlugin reloaded."));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

}
