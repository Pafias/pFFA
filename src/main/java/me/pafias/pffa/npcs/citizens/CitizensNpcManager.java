package me.pafias.pffa.npcs.citizens;

import me.pafias.pffa.events.FFAPlayerSpawnedEvent;
import me.pafias.pffa.events.FFAPlayerSpawnEvent;
import me.pafias.pffa.npcs.NpcManager;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.GuiManager;
import me.pafias.pffa.services.KitManager;
import me.pafias.pffa.services.SpawnManager;
import me.pafias.putils.CC;
import me.pafias.putils.Tasks;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.SimpleNPCDataStore;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CitizensNpcManager implements NpcManager {

    private final KitManager kitManager;
    private final SpawnManager spawnManager;
    private final GuiManager guiManager;

    public CitizensNpcManager(pFFA plugin, KitManager kitManager, SpawnManager spawnManager, GuiManager guiManager) {
        this.kitManager = kitManager;
        this.spawnManager = spawnManager;
        this.guiManager = guiManager;

        final Citizens citizens = (Citizens) CitizensAPI.getPlugin();
        npcDataStore = SimpleNPCDataStore.create(new YamlStorage(new File(plugin.getDataFolder(), "npcs-citizens.yml")));
        npcDataStore.reloadFromSource();
        npcRegistry = citizens.getNamedNPCRegistry("pffa");
        if (npcRegistry == null)
            npcRegistry = citizens.createNamedNPCRegistry("pffa", npcDataStore);
        npcDataStore.loadInto(npcRegistry);
        Tasks.runLaterSync(40, () -> {
            npcRegistry.forEach(npc -> {
                if (!npc.isSpawned() && npc.getStoredLocation() != null && npc.getStoredLocation().getWorld() != null) {
                    npc.spawn(npc.getStoredLocation());
                    plugin.getLogger().info("Spawned NPC " + npc.getName() + " at " + npc.getStoredLocation());
                }
            });
        });

        plugin.getServer().getPluginManager().registerEvents(new CitizensNpcListener(plugin, this), plugin);
    }

    private NPCRegistry npcRegistry;
    private NPCDataStore npcDataStore;

    @Override
    public void createNpc(Component npcName, String npcSkinPlayerName, Location location, @Nullable Kit kit) {
        final String name = PlainTextComponentSerializer.plainText().serializeOrNull(npcName);
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("NPC name cannot be null or empty.");
        if (location == null || location.getWorld() == null)
            throw new IllegalArgumentException("Location cannot be null or in an invalid world.");
        final NPC npc = npcRegistry.createNPC(EntityType.PLAYER, name);
        npc.spawn(location, SpawnReason.CREATE);
        final CurrentLocation currentLocation = npc.getOrAddTrait(CurrentLocation.class);
        currentLocation.setLocation(location);
        if (kit != null) {
            Equipment equipment = npc.getOrAddTrait(Equipment.class);
            equipment.set(Equipment.EquipmentSlot.HELMET, kit.getArmor()[0]);
            equipment.set(Equipment.EquipmentSlot.CHESTPLATE, kit.getArmor()[1]);
            equipment.set(Equipment.EquipmentSlot.LEGGINGS, kit.getArmor()[2]);
            equipment.set(Equipment.EquipmentSlot.BOOTS, kit.getArmor()[3]);
        }
        final SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinName(npcSkinPlayerName);
        npcRegistry.saveToStore();
    }

    public void createNpc(String npcName, Location location, @Nullable Kit kit) {
        createNpc(CC.a(npcName), npcName, location, kit);
    }

    public void removeNpc(Location location) {
        if (location == null || location.getWorld() == null)
            throw new IllegalArgumentException("Location cannot be null or in an invalid world.");
        final Entity entity = location.getWorld().getNearbyEntities(location, 2, 2, 2).stream()
                .filter(e -> e.hasMetadata("NPC"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No NPC found at the specified location."));
        final NPC npc = npcRegistry.getNPC(entity);
        if (npc == null)
            throw new IllegalArgumentException("Entity is not a registered NPC.");
        npc.destroy();
        npcRegistry.saveToStore();
    }

    @Override
    public boolean trigger(@Nullable Entity entity, String entityName, User user, boolean leftClick) {
        assert entity != null;
        if (!entity.hasMetadata("NPC")) return false;
        final NPC npc = npcRegistry.getNPC(entity);
        if (npc == null) return false;
        if (npc.getName() != null || (entity.isCustomNameVisible() && entity.getCustomName() != null)) {
            Kit kit = kitManager.getKit(npc.getName());
            if (kit != null) { // Clicked on Kit npc
                if (!leftClick) {
                    guiManager.openSpawnGui(user, kit);
                    user.setLastKit(kit);
                } else {
                    Spawn spawn = spawnManager.getDefaultSpawn();

                    final FFAPlayerSpawnEvent event = new FFAPlayerSpawnEvent(user.getPlayer(), spawn, kit);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return false;
                    spawn = event.getSpawn();
                    kit = event.getKit();
                    if (spawn != null) {
                        event.getSpawn().teleport(user.getPlayer());
                        user.setLastSpawn(spawn);
                    }
                    if (kit != null) {
                        kit.give(user.getPlayer());
                        user.setLastKit(kit);
                    }
                    user.heal(false);
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                    Bukkit.getPluginManager().callEvent(new FFAPlayerSpawnedEvent(user.getPlayer(), spawn, kit));
                }
                return true;
            }
            Spawn spawn = spawnManager.getSpawn(npc.getName());
            if (spawn != null) { // Clicked on Spawn npc
                if (!leftClick) {
                    guiManager.openKitGui(user, spawn);
                    user.setLastSpawn(spawn);
                } else {
                    Kit k = kitManager.getDefaultKit();

                    final FFAPlayerSpawnEvent event = new FFAPlayerSpawnEvent(user.getPlayer(), spawn, k);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return false;
                    spawn = event.getSpawn();
                    k = event.getKit();
                    if (spawn != null) {
                        event.getSpawn().teleport(user.getPlayer());
                        user.setLastSpawn(spawn);
                    }
                    if (k != null) {
                        k.give(user.getPlayer());
                        user.setLastKit(k);
                    }
                    user.heal(false);
                    Tasks.runLaterSync(1, () -> user.getPlayer().closeInventory());
                    Bukkit.getPluginManager().callEvent(new FFAPlayerSpawnedEvent(user.getPlayer(), spawn, k));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> boolean exists(T npc) {
        if (npc instanceof NPC var)
            return var.getOwningRegistry().equals(npcRegistry);
        else if (npc instanceof Entity entity) {
            final NPC npcEntity = npcRegistry.getNPC(entity);
            return npcEntity != null && npcEntity.getOwningRegistry().equals(npcRegistry);
        }
        return false;
    }

    @Override
    public void shutdown() {
        npcRegistry.despawnNPCs(DespawnReason.RELOAD);
    }

}
