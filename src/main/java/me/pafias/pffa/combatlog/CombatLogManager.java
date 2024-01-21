package me.pafias.pffa.combatlog;

import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.Variables;
import me.pafias.pffa.util.CC;
import me.pafias.pffa.util.Reflection;
import me.pafias.pffa.util.Tasks;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.event.EventPriority.*;

public class CombatLogManager implements Listener {

    private final pFFA plugin;

    private final List<CombatLog> combatLogs = new ArrayList<>();
    private int combatLogDurationInSeconds;

    public CombatLogManager(pFFA plugin, Variables variables) {
        this.plugin = plugin;
        ConfigurationSection combatlog = variables.combatlog;

        if (!combatlog.getBoolean("enabled")) return;

        combatLogDurationInSeconds = combatlog.getInt("duration", 10);

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
                    if (combatlog.getBoolean("display.action_bar"))
                        Reflection.sendActionbar(player, CC.t("&aYou are in combat. &cDo not log out!"));
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
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getEntity().getWorld().getName())) return;
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            startCombat(attacker, victim);
        }
    }

    @EventHandler(priority = HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getEntity().getWorld().getName())) return;
        event.setDroppedExp(0);
        event.getDrops().clear();
        getCombatLogs(event.getEntity()).forEach(this::endCombat);
    }

    @EventHandler(priority = LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getSM().getVariables().ffaWorlds.contains(event.getPlayer().getWorld().getName())) return;
        Player player = event.getPlayer();
        CombatLog combatLog = getLastCombatLog(player);
        if (combatLog != null) {
            Player other = combatLog.getAttacker().equals(player) ? combatLog.getVictim() : combatLog.getAttacker();
            try {
                Class c = player.getClass();
                Method method = c.getMethod("setKiller", Player.class);
                method.invoke(player, other);
            } catch (Exception ex) {
                ex.printStackTrace();
                player.damage(1, other);
            }
            player.setHealth(0);
            plugin.getServer().broadcastMessage(CC.tf("&b%s &clogged out while in combat!", player.getName()));
        }
        getCombatLogs(player).forEach(this::endCombat);
    }

    private void startCombat(Player attacker, Player victim) {
        CombatLog combatLog = getCombatLog(attacker, victim);
        if (combatLog == null) {
            if (getCombatLogs(attacker).isEmpty())
                attacker.sendMessage(CC.tf("&aYou attacked &b%s&c. Do not log out", victim.getName()));
            if (getCombatLogs(victim).isEmpty())
                victim.sendMessage(CC.tf("&cYou got attacked by &b%s&c. Do not log out", attacker.getName()));
            combatLogs.add(new CombatLog(attacker, victim, combatLogDurationInSeconds));
        } else
            combatLog.reset(combatLogDurationInSeconds);
    }

    private void endCombat(CombatLog combatLog) {
        combatLogs.remove(combatLog);
        combatLog.getPlayers().forEach(player -> {
            Reflection.sendActionbar(player, "");
            player.setLevel(0);
            player.setExp(0);
            player.sendMessage(CC.t("&aYou are no longer in combat"));
        });
    }

    public boolean isInCombat(Player player) {
        return !getCombatLogs(player).isEmpty();
    }

    public List<CombatLog> getCombatLogs(Player player) {
        return combatLogs.stream().filter(c -> c.getPlayers().contains(player)).collect(Collectors.toList());
    }

    public CombatLog getLastCombatLog(Player player) {
        List<CombatLog> list = getCombatLogs(player);
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }

    public CombatLog getCombatLog(Player player1, Player player2) {
        return combatLogs.stream().filter(c -> c.getPlayers().contains(player1) && c.getPlayers().contains(player2)).findAny().orElse(null);
    }

}
