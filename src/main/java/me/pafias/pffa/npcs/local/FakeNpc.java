package me.pafias.pffa.npcs.local;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import me.pafias.pffa.npcs.local.packets.PacketHandler;
import me.pafias.pffa.objects.Kit;
import me.pafias.putils.Tasks;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class FakeNpc {

    private final PacketHandler packetHandler;

    @Getter
    private final PlayerProfile profile;
    @Getter
    private final Component nametag;
    @Getter
    private final Location location;
    @Getter
    private final Kit kit;

    @Getter
    private final int entityId = new Random().nextInt(100000) + 2000;

    @Getter
    private final Set<Player> viewers = ConcurrentHashMap.newKeySet();

    private final ExecutorService executor;

    public FakeNpc(ExecutorService executor, PacketHandler packetHandler, PlayerProfile profile, Component nametag, Location location, Kit kit) {
        this.executor = executor;
        this.packetHandler = packetHandler;
        this.profile = profile;
        this.nametag = nametag;
        this.location = location;
        this.kit = kit;
    }

    public CompletableFuture<Void> spawn(Player player) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        queueVisibilityTask(() -> {
            if (viewers.contains(player)) {
                future.complete(null);
                return;
            }
            viewers.add(player);

            packetHandler.addToTab(player, this);
            packetHandler.spawnNpc(player, this);
            if (kit != null) {
                Equipment[] equipment = new Equipment[4];
                int i = 0;
                for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HELMET, EquipmentSlot.CHEST_PLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS}) {
                    ItemStack item = kit.getArmor()[i];
                    equipment[i] = new Equipment(
                            equipmentSlot,
                            SpigotConversionUtil.fromBukkitItemStack(item)
                    );
                    i++;
                }
                packetHandler.changeEquipment(player, this, equipment);
            }
            Tasks.runLaterSync(60, () -> {
                packetHandler.removeFromTab(player, this);
            });

            future.complete(null);
        });
        return future;
    }

    public void destroyForAll() {
        Bukkit.getOnlinePlayers().forEach(this::destroy);
    }

    public void destroy(Player player) {
        queueVisibilityTask(() -> {
            if (!viewers.contains(player)) return;
            viewers.remove(player);
            packetHandler.destroyNpc(player, this);
        });
    }

    // Queue

    private boolean queueRunning = false;
    private final Queue<Runnable> visibilityTaskQueue = new ConcurrentLinkedQueue<>();

    private void tryRunQueue() {
        if (visibilityTaskQueue.isEmpty() || queueRunning) return;
        queueRunning = true;
        CompletableFuture.runAsync(() -> {
                    while (!visibilityTaskQueue.isEmpty()) try {
                        visibilityTaskQueue.remove().run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    queueRunning = false;
                }, executor)
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void queueVisibilityTask(Runnable runnable) {
        visibilityTaskQueue.add(runnable);
        tryRunQueue();
    }

}