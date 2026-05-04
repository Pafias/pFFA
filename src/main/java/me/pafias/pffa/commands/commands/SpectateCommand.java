package me.pafias.pffa.commands.commands;

import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.putils.CC;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SpectateCommand implements CommandExecutor, TabExecutor {

    private final pFFA plugin;

    public SpectateCommand(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.t("&cOnly players."));
            return true;
        }
        if (plugin.getSM().getCombatLogManager().isInCombat(player)) {
            sender.sendMessage(CC.t("&cYou cannot spectate while in combat."));
            return true;
        }
        final User user = plugin.getSM().getUserManager().getUser(player);
        if (!user.isSpectating() && !plugin.getFfaWorlds().contains(player.getWorld().getName())) {
            sender.sendMessage(CC.t("&cYou can only spectate in FFA worlds."));
            return true;
        }
        final Player targetPlayer = args.length == 1 ? plugin.getServer().getPlayer(args[0]) : null;
        final Location targetLocation = targetPlayer != null && player.canSee(targetPlayer) ? targetPlayer.getEyeLocation() : player.getEyeLocation();
        if (user.isSpectating()) {
            player.teleport(plugin.getLobbySpawn());
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
        } else {
            player.teleport(targetLocation);
            player.setGameMode(GameMode.SPECTATOR);
        }
        user.setSpectating(!user.isSpectating());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return plugin.getServer().getOnlinePlayers()
                    .stream()
                    .filter(player -> ((Player) sender).canSee(player))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        return Collections.emptyList();
    }

}
