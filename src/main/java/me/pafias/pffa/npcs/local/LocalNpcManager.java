package me.pafias.pffa.npcs.local;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import me.pafias.pffa.npcs.NpcManager;
import me.pafias.pffa.npcs.local.packets.*;
import me.pafias.pffa.npcs.local.profile.GameProfile;
import me.pafias.pffa.npcs.local.profile.Property;
import me.pafias.pffa.npcs.local.profile.PropertyMap;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.gui.KitMenu;
import me.pafias.pffa.objects.gui.SpawnMenu;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.KitManager;
import me.pafias.pffa.services.SpawnManager;
import me.pafias.pffa.util.Serializer;
import me.pafias.putils.BukkitPlayerManager;
import me.pafias.putils.LCC;
import me.pafias.putils.builders.GameProfileBuilder;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LocalNpcManager implements NpcManager {

    private final pFFA plugin;
    private final KitManager kitManager;
    private final SpawnManager spawnManager;

    private LocalNpcListener listener;
    private VisibilityTask visibilityTask;
    private PacketHandler packetHandler;

    private final ExecutorService executor;

    public LocalNpcManager(pFFA plugin, KitManager kitManager, SpawnManager spawnManager) throws Exception {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.spawnManager = spawnManager;

        this.executor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("pFFA-LocalNpcManager-%d").build());

        if (plugin.parseVersion() >= 16)
            packetHandler = new PacketHandler1_16(); // 1.16
        else if (plugin.parseVersion() >= 15)
            packetHandler = new PacketHandler1_15(); // 1.15 - 1.16
        else if (plugin.parseVersion() >= 14)
            packetHandler = new PacketHandler1_14(); // 1.14 - 1.15
        else if (plugin.parseVersion() >= 10)
            packetHandler = new PacketHandler1_10(); // 1.10 - 1.14
        else if (plugin.parseVersion() >= 9)
            packetHandler = new PacketHandler1_9(); // 1.9 - 1.10
        else if (plugin.parseVersion() >= 8)
            packetHandler = new PacketHandler1_8(); // 1.8 - 1.9
        else if (plugin.parseVersion() >= 7)
            packetHandler = new PacketHandler1_7(); // 1.7 - 1.8
        else
            throw new IllegalStateException("Failed to load Local NPC Manager: The server version is not supported");

        file = new File(plugin.getDataFolder(), "npcs.yml");
        if (!file.exists()) file.createNewFile();
        config = YamlConfiguration.loadConfiguration(file);

        // Spawn stored npcs
        for (final String key : config.getKeys(false)) {
            try {
                final UUID uuid = UUID.fromString(key);
                final String name = config.getString(key + ".name");
                final PropertyMap properties = new PropertyMap();
                if (config.isConfigurationSection(key + ".properties")) {
                    for (String propertyKey : config.getConfigurationSection(key + ".properties").getKeys(false)) {
                        final String value = config.getString(key + ".properties." + propertyKey + ".value");
                        if (value == null) continue;
                        final String signature = config.getString(key + ".properties." + propertyKey + ".signature");
                        properties.put(propertyKey, new Property(propertyKey, value, signature));
                    }
                }
                final String nametag = LCC.t(config.getString(key + ".nametag"));
                final Location location = Serializer.parseConfigLocation(config, key + ".location");
                final String kitName = config.getString(key + ".kit");
                final Kit kit = kitName != null ? kitManager.getKit(kitName) : null;
                createNpc(uuid, name, properties, nametag, location, kit, false);
                plugin.getLogger().info("Spawned NPC " + uuid + " " + name + " at " + location);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load NPC with key: " + key + ". " + ex.getMessage());
            }
        }

        listener = new LocalNpcListener(plugin, this);
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        visibilityTask = new VisibilityTask(this);
        visibilityTask.runTaskTimerAsynchronously(plugin, 60, 3);
    }

    private final File file;
    private final FileConfiguration config;
    @Getter
    private final Map<UUID, FakeNpc> npcs = new ConcurrentHashMap<>();

    /**
     * Creates a new NPC with the proper name and skin.
     */
    @Override
    public void createNpc(String npcName, String npcSkinPlayerName, Location location, @Nullable Kit kit) {
        final OfflinePlayer skinPlayer = BukkitPlayerManager.getOfflinePlayerByName(npcSkinPlayerName);
        final GameProfile skinProfile = new GameProfile(new GameProfileBuilder()
                .setUuid(skinPlayer.getUniqueId())
                .setName(skinPlayer.getName())
                .setFetchProperties(true)
                .build());
        final GameProfile profile = new GameProfile(GameProfileBuilder.fromHandle(skinProfile.getHandle())
                .setUuid(UUID.nameUUIDFromBytes(npcName.getBytes()))
                .setName(npcName)
                .build());
        createNpc(profile, npcName, location, kit, true);
    }

    /**
     * Creates a new NPC with the given UUID and name, disregarding skin (lookups)
     */
    public void createNpc(UUID npcUuid, String npcName, PropertyMap properties, String nametag, Location location, @Nullable Kit kit, boolean save) {
        final GameProfileBuilder builder = new GameProfileBuilder();
        builder.setUuid(npcUuid);
        builder.setName(npcName);
        if (properties != null)
            for (Map.Entry<String, Collection<Property>> entry : properties.asMap().entrySet())
                for (Property property : entry.getValue())
                    builder.putProperty(property.getName(), property.getValue(), property.getSignature());
        final GameProfile profile = new GameProfile(builder.build());
        createNpc(profile, nametag, location, kit, save);
    }

    /**
     * Creates a new NPC with the given profile
     */
    public void createNpc(GameProfile profile, String nametag, Location location, @Nullable Kit kit, boolean save) {
        final FakeNpc npc = new FakeNpc(executor, packetHandler, packetHandler.getNextEntityId(), profile, nametag, location, kit);
        npcs.put(npc.getProfile().getId(), npc);

        if (save) {
            config.set(npc.getProfile().getId().toString() + ".name", npc.getProfile().getName());
            profile.getProperties().asMap().forEach((name, propertyList) -> {
                for (Property property : propertyList) {
                    config.set(npc.getProfile().getId().toString() + ".properties." + property.getName() + ".value", property.getValue());
                    if (property.hasSignature())
                        config.set(npc.getProfile().getId().toString() + ".properties." + property.getName() + ".signature", property.getSignature());
                }
            });
            config.set(npc.getProfile().getId().toString() + ".nametag", nametag);
            config.set(npc.getProfile().getId().toString() + ".location", Serializer.locationToConfig("location", npc.getLocation()));
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
                .collect(Collectors.toList());
        if (possibleNpcs.isEmpty()) throw new IllegalArgumentException("No NPC found near the specified location.");
        final FakeNpc npc = possibleNpcs.get(0);
        npc.destroyForAll();
        npcs.remove(npc.getProfile().getId());

        config.set(npc.getProfile().getId().toString(), null);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean trigger(@Nullable LivingEntity entity, String entityName, User user, boolean leftClick) {
        assert entity == null; // Entity is only used for Citizens NPCs
        if (entityName != null && kitManager.exists(entityName)) {
            // Clicked on Kit npc
            final Kit kit = kitManager.getKit(entityName);
            if (!leftClick) {
                new SpawnMenu(user, kit, spawnManager.getSpawns(user.getPlayer()).values())
                        .open();
            } else {
                kit.give(user.getPlayer());
                spawnManager.getDefaultSpawn().teleport(user.getPlayer());
                user.heal(false);
            }
            user.setLastKit(kit);
            return true;
        } else if (entityName != null && spawnManager.exists(entityName)) {
            // Clicked on Spawn npc
            final Spawn spawn = spawnManager.getSpawn(entityName);
            if (!leftClick) {
                new KitMenu(user, spawn, kitManager.getKits(user.getPlayer()).values())
                        .open();
            } else {
                kitManager.getDefaultKit().give(user.getPlayer());
                user.heal(false);
                spawn.teleport(user.getPlayer());
            }
            user.setLastSpawn(spawn);
            return true;
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
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        HandlerList.unregisterAll(listener);
        executor.shutdown();
        plugin.getLogger().info("Local NPC Manager shut down.");
    }

}
