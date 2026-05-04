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
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.bukkit.event.EventPriority.*;

public class CombatLogManager implements Listener {

    private final pFFA plugin;

    public CombatLogManager(pFFA plugin) {
        this.plugin = plugin;
        final ConfigurationSection combatlog = plugin.getConfig().getConfigurationSection("combatlog");

        if (!combatlog.getBoolean("enabled")) {
            plugin.getLogger().warning("Combatlog is disabled in the config, not loading Combatlog Manager.");
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        logoutBroadcast = combatlog.getString("logout_broadcast");
        combatLogDurationInSeconds = combatlog.getInt("duration");
        attackerMessage = combatlog.getString("attacker_message");
        if (attackerMessage == null || attackerMessage.isEmpty()) attackerMessage = null;
        victimMessage = combatlog.getString("victim_message");
        if (victimMessage == null || victimMessage.isEmpty()) victimMessage = null;


        final String actionBarString = combatlog.getString("display.action_bar");
        @Nullable final Component actionBar = actionBarString != null && !actionBarString.isEmpty()
                ? CC.a(actionBarString)
                : null;
        final boolean displayLevel = combatlog.getBoolean("display.level");
        final boolean displayExpbar = combatlog.getBoolean("display.exp_bar");
        Tasks.runRepeatingSync(2, 10, () -> {
            for (final Iterator<CombatLog> iterator = combatLogs.iterator(); iterator.hasNext(); ) {
                final CombatLog combatLog = iterator.next();

                if (combatLog.isExpired()) {
                    iterator.remove();
                    endCombat(combatLog, false);
                    continue;
                }

                final int secondsLeft = combatLog.getTimeLeftSeconds() + 1;
                final float timeLeft = Math.min(1.0f, Math.max(0.0f, secondsLeft / (float) combatLog.getDurationSeconds()));
                for (final Player player : combatLog.getPlayers()) {
                    if (actionBar != null)
                        player.sendActionBar(actionBar);
                    if (displayLevel)
                        player.setLevel(secondsLeft);
                    if (displayExpbar)
                        player.setExp(timeLeft);
                }
            }
        });
    }

    @EventHandler(priority = MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!plugin.getFfaWorlds().contains(event.getEntity().getWorld().getName())) return;
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker)
            startCombat(attacker, victim);
    }

    @EventHandler(priority = HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getFfaWorlds().contains(event.getEntity().getWorld().getName())) return;
        event.setDroppedExp(0);
        event.getDrops().clear();
        getCombatLogs(event.getEntity()).forEach(this::endCombat);
    }

    private String logoutBroadcast;

    @EventHandler(priority = LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getFfaWorlds().contains(event.getPlayer().getWorld().getName())) return;
        final Player player = event.getPlayer();
        final CombatLog combatLog = getLastCombatLog(player);
        if (combatLog != null) {
            final Player other = combatLog.getAttacker().equals(player) ? combatLog.getVictim() : combatLog.getAttacker();
            player.setKiller(other);
            player.setHealth(0);
            if (logoutBroadcast != null && !logoutBroadcast.isEmpty())
                plugin.getServer().broadcast(CC.af(logoutBroadcast, player.getName()));
        }
        getCombatLogs(player).forEach(this::endCombat);
    }

    private final List<CombatLog> combatLogs = new ArrayList<>();
    private final Map<UUID, List<CombatLog>> playerMap = new HashMap<>();

    private int combatLogDurationInSeconds;
    private String attackerMessage, victimMessage;

    private void startCombat(Player attacker, Player victim) {
        final CombatLog combatLog = getCombatLog(attacker, victim);
        if (combatLog == null) {
            if (!isInCombat(attacker) && attackerMessage != null)
                attacker.sendMessage(CC.af(attackerMessage, victim.getName()));
            if (!isInCombat(victim) && victimMessage != null)
                victim.sendMessage(CC.af(victimMessage, attacker.getName()));

            final CombatLog log = new CombatLog(attacker, victim, combatLogDurationInSeconds);
            combatLogs.add(log);
            indexCombatLog(log);
        } else
            combatLog.reset(combatLogDurationInSeconds);
    }

    private void endCombat(CombatLog combatLog) {
        endCombat(combatLog, true);
    }

    private void endCombat(CombatLog combatLog, boolean removeFromCombatLogs) {
        if (removeFromCombatLogs)
            combatLogs.remove(combatLog);
        unindexCombatLog(combatLog);

        for (final Player player : combatLog.getPlayers()) {
            player.sendActionBar(Component.empty());
            player.setLevel(0);
            player.setExp(0);
            player.sendMessage(CC.tf("&aYou are no longer in combat with %s", combatLog.getVictim() == player ? combatLog.getAttacker().getName() : combatLog.getVictim().getName()));
        }
    }

    private void indexCombatLog(CombatLog combatLog) {
        for (final Player player : combatLog.getPlayers()) {
            playerMap.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>()).add(combatLog);
        }
    }

    private void unindexCombatLog(CombatLog combatLog) {
        for (final Player player : combatLog.getPlayers()) {
            final List<CombatLog> logs = playerMap.get(player.getUniqueId());
            if (logs == null) continue;
            logs.remove(combatLog);
            if (logs.isEmpty()) playerMap.remove(player.getUniqueId());
        }
    }

    public boolean isInCombat(Player player) {
        final List<CombatLog> logs = playerMap.get(player.getUniqueId());
        return logs != null && !logs.isEmpty();
    }

    public List<CombatLog> getCombatLogs(Player player) {
        final List<CombatLog> logs = playerMap.get(player.getUniqueId());
        return logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    public CombatLog getLastCombatLog(Player player) {
        final List<CombatLog> logs = playerMap.get(player.getUniqueId());
        if (logs == null || logs.isEmpty()) return null;
        return logs.getLast();
    }

    public CombatLog getCombatLog(Player player1, Player player2) {
        final List<CombatLog> logs = playerMap.get(player1.getUniqueId());
        if (logs == null) return null;

        for (final CombatLog log : logs) {
            if (log.getAttacker() == player2 || log.getVictim() == player2) {
                return log;
            }
        }

        return null;
    }

}
