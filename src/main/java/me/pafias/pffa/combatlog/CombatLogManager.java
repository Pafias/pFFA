package me.pafias.pffa.combatlog;

import me.pafias.pffa.pFFA;
import me.pafias.putils.CC;
import me.pafias.putils.Tasks;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.bukkit.event.EventPriority.*;

public class CombatLogManager implements Listener {

    private final pFFA plugin;

    public CombatLogManager(pFFA plugin) {
        this.plugin = plugin;
        this.ffaWorlds = Set.copyOf(plugin.getConfig().getStringList("ffa_worlds"));
        final ConfigurationSection combatlog = plugin.getConfig().getConfigurationSection("combatlog");

        if (!combatlog.getBoolean("enabled")) {
            plugin.getLogger().warning("Combatlog is disabled in the config, not loading Combatlog Manager.");
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        Tasks.runRepeatingSync(2, 10, () -> {
            for (final Iterator<CombatLog> iterator = combatLogs.iterator(); iterator.hasNext(); ) {
                final CombatLog combatLog = iterator.next();

                if (combatLog.isExpired()) {
                    iterator.remove();
                    endCombat(combatLog);
                    continue;
                }

                final int secondsLeft = combatLog.getTimeLeftSeconds() + 1;
                combatLog.getPlayers().forEach(player -> {
                    final String actionBar = combatlog.getString("display.action_bar");
                    if (actionBar != null && !actionBar.isEmpty())
                        player.sendActionBar(CC.a(actionBar));
                    if (combatlog.getBoolean("display.level"))
                        player.setLevel(secondsLeft);
                    if (combatlog.getBoolean("display.exp_bar"))
                        player.setExp(Math.min(1.0f, Math.max(0.0f, secondsLeft / (float) combatLog.getDurationSeconds())));
                });
            }
        });
    }

    private final Set<String> ffaWorlds;

    @EventHandler(priority = MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!ffaWorlds.contains(event.getEntity().getWorld().getName())) return;
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker)
            startCombat(attacker, victim);
    }

    @EventHandler(priority = HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!ffaWorlds.contains(event.getEntity().getWorld().getName())) return;
        event.setDroppedExp(0);
        event.getDrops().clear();
        getCombatLogs(event.getEntity()).forEach(this::endCombat);
    }

    @EventHandler(priority = LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        final Player player = event.getPlayer();
        final CombatLog combatLog = getLastCombatLog(player);
        if (combatLog != null) {
            final Player other = combatLog.getAttacker().equals(player) ? combatLog.getVictim() : combatLog.getAttacker();
            player.setKiller(other);
            player.setHealth(0);
            final String logoutBroadcast = plugin.getConfig().getString("combatlog.logout_broadcast");
            if (logoutBroadcast != null && !logoutBroadcast.isEmpty())
                plugin.getServer().broadcast(CC.af(logoutBroadcast, player.getName()));
        }
        getCombatLogs(player).forEach(this::endCombat);
    }

    private final List<CombatLog> combatLogs = new ArrayList<>();

    private void startCombat(Player attacker, Player victim) {
        final CombatLog combatLog = getCombatLog(attacker, victim);
        int combatLogDurationInSeconds = plugin.getConfig().getInt("combatlog.duration");
        if (combatLog == null) {
            if (getCombatLogs(attacker).isEmpty()) {
                final String message = plugin.getConfig().getString("combatlog.attacker_message");
                if (message != null && !message.isEmpty())
                    attacker.sendMessage(CC.af(message, victim.getName()));
            }
            if (getCombatLogs(victim).isEmpty()) {
                final String message = plugin.getConfig().getString("combatlog.victim_message");
                if (message != null && !message.isEmpty())
                    victim.sendMessage(CC.af(message, attacker.getName()));
            }
            combatLogs.add(new CombatLog(attacker, victim, combatLogDurationInSeconds));
        } else
            combatLog.reset(combatLogDurationInSeconds);
    }

    private void endCombat(CombatLog combatLog) {
        combatLogs.remove(combatLog);
        for (final Player player : combatLog.getPlayers()) {
            player.sendActionBar(Component.empty());
            player.setLevel(0);
            player.setExp(0);
            player.sendMessage(CC.tf("&aYou are no longer in combat with %s", combatLog.getVictim() == player ? combatLog.getAttacker().getName() : combatLog.getVictim().getName()));
        }
    }

    public boolean isInCombat(Player player) {
        return !getCombatLogs(player).isEmpty();
    }

    public List<CombatLog> getCombatLogs(Player player) {
        final List<CombatLog> list = new ArrayList<>();
        for (final CombatLog combatLog : combatLogs) {
            if (combatLog.getPlayers().contains(player)) {
                list.add(combatLog);
            }
        }
        return list;
    }

    public CombatLog getLastCombatLog(Player player) {
        final List<CombatLog> list = getCombatLogs(player);
        if (list.isEmpty()) return null;
        return list.getLast();
    }

    public CombatLog getCombatLog(Player player1, Player player2) {
        for (final CombatLog combatLog : combatLogs) {
            if (combatLog.getPlayers().contains(player1) && combatLog.getPlayers().contains(player2))
                return combatLog;
        }
        return null;
    }

}
