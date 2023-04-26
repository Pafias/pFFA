package me.pafias.pafiasffa.commands.subcommands;

import me.pafias.pafiasffa.commands.ICommand;
import me.pafias.pafiasffa.objects.User;
import me.pafias.pafiasffa.objects.UserConfig;
import me.pafias.pafiasffa.util.CC;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditstatsCommand extends ICommand {

    public EditstatsCommand() {
        super("editstats", "ffa.editstats", "statsedit");
    }

    @Override
    public String getArgs() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Edit a player's statistics";
    }

    @Override
    public void execute(String mainCommand, CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            EditstatsListener el = new EditstatsListener((Player) sender);
            plugin.getServer().getPluginManager().registerEvents(el, plugin);
            sender.sendMessage(" ");
            sender.sendMessage(CC.t("&6You can cancel this operation at any time by typing 'cancel', 'exit', or 'abort'"));
            sender.sendMessage(" ");
            sender.sendMessage(CC.t("&6Type the name of the player"));
            sender.sendMessage(" ");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    class EditstatsListener implements Listener {

        Player sender;

        EditstatsListener(Player sender) {
            this.sender = sender;
        }

        private String player = null;
        private int kills = -1;
        private int deaths = -1;

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (event.getPlayer() != sender) return;
            if (event.getMessage().startsWith("/")) return;
            event.setCancelled(true);
            if (event.getMessage().equalsIgnoreCase("exit") || event.getMessage().equalsIgnoreCase("cancel")
                    || event.getMessage().equalsIgnoreCase("abort")) {
                cleanup();
                event.getPlayer().sendMessage(ChatColor.GOLD + "You cancelled the operation.");
                return;
            }
            if (player == null) {
                String target = event.getMessage().trim();
                if (plugin.getServer().getPlayer(target) == null) {
                    CompletableFuture.supplyAsync(() -> plugin.getServer().getOfflinePlayer(target).hasPlayedBefore()).thenAccept(exists -> {
                        if (!exists)
                            event.getPlayer().sendMessage(ChatColor.RED + "Player not found! Try again.");
                    });
                }
                player = target;
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(ChatColor.GREEN + "Player set: " + ChatColor.GRAY + player);
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(ChatColor.GOLD + "Now type the amount of " + ChatColor.LIGHT_PURPLE
                        + "kills" + ChatColor.GOLD + " to set to the player.");
                return;
            }
            if (kills == -1) {
                try {
                    kills = Integer.parseInt(event.getMessage().trim());
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Incorrect value. Try again.");
                    return;
                }
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(ChatColor.GREEN + "Kills set: " + ChatColor.GRAY + kills);
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(ChatColor.GOLD + "Now type the amount of " + ChatColor.LIGHT_PURPLE
                        + "deaths" + ChatColor.GOLD + " to set to the player.");
                return;
            }
            if (deaths == -1) {
                try {
                    deaths = Integer.parseInt(event.getMessage().trim());
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Incorrect value. Try again.");
                    return;
                }
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(ChatColor.GREEN + "Deaths set: " + ChatColor.GRAY + deaths);
                event.getPlayer().sendMessage(" ");
                if (player != null && kills != -1 && deaths != -1) {
                    User user = plugin.getSM().getUserManager().getUser(player);
                    if (user != null) {
                        user.setKills(kills);
                        user.setDeaths(deaths);
                    } else {
                        CompletableFuture.supplyAsync(() -> plugin.getServer().getOfflinePlayer(player)).thenAccept(offlinePlayer -> {
                            UserConfig config = new UserConfig(offlinePlayer.getUniqueId());
                            try {
                                config.update("kills", kills);
                                config.update("deaths", deaths);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                event.getPlayer().sendMessage(CC.t("&cSomething went wrong while saving: " + ex.getMessage()));
                                event.getPlayer().sendMessage(CC.t("&cOperation cancelled."));
                                cleanup();
                            }
                        });
                    }
                    event.getPlayer().sendMessage(ChatColor.GREEN + "All set.");
                    cleanup();
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Error: One or more values are null or invalid.");
                    event.getPlayer().sendMessage(ChatColor.RED + "Operation cancelled.");
                    cleanup();
                }
            }
        }

        private void cleanup() {
            player = null;
            kills = -1;
            deaths = -1;
            HandlerList.unregisterAll(this);
        }

    }

}
