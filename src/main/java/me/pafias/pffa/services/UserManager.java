package me.pafias.pffa.services;

import lombok.Getter;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.objects.exceptions.UserLoadingException;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.storage.UserDataStorage;
import me.pafias.putils.CC;
import me.pafias.putils.Tasks;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final pFFA plugin;
    private final UserDataStorage userDataStorage;

    public UserManager(pFFA plugin,
                       UserDataStorage userDataStorage,
                       int dataSaveIntervalMinutes) {
        this.plugin = plugin;
        this.userDataStorage = userDataStorage;
        startAutoSave(dataSaveIntervalMinutes);
        startPeriodicCleanup();
    }

    private static final long DATA_EXPIRY_MILLIS = 60_000L; // 60 seconds

    @Getter
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    public User getUser(final UUID uuid) {
        return users.get(uuid);
    }

    public User getUser(final Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(final String name) {
        final String nameLower = name.toLowerCase();
        for (User user : users.values()) {
            if (user.getName().equalsIgnoreCase(nameLower) || user.getName().toLowerCase().startsWith(nameLower))
                return user;
        }
        return null;
    }

    private final Map<UUID, PreloadedData> preloadedData = new ConcurrentHashMap<>();

    public UserData loadUser(final UUID uuid) throws UserLoadingException {
        UserData userData = userDataStorage.getUserData(uuid.toString());
        if (userData == null)
            userData = new UserData(uuid, new FfaData(0, 0, 0));
        preloadedData.put(uuid, new PreloadedData(userData, System.currentTimeMillis() + DATA_EXPIRY_MILLIS));
        return userData;
    }

    public User addUser(final Player player) throws UserLoadingException {
        final UUID uuid = player.getUniqueId();
        final PreloadedData preloaded = preloadedData.remove(uuid);
        if (preloaded == null || preloaded.expiresAt() <= System.currentTimeMillis())
            throw new UserLoadingException("Failed to load player data for " + player.getName());
        final User user = new User(player, preloaded.data());
        users.put(uuid, user);
        return user;
    }

    public User removeUser(final Player player) {
        final User user = users.remove(player.getUniqueId());
        if (user != null)
            queueDataSave(user, true, true);
        return user;
    }

    public void removeUser(final User user, boolean saveData) {
        if (user == null)
            return;
        users.remove(user.getUniqueId());
        if (saveData)
            queueDataSave(user, true, true);
    }

    private final Set<User> savingQueue = ConcurrentHashMap.newKeySet();

    public void queueDataSave(final User user, boolean forceSaveNow, boolean async) {
        if (forceSaveNow) {
            saveData(user, async);
        } else
            savingQueue.add(user);
    }

    private void saveData(final User user, boolean async) {
        if (user == null)
            return;

        final UserData data = user.getUserData().copy();
        if (async) {
            Tasks.runAsync(() -> performSave(user, data));
        } else {
            performSave(user, data);
        }
    }

    private void performSave(final User user, final UserData data) {
        try {
            userDataStorage.setUserData(data);
            savingQueue.remove(user);
        } catch (Exception e) {
            savingQueue.add(user);

            plugin.getLogger().severe("Failed to save data for " + user.getUniqueId());
            e.printStackTrace();

            Tasks.runSync(() -> {
                final Player player = user.getPlayer();
                if (player != null && player.isOnline())
                    player.sendMessage(CC.t("&cFailed to save player data: Your statistics may revert when you rejoin."));
            });
        }
    }

    private void startAutoSave(int saveIntervalMinutes) {
        final long intervalTicks = saveIntervalMinutes * 60L * 20L;
        Tasks.runRepeatingSync(intervalTicks, intervalTicks, () -> {
            for (User user : savingQueue) {
                saveData(user, true);
            }
        });
    }

    private void startPeriodicCleanup() {
        Tasks.runRepeatingSync(60 * 20L, 60 * 20L, () -> {
            final long now = System.currentTimeMillis();
            preloadedData.entrySet().removeIf(
                    entry -> entry.getValue().expiresAt() <= now
            );
        });
    }

    public void shutdown() {
        final List<UserData> data = users.values().stream()
                .map(User::getUserData)
                .map(UserData::copy)
                .toList();
        try {
            userDataStorage.setUserDataBatch(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            users.clear();
            preloadedData.clear();
            savingQueue.clear();
        }
    }

    private record PreloadedData(
            UserData data,
            long expiresAt
    ) {
    }

}
