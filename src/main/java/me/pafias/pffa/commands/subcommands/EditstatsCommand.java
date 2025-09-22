package me.pafias.pffa.commands.subcommands;

import me.pafias.pffa.commands.BaseFFACommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.UserData;
import me.pafias.putils.BukkitPlayerManager;
import me.pafias.putils.LCC;
import me.pafias.putils.Tasks;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditstatsCommand extends BaseFFACommand {

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
            final EditstatsListener el = new EditstatsListener((Player) sender);
            plugin.getServer().getPluginManager().registerEvents(el, plugin);
            sender.sendMessage(" ");
            sender.sendMessage(LCC.t("&6You can cancel this operation at any time by typing 'cancel', 'exit', or 'abort'"));
            sender.sendMessage(" ");
            sender.sendMessage(LCC.t("&6Type the name/uuid of the player"));
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

        private OfflinePlayer player = null;
        private int kills = -1;
        private int deaths = -1;
        private int killstreak = -1;

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (event.getPlayer() != sender) return;
            if (event.getMessage().startsWith("/")) return;
            event.setCancelled(true);
            if (event.getMessage().equalsIgnoreCase("exit") || event.getMessage().equalsIgnoreCase("cancel")
                    || event.getMessage().equalsIgnoreCase("abort")) {
                cleanup();
                event.getPlayer().sendMessage(LCC.t("&cYou cancelled the operation."));
                return;
            }
            if (player == null) {
                event.getPlayer().sendMessage(LCC.t("&6Fetching player..."));
                final String target = event.getMessage().trim();
                CompletableFuture.supplyAsync(() -> BukkitPlayerManager.getOfflinePlayerByInput(target))
                        .thenAccept(offlinePlayer -> {
                            Tasks.runSync(() -> {
                                if (offlinePlayer == null)
                                    event.getPlayer().sendMessage(LCC.t("&cPlayer not found! Try again."));
                                else {
                                    player = offlinePlayer;
                                    event.getPlayer().sendMessage(" ");
                                    event.getPlayer().sendMessage(LCC.tf("&aPlayer set: &7%s", player.getName()));
                                    event.getPlayer().sendMessage(" ");
                                    event.getPlayer().performCommand("ffa stats " + player.getUniqueId());
                                    event.getPlayer().sendMessage(" ");
                                    event.getPlayer().sendMessage(LCC.t("&6Now type the amount of &dkills &6to set to the player."));
                                }
                            });
                        });
                return;
            }
            if (kills == -1) {
                try {
                    kills = Integer.parseInt(event.getMessage().trim());
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(LCC.t("&cIncorrect value. Try again."));
                    return;
                }
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(LCC.tf("&aKills set: &7%d", kills));
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(LCC.t("&6Now type the amount of &ddeaths &6to set to the player."));
                return;
            }
            if (deaths == -1) {
                try {
                    deaths = Integer.parseInt(event.getMessage().trim());
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(LCC.t("&cIncorrect value. Try again."));
                    return;
                }
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(LCC.tf("&aDeaths set: &7", deaths));
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(LCC.t("&6Now type the value of &dbest killstreak &6to set to the player."));
                return;
            }
            if (killstreak == -1) {
                try {
                    killstreak = Integer.parseInt(event.getMessage().trim());
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(LCC.t("&cIncorrect value. Try again."));
                    return;
                }
                event.getPlayer().sendMessage(" ");
                event.getPlayer().sendMessage(LCC.tf("&aBest killstreak set: &7", killstreak));
                event.getPlayer().sendMessage(" ");
                if (player != null && kills != -1 && deaths != -1 && killstreak != -1) {
                    final User user = plugin.getSM().getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                        user.setKills(kills);
                        user.setDeaths(deaths);
                        user.setBestKillstreak(killstreak);
                    } else {
                        final FfaData ffaData = new FfaData(kills, deaths, killstreak);
                        final UserData userData = new UserData(false, player.getUniqueId(), ffaData);
                        try {
                            plugin.getSM().getUserDataStorage().setUserData(userData);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            event.getPlayer().sendMessage(LCC.t("&cSomething went wrong while saving: " + ex.getMessage()));
                            event.getPlayer().sendMessage(LCC.t("&cOperation cancelled."));
                            cleanup();
                        }
                    }
                    event.getPlayer().sendMessage(LCC.t("&aAll set."));
                    cleanup();
                } else {
                    event.getPlayer().sendMessage(LCC.t("&cError: One or more values are null or invalid."));
                    event.getPlayer().sendMessage(LCC.t("&cOperation cancelled."));
                    cleanup();
                }
            }
        }

        private void cleanup() {
            player = null;
            kills = -1;
            deaths = -1;
            killstreak = -1;
            HandlerList.unregisterAll(this);
        }

    }

}
