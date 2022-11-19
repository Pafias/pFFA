package me.pafias.pafiasffa.services;

import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.User;
import me.pafias.pafiasffa.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserManager {

    private PafiasFFA plugin;

    private final Set<User> users;

    public UserManager(PafiasFFA plugin) {
        this.plugin = plugin;
        users = new HashSet<>();
        startAutoSave();
    }

    public Set<User> getUsers() {
        return users;
    }

    public User getUser(UUID uuid) {
        return users.stream().filter(u -> u.getUUID().equals(uuid)).findAny().orElse(null);
    }

    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(String name) {
        return users.stream().filter(u -> u.getName().toLowerCase().startsWith(name.toLowerCase().trim())).findAny().orElse(null);
    }

    public void addUser(Player player) {
        users.add(new User(player));
    }

    public void removeUser(Player player) {
        User user = getUser(player);
        removeUser(user);
    }

    public void removeUser(User user) {
        queueDataSave(user, true);
        if (plugin.getSM().getVariables().ffaWorlds.contains(user.getPlayer().getWorld().getName()))
            user.getPlayer().getInventory().clear();
        users.remove(user);
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
        try {
            user.getConfig().update("kills", user.getKills());
            user.getConfig().update("deaths", user.getDeaths());
        } catch (IOException e) {
            user.getPlayer().sendMessage(CC.t("&cFailed to save player data: Your kills and deaths will revert when you rejoin."));
            e.printStackTrace();
        }
    }

    private void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (savingQueue.isEmpty()) return;
                savingQueue.forEach(user -> saveData(user));
            }
        }.runTaskTimerAsynchronously(plugin, (60 * 20), (60 * 20));
    }

}
