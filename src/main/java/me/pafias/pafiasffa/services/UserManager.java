package me.pafias.pafiasffa.services;

import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.User;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;

public class UserManager {

    private final PafiasFFA plugin;

    private final Map<UUID, User> users = new HashMap<>();
    private final Map<String, User> usersByName = new WeakHashMap<>();

    public UserManager(PafiasFFA plugin, Variables variables) {
        this.plugin = plugin;
        startAutoSave(variables.dataSaveIntervalMinutes);
    }

    public Map<UUID, User> getUsers() {
        return users;
    }

    public User getUser(UUID uuid) {
        return users.get(uuid);
    }

    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(String name) {
        User user = usersByName.get(name.toLowerCase());
        if (user == null) {
            user = users.values().stream().filter(u -> u.getName().toLowerCase().startsWith(name.toLowerCase().trim())).findAny().orElse(null);
            if (user != null)
                usersByName.put(name.toLowerCase(), user);
        }
        return user;
    }

    public void addUser(Player player) {
        users.put(player.getUniqueId(), new User(player));
    }

    public void removeUser(Player player) {
        User user = getUser(player);
        removeUser(user);
    }

    public void removeUser(User user) {
        queueDataSave(user, true);
        if (plugin.getSM().getVariables().ffaWorlds.contains(user.getPlayer().getWorld().getName()))
            user.getPlayer().getInventory().clear();
        users.remove(user.getUUID());
    }

    private final Set<User> savingQueue = new HashSet<>();

    public void queueDataSave(User user, boolean forceSaveNow) {
        if (forceSaveNow) {
            savingQueue.remove(user);
            saveData(user);
        } else
            savingQueue.add(user);
    }

    private void saveData(User user) {
        int kills = user.getKills();
        int deaths = user.getDeaths();
        int ks = user.getBestKillstreak();
        try {
            user.getConfig().update("kills", kills);
            user.getConfig().update("deaths", deaths);
            user.getConfig().update("killstreak", ks);
        } catch (IOException e) {
            user.getPlayer().sendMessage(CC.t("&cFailed to save player data: Your kills and deaths will revert when you rejoin."));
            e.printStackTrace();
        }
    }

    private void startAutoSave(double saveInterval) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (savingQueue.isEmpty()) return;
                savingQueue.forEach(user -> saveData(user));
            }
        }.runTaskTimerAsynchronously(plugin, (long) (saveInterval * 60 * 20), (long) (saveInterval * 60 * 20));
    }

}
