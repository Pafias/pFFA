package me.pafias.pffa.npcs.citizens;

import me.pafias.pffa.npcs.NpcManager;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.objects.User;
import me.pafias.pffa.objects.gui.KitMenu;
import me.pafias.pffa.objects.gui.SpawnMenu;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.services.KitManager;
import me.pafias.pffa.services.SpawnManager;
import me.pafias.putils.LCC;
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
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CitizensNpcManager implements NpcManager {

    private final KitManager kitManager;
    private final SpawnManager spawnManager;

    public CitizensNpcManager(pFFA plugin, KitManager kitManager, SpawnManager spawnManager) {
        this.kitManager = kitManager;
        this.spawnManager = spawnManager;

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
    public void createNpc(String npcName, String npcSkinPlayerName, Location location, @Nullable Kit kit) {
        if (npcName == null || npcName.isEmpty())
            throw new IllegalArgumentException("NPC name cannot be null or empty.");
        if (location == null || location.getWorld() == null)
            throw new IllegalArgumentException("Location cannot be null or in an invalid world.");
        final NPC npc = npcRegistry.createNPC(EntityType.PLAYER, npcName);
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
        createNpc(LCC.t(npcName), npcName, location, kit);
    }

    public void removeNpc(Location location) {
        if (location == null || location.getWorld() == null)
            throw new IllegalArgumentException("Location cannot be null or in an invalid world.");
        final Entity entity = location.getWorld().getEntities().stream()
                .filter(e -> e.hasMetadata("NPC") && e.getLocation().distanceSquared(location) <= Math.exp(4))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No NPC found at the specified location."));
        final NPC npc = npcRegistry.getNPC(entity);
        if (npc == null)
            throw new IllegalArgumentException("Entity is not a registered NPC.");
        npc.destroy();
        npcRegistry.saveToStore();
    }

    @Override
    public boolean trigger(@Nullable LivingEntity entity, String entityName, User user, boolean leftClick) {
        assert entity != null;
        if (!entity.hasMetadata("NPC")) return false;
        final NPC npc = npcRegistry.getNPC(entity);
        if (npc == null) return false;
        if (entity.isCustomNameVisible() && entity.getCustomName() != null && kitManager.exists(entity.getCustomName())) {
            // Clicked on Kit npc
            final Kit kit = kitManager.getKit(npc.getName());
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
        } else if (entity.isCustomNameVisible() && entity.getCustomName() != null && spawnManager.exists(entity.getCustomName())) {
            // Clicked on Spawn npc
            final Spawn spawn = spawnManager.getSpawn(entity.getCustomName());
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
        if (npc instanceof NPC)
            return ((NPC) npc).getOwningRegistry().equals(npcRegistry);
        else if (npc instanceof Entity) {
            final NPC npcEntity = npcRegistry.getNPC((Entity) npc);
            return npcEntity != null && npcEntity.getOwningRegistry().equals(npcRegistry);
        }
        return false;
    }

    @Override
    public void shutdown() {
        npcRegistry.despawnNPCs(DespawnReason.RELOAD);
    }

}
