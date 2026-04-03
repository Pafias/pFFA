package me.pafias.pffa.services;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.objects.exceptions.UserLoadingException;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.storage.UserDataStorage;
import me.pafias.putils.LCC;
import me.pafias.putils.Tasks;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class UserManager {

    private final pFFA plugin;
    private final UserDataStorage userDataStorage;

    private final ExecutorService executor;

    public UserManager(pFFA plugin,
                       UserDataStorage userDataStorage,
                       int dataSaveIntervalMinutes) {
        this.plugin = plugin;
        this.userDataStorage = userDataStorage;
        this.executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("pFFA-UserManager-%d").build());
        startAutoSave(dataSaveIntervalMinutes);
    }

    @Getter
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    public User getUser(final UUID uuid) {
        return users.get(uuid);
    }

    public User getUser(final Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(String name) {
        for (User user : users.values()) {
            if (user.getName().equalsIgnoreCase(name) || user.getName().toLowerCase().startsWith(name.toLowerCase()))
                return user;
        }
        return null;
    }

    private final Map<UUID, CompletableFuture<UserData>> preloadedData = new ConcurrentHashMap<>();

    public CompletableFuture<UserData> loadUser(final UUID uuid) {
        return preloadedData.computeIfAbsent(uuid, id ->
                CompletableFuture.supplyAsync(() -> {
                    UserData userData = userDataStorage.getUserData(id.toString());
                    if (userData == null)
                        userData = new UserData(false, id, new FfaData(0, 0, 0));
                    return userData;
                }, executor));
    }

    public User addUser(final Player player) throws UserLoadingException {
        CompletableFuture<UserData> future = preloadedData.get(player.getUniqueId());
        if (future == null)
            future = loadUser(player.getUniqueId());

        UserData userData;
        try {
            userData = future.getNow(null);
            if (userData == null) {
                userData = new UserData(true, player.getUniqueId(), new FfaData(0, 0, 0));
                plugin.getLogger().warning("Player " + player.getName() +
                        " joined before data load completed. Using temporary data.");
                future.thenAcceptAsync(loaded -> {
                    Tasks.runSync(() -> {
                        User user = getUser(player);
                        if (user != null) {
                            user.setUserData(loaded);
                            plugin.getLogger().info("UserData late loaded for " + player.getName());
                        }
                    });
                });
            }
        } catch (Exception e) {
            preloadedData.remove(player.getUniqueId());
            throw new UserLoadingException("Failed to load user data for " + player.getName(), e);
        }

        preloadedData.remove(player.getUniqueId());

        final User user = new User(player, userData);
        users.put(player.getUniqueId(), user);
        return user;
    }

    public User removeUser(final Player player) {
        final User user = getUser(player);
        removeUser(user, true);
        return user;
    }

    public void removeUser(final User user, boolean saveData) {
        if (saveData)
            queueDataSave(user, true, true);
        users.remove(user.getUniqueId());
    }

    private final Set<User> savingQueue = ConcurrentHashMap.newKeySet();

    public void queueDataSave(final User user, boolean forceSaveNow, boolean async) {
        if (forceSaveNow) {
            saveData(user, async);
            savingQueue.remove(user);
        } else
            savingQueue.add(user);
    }

    private void saveData(final User user, boolean async) {
        if (user == null || user.getUserData() == null || user.getUserData().isTemp()) return;
        try {
            if (async)
                executor.submit(() -> userDataStorage.setUserData(user.getUserData()));
            else
                userDataStorage.setUserData(user.getUserData());
            savingQueue.remove(user);
        } catch (Exception e) {
            if (user.getPlayer().isOnline())
                user.getPlayer().sendMessage(LCC.t("&cFailed to save player data: Your statistics will revert when you rejoin."));
            e.printStackTrace();
        }
    }

    private void startAutoSave(double saveInterval) {
        long interval = (long) (saveInterval * 60 * 20);
        Tasks.runRepeatingAsync(interval, interval, () -> {
            if (savingQueue.isEmpty()) return;
            savingQueue.forEach(user -> saveData(user, true));
        });
    }

    public void saveAllSync() {
        for (final User user : users.values()) {
            saveData(user, false);
            removeUser(user, false);
        }
    }

    public void shutdown() {
        saveAllSync();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    plugin.getLogger().severe("UserManager executor did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
