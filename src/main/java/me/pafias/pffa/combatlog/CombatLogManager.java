package me.pafias.pffa.combatlog;

import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.Reflection;
import me.pafias.putils.LCC;
import me.pafias.putils.Tasks;
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
import java.util.stream.Collectors;

import static org.bukkit.event.EventPriority.*;

public class CombatLogManager implements Listener {

    private final pFFA plugin;

    public CombatLogManager(pFFA plugin) {
        this.plugin = plugin;
        ConfigurationSection combatlog = plugin.getConfig().getConfigurationSection("combatlog");

        if (!combatlog.getBoolean("enabled")) {
            plugin.getLogger().warning("Combatlog is disabled in the config, not loading Combatlog Manager.");
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        Tasks.runRepeatingSync(2, 10, () -> {
            for (Iterator<CombatLog> iterator = combatLogs.iterator(); iterator.hasNext(); ) {
                CombatLog combatLog = iterator.next();

                if (combatLog.isExpired()) {
                    iterator.remove();
                    endCombat(combatLog);
                    continue;
                }

                int secondsLeft = combatLog.getTimeLeftSeconds() + 1;
                combatLog.getPlayers().forEach(player -> {
                    String actionBar = combatlog.getString("display.action_bar");
                    if (actionBar != null && !actionBar.isEmpty())
                        Reflection.sendActionbar(player, LCC.t(actionBar));
                    if (combatlog.getBoolean("display.level"))
                        player.setLevel(secondsLeft);
                    if (combatlog.getBoolean("display.exp_bar"))
                        player.setExp(Math.min(1.0f, Math.max(0.0f, secondsLeft / (float) combatLog.getDurationSeconds())));
                });
            }
        });
    }

    @EventHandler(priority = MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!plugin.getConfig().getStringList("ffa_worlds").contains(event.getEntity().getWorld().getName())) return;
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player)
            startCombat((Player) event.getDamager(), (Player) event.getEntity());
    }

    @EventHandler(priority = HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getStringList("ffa_worlds").contains(event.getEntity().getWorld().getName())) return;
        event.setDroppedExp(0);
        event.getDrops().clear();
        getCombatLogs(event.getEntity()).forEach(this::endCombat);
    }

    @EventHandler(priority = LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfig().getStringList("ffa_worlds").contains(event.getPlayer().getWorld().getName())) return;
        Player player = event.getPlayer();
        CombatLog combatLog = getLastCombatLog(player);
        if (combatLog != null) {
            Player other = combatLog.getAttacker().equals(player) ? combatLog.getVictim() : combatLog.getAttacker();
            Reflection.setKiller(player, other);
            player.setHealth(0);
            String logoutBroadcast = plugin.getConfig().getString("combatlog.logout_broadcast");
            if (logoutBroadcast != null && !logoutBroadcast.isEmpty())
                plugin.getServer().broadcastMessage(LCC.tf(logoutBroadcast, player.getName()));
        }
        getCombatLogs(player).forEach(this::endCombat);
    }

    private final List<CombatLog> combatLogs = new ArrayList<>();

    private void startCombat(Player attacker, Player victim) {
        CombatLog combatLog = getCombatLog(attacker, victim);
        int combatLogDurationInSeconds = plugin.getConfig().getInt("combatlog.duration");
        if (combatLog == null) {
            if (getCombatLogs(attacker).isEmpty()) {
                String message = plugin.getConfig().getString("combatlog.attacker_message");
                if (message != null && !message.isEmpty())
                    attacker.sendMessage(LCC.tf(message, victim.getName()));
            }
            if (getCombatLogs(victim).isEmpty()) {
                String message = plugin.getConfig().getString("combatlog.victim_message");
                if (message != null && !message.isEmpty())
                    victim.sendMessage(LCC.tf(message, attacker.getName()));
            }
            combatLogs.add(new CombatLog(attacker, victim, combatLogDurationInSeconds));
        } else
            combatLog.reset(combatLogDurationInSeconds);
    }

    private void endCombat(CombatLog combatLog) {
        combatLogs.remove(combatLog);
        for (Player player : combatLog.getPlayers()) {
            Reflection.sendActionbar(player, "");
            player.setLevel(0);
            player.setExp(0);
            player.sendMessage(LCC.tf("&aYou are no longer in combat with %s", combatLog.getVictim() == player ? combatLog.getAttacker().getName() : combatLog.getVictim().getName()));
        }
    }

    public boolean isInCombat(Player player) {
        return !getCombatLogs(player).isEmpty();
    }

    public List<CombatLog> getCombatLogs(Player player) {
        return combatLogs.stream()
                .filter(c -> c.getPlayers().contains(player))
                .collect(Collectors.toList());
    }

    public CombatLog getLastCombatLog(Player player) {
        List<CombatLog> list = getCombatLogs(player);
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }

    public CombatLog getCombatLog(Player player1, Player player2) {
        return combatLogs.stream()
                .filter(c -> c.getPlayers().contains(player1) && c.getPlayers().contains(player2))
                .findAny().orElse(null);
    }

}
