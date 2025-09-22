package me.pafias.pffa.npcs.local;

import lombok.Getter;
import me.pafias.pffa.npcs.local.packets.PacketHandler;
import me.pafias.pffa.npcs.local.profile.GameProfile;
import me.pafias.pffa.npcs.local.wrapper.ItemSlot;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.pFFA;
import me.pafias.putils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class FakeNpc {

    private final PacketHandler packetHandler;

    @Getter
    private final GameProfile profile;
    @Getter
    private final String nametag;
    @Getter
    private final Location location;
    @Getter
    private final Kit kit;

    @Getter
    private final int entityId;

    @Getter
    private final Set<Player> viewers = ConcurrentHashMap.newKeySet();

    private final ExecutorService executor;

    public FakeNpc(ExecutorService executor, PacketHandler packetHandler, int entityId, GameProfile profile, String nametag, Location location, Kit kit) {
        this.entityId = entityId;
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
                Map<ItemSlot, ItemStack> equipment = new ConcurrentHashMap<>();
                int i = 0;
                for (ItemSlot equipmentSlot : new ItemSlot[]{ItemSlot.HEAD, ItemSlot.CHEST, ItemSlot.LEGS, ItemSlot.FEET}) {
                    ItemStack item = kit.getArmor()[i];
                    equipment.put(equipmentSlot, item);
                    i++;
                }
                final ItemStack mainHand = kit.getItems().get(0);
                if (mainHand != null)
                    equipment.put(ItemSlot.MAINHAND, mainHand);

                final double version = pFFA.get().parseVersion();

                final ItemStack offHand = kit.getItems().get(40);
                if (offHand != null && version >= 9)
                    equipment.put(ItemSlot.OFFHAND, offHand);

                if (version >= 16)
                    // On 1.16 and above we can send all equipment at once
                    packetHandler.changeEquipment(player, this, equipment);
                else {
                    // Before that we have to send each piece of equipment separately
                    for (Map.Entry<ItemSlot, ItemStack> entry : equipment.entrySet()) {
                        Map<ItemSlot, ItemStack> singleEquipment = new HashMap<>();
                        singleEquipment.put(entry.getKey(), entry.getValue());
                        packetHandler.changeEquipment(player, this, singleEquipment);
                    }
                }
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