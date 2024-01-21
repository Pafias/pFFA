package me.pafias.pffa.listeners;

import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.CC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.util.List;
import java.util.Random;

public class DeathMessagesHandler implements Listener {

    public DeathMessagesHandler(pFFA plugin) {
        File file = new File(plugin.getDataFolder(), "deathmessages.yml");
        plugin.saveResource("deathmessages.yml", !file.exists());
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("enabled")) return;
        messages = config.getConfigurationSection("messages");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private ConfigurationSection messages;

    private String getRandomMessageWithKiller(String string, Player player, Player killer) {
        List<String> list = messages.getStringList("with_killer." + string);
        String var = list.get(new Random().nextInt(list.size()));
        var = var.replace("{player}", player.getName());
        var = var.replace("{killer}", killer.getName());
        var = var.replace("{health}", String.format("%.1f", killer.getPlayer().getHealth() / 2d));
        return CC.t(var.trim());
    }

    private String getRandomMessageWithOUTKiller(String string, Player player) {
        List<String> list = messages.getStringList("without_killer." + string);
        String var = list.get(new Random().nextInt(list.size()));
        var = var.replace("{player}", player.getName());
        return CC.t(var.trim());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent event) {
        if (event.getDeathMessage() == null) return;
        Player player = event.getEntity();
        if (player.getKiller() != null) {
            Player killer = player.getKiller();
            if (event.getDeathMessage().contains("hit the ground too hard") || event.getDeathMessage().contains("fell from a high place")) {
                event.setDeathMessage(getRandomMessageWithKiller("hit_the_ground_too_hard", player, killer));
            } else if (event.getDeathMessage().contains("drowned")) {
                event.setDeathMessage(getRandomMessageWithKiller("drowned", player, killer));
            } else if (event.getDeathMessage().contains("swim in lava")) {
                event.setDeathMessage(getRandomMessageWithKiller("swim_in_lava", player, killer));
            } else if (event.getDeathMessage().contains("struck by lightning")) {
                event.setDeathMessage(getRandomMessageWithKiller("struck_by_lightning", player, killer));
            } else if (event.getDeathMessage().contains("blew up") || event.getDeathMessage().contains("blown up")) {
                event.setDeathMessage(getRandomMessageWithKiller("blew_up", player, killer));
            } else if (event.getDeathMessage().contains("went up in flames") || event.getDeathMessage().contains("burned to death")) {
                event.setDeathMessage(getRandomMessageWithKiller("burned", player, killer));
            } else if (event.getDeathMessage().contains("shot")) {
                event.setDeathMessage(getRandomMessageWithKiller("shot", player, killer));
            } else if (event.getDeathMessage().contains("pricked to death") || event.getDeathMessage().contains("walked into a cactus")) {
                event.setDeathMessage(getRandomMessageWithKiller("pricked", player, killer));
            } else if (event.getDeathMessage().contains("falling anvil")) {
                event.setDeathMessage(getRandomMessageWithKiller("falling_anvil", player, killer));
            } else if (event.getDeathMessage().contains("starved")) {
                event.setDeathMessage(getRandomMessageWithKiller("starved", player, killer));
            } else if (event.getDeathMessage().contains("suffocated in")) {
                event.setDeathMessage(getRandomMessageWithKiller("suffocated", player, killer));
            } else if (event.getDeathMessage().contains("fell out of the world")) {
                event.setDeathMessage(getRandomMessageWithKiller("fell_out_of_world", player, killer));
            } else if (event.getDeathMessage().contains("was slain by")) {
                event.setDeathMessage(getRandomMessageWithKiller("was_slain_by", player, killer));
            } else {
                event.setDeathMessage(getRandomMessageWithKiller("other", player, killer));
            }
        } else {
            if (event.getDeathMessage().contains("hit the ground too hard") || event.getDeathMessage().contains("fell from a high place")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("hit_the_ground_too_hard", player));
            } else if (event.getDeathMessage().contains("drowned")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("drowned", player));
            } else if (event.getDeathMessage().contains("swim in lava")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("swim_in_lava", player));
            } else if (event.getDeathMessage().contains("struck by lightning")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("struck_by_lightning", player));
            } else if (event.getDeathMessage().contains("blew up") || event.getDeathMessage().contains("blown up")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("blew_up", player));
            } else if (event.getDeathMessage().contains("went up in flames") || event.getDeathMessage().contains("burned to death")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("burned", player));
            } else if (event.getDeathMessage().contains("shot")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("shot", player));
            } else if (event.getDeathMessage().contains("pricked to death") || event.getDeathMessage().contains("walked into a cactus")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("pricked", player));
            } else if (event.getDeathMessage().contains("falling anvil")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("falling_anvil", player));
            } else if (event.getDeathMessage().contains("starved")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("starved", player));
            } else if (event.getDeathMessage().contains("suffocated in")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("suffocated", player));
            } else if (event.getDeathMessage().contains("fell out of the world")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("fell_out_of_world", player));
            } else if (event.getDeathMessage().contains("was slain by")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("was_slain_by", player));
            } else if (event.getDeathMessage().toLowerCase().contains("intentional game design")) {
                event.setDeathMessage(getRandomMessageWithOUTKiller("intentional_game_design", player));
            } else {
                event.setDeathMessage(getRandomMessageWithOUTKiller("other", player));
            }
        }
    }

}
