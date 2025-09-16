package me.pafias.pffa.commands;

import lombok.Getter;
import me.pafias.pffa.pFFA;
import me.pafias.putils.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseFFACommand {

    public pFFA plugin = pFFA.get();

    @Getter
    String name;

    @Getter
    Set<String> aliases;

    @Getter
    String permission;

    public BaseFFACommand(String name) {
        this.name = name;
        this.aliases = new HashSet<>();
        this.permission = null;
    }

    public BaseFFACommand(String name, String permission, String... aliases) {
        this.name = name;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.permission = permission;
    }

    public abstract String getArgs();

    public abstract String getDescription();

    public abstract void execute(String mainCommand, CommandSender sender, String[] args);

    public abstract List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);

    public void noPermission(CommandSender sender) {
        sender.sendMessage(CC.t("&cNo permission."));
    }

}
