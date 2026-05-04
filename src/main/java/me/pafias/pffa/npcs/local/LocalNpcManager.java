package me.pafias.pffa.npcs.local;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import me.pafias.pffa.npcs.NpcManager;
import me.pafias.pffa.npcs.local.packets.*;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.GuiManager;
import me.pafias.pffa.services.KitManager;
import me.pafias.pffa.services.SpawnManager;
import me.pafias.putils.BukkitPlayerManager;
import me.pafias.putils.CC;
import me.pafias.putils.Tasks;
import me.pafias.putils.builders.PlayerProfileBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalNpcManager implements NpcManager {

    private final pFFA plugin;
    private final KitManager kitManager;
    private final SpawnManager spawnManager;
    private final GuiManager guiManager;

    private LocalNpcListener listener;
    private VisibilityTask visibilityTask;
    private PacketHandler packetHandler;

    private final ExecutorService executor;

    public LocalNpcManager(pFFA plugin, KitManager kitManager, SpawnManager spawnManager, GuiManager guiManager) throws Exception {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.spawnManager = spawnManager;
        this.guiManager = guiManager;

        this.executor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("pFFA-LocalNpcManager-%d").build());

        final ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
        if (version.isOlderThan(ServerVersion.V_1_17))
            throw new IllegalStateException("Failed to load Local NPC Manager: The server version is older than 1.17");
        else if (version.isOlderThan(ServerVersion.V_1_19_3))
            packetHandler = new PacketHandler1_17(); // 1.17 - 1.19.3
        else if (version.isOlderThan(ServerVersion.V_1_20_2))
            packetHandler = new PacketHandler1_19_3(); // 1.19.3 - 1.20.2
        else if (version.isOlderThan(ServerVersion.V_1_21_3))
            packetHandler = new PacketHandler1_20_2(); // 1.20.2 - 1.21.3
        else if (version.isOlderThan(ServerVersion.V_1_21_9))
            packetHandler = new PacketHandler1_21_3(); // 1.21.3 - 1.21.9
        else
            packetHandler = new PacketHandler1_21_9(); // 1.21.9 - latest

        file = new File(plugin.getDataFolder(), "npcs.yml");
        if (!file.exists()) file.createNewFile();
        config = YamlConfiguration.loadConfiguration(file);

        // Spawn stored npcs
        for (final String key : config.getKeys(false)) {
            try {
                final UUID uuid = UUID.fromString(key);
                final String name = config.getString(key + ".name");
                final Set<ProfileProperty> properties = new HashSet<>();
                if (config.isConfigurationSection(key + ".properties")) {
                    for (String propertyKey : config.getConfigurationSection(key + ".properties").getKeys(false)) {
                        final String value = config.getString(key + ".properties." + propertyKey + ".value");
                        if (value == null) continue;
                        final String signature = config.getString(key + ".properties." + propertyKey + ".signature");
                        properties.add(new ProfileProperty(propertyKey, value, signature));
                    }
                }
                final Component nametag = CC.a(config.getString(key + ".nametag"));
                final Location location = config.getLocation(key + ".location");
                final String kitName = config.getString(key + ".kit");
                final Kit kit = kitName != null ? kitManager.getKit(kitName) : null;
                createNpc(uuid, name, properties, nametag, location, kit, false);
                plugin.getLogger().info("Spawned NPC " + uuid + " " + name + " at " + location);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load NPC with key: " + key + ". " + ex.getMessage());
            }
        }

        listener = new LocalNpcListener(plugin, this);
        PacketEvents.getAPI().getEventManager().registerListener(listener);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        visibilityTask = new VisibilityTask(this);
        visibilityTask.runTaskTimerAsynchronously(plugin, 60, 3);
    }

    private final File file;
    private final FileConfiguration config;
    @Getter
    private final Int2ObjectMap<FakeNpc> npcs = new Int2ObjectOpenHashMap<>();

    /**
     * Creates a new NPC with the proper name and skin.
     */
    @Override
    public void createNpc(Component npcName, String npcSkinPlayerName, Location location, @Nullable Kit kit) {
        final OfflinePlayer skinPlayer = BukkitPlayerManager.getOfflinePlayerByName(npcSkinPlayerName, false);
        final PlayerProfile skinProfile = new PlayerProfileBuilder()
                .setUuid(skinPlayer.getUniqueId())
                .setName(skinPlayer.getName())
                .setFetchProperties(true)
                .build();
        final String cleanName = PlainTextComponentSerializer.plainText().serializeOr(npcName, "");
        final PlayerProfile profile = new PlayerProfileBuilder()
                .setName(cleanName)
                .setUuid(UUID.nameUUIDFromBytes(cleanName.getBytes()))
                .setProperties(skinProfile.getProperties())
                .build();
        createNpc(profile, npcName, location, kit, true);
    }

    /**
     * Creates a new NPC with the given UUID and name, disregarding skin (lookups)
     */
    public void createNpc(UUID npcUuid, String npcName, Set<ProfileProperty> properties, Component nametag, Location location, @Nullable Kit kit, boolean save) {
        final PlayerProfile profile = new PlayerProfileBuilder()
                .setUuid(npcUuid)
                .setName(npcName)
                .setFetchProperties(false)
                .build();
        if (properties != null)
            profile.setProperties(properties);
        createNpc(profile, nametag, location, kit, save);
    }

    /**
     * Creates a new NPC with the given profile
     */
    public void createNpc(PlayerProfile profile, Component nametag, Location location, @Nullable Kit kit, boolean save) {
        final FakeNpc npc = new FakeNpc(executor, packetHandler, profile, nametag, location, kit);
        npcs.put(npc.getEntityId(), npc);

        if (save) {
            config.set(npc.getProfile().getId().toString() + ".name", npc.getProfile().getName());
            profile.getProperties().forEach(property -> {
                config.set(npc.getProfile().getId().toString() + ".properties." + property.getName() + ".value", property.getValue());
                if (property.isSigned())
                    config.set(npc.getProfile().getId().toString() + ".properties." + property.getName() + ".signature", property.getSignature());
            });
            config.set(npc.getProfile().getId().toString() + ".nametag", CC.serialize(nametag));
            config.set(npc.getProfile().getId().toString() + ".location", npc.getLocation());
            if (kit != null)
                config.set(npc.getProfile().getId().toString() + ".kit", kit.getName());
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeNpc(Location location) {
        if (location == null || location.getWorld() == null)
            throw new IllegalArgumentException("Location cannot be null and must have a valid world.");
        final List<FakeNpc> possibleNpcs = npcs.values().stream()
                .filter(npc -> npc.getLocation().getWorld().equals(location.getWorld()) &&
                        npc.getLocation().distanceSquared(location) < 3.0)
                .sorted(Comparator.comparingDouble(npc -> npc.getLocation().distanceSquared(location)))
                .toList();
        if (possibleNpcs.isEmpty()) throw new IllegalArgumentException("No NPC found near the specified location.");
        final FakeNpc npc = possibleNpcs.get(0);
        npc.destroyForAll();
        npcs.remove(npc.getEntityId());

        config.set(npc.getProfile().getId().toString(), null);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean trigger(@Nullable Entity entity, String entityName, User user, boolean leftClick) {
        assert entity == null; // Entity is only used for Citizens NPCs
        if (entityName != null) {
            final Kit kit = kitManager.getKit(entityName);
            if (kit != null) { // Clicked on Kit npc
                if (!leftClick) {
                    guiManager.openSpawnGui(user, kit);
                } else {
                    final Spawn defaultSpawn = spawnManager.getDefaultSpawn();
                    kit.give(user.getPlayer());
                    defaultSpawn.teleport(user.getPlayer());
                    user.heal(false);
                    user.setLastSpawn(defaultSpawn);
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                }
                user.setLastKit(kit);
                return true;
            }
            final Spawn spawn = spawnManager.getSpawn(entityName);
            if (spawn != null) { // Clicked on Spawn npc
                if (!leftClick) {
                    guiManager.openKitGui(user, spawn);
                } else {
                    final Kit defaultKit = kitManager.getDefaultKit();
                    defaultKit.give(user.getPlayer());
                    spawn.teleport(user.getPlayer());
                    user.heal(false);
                    user.setLastKit(defaultKit);
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                }
                user.setLastSpawn(spawn);
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> boolean exists(T npc) {
        return false;
    }

    @Override
    public void shutdown() {
        plugin.getLogger().info("Shutting down Local NPC Manager...");
        visibilityTask.cancel();
        for (final FakeNpc npc : npcs.values())
            npc.destroyForAll();
        npcs.clear();
        PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        HandlerList.unregisterAll(listener);
        executor.shutdown();
        plugin.getLogger().info("Local NPC Manager shut down.");
    }

}
