package me.pafias.pffa.npcs.local.packets;

import lombok.SneakyThrows;
import me.pafias.pffa.npcs.local.FakeNpc;
import me.pafias.pffa.npcs.local.wrapper.ItemSlot;
import me.pafias.pffa.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketHandler1_7 implements PacketHandler {

    @SneakyThrows
    public PacketHandler1_7() {
        try {
            gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        } catch (ClassNotFoundException ignored) {
            try {
                gameProfileClass = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
            } catch (ClassNotFoundException ignored2) {
            }
        }

        try {
            interactManagerClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PlayerInteractManager");
        } catch (ClassNotFoundException ignored) {
        }
        try {
            entityPlayerClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".EntityPlayer");
        } catch (ClassNotFoundException ignored) {
        }

        try {
            mcServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
        } catch (Exception ignored) {
        }
        try {
            mcServerClass = mcServer.getClass().getSuperclass();
        } catch (Exception ignored) {
        }

        try {
            World world = Bukkit.getWorlds().get(0);
            Object worldServer = world.getClass().getMethod("getHandle").invoke(world);
            worldServerClass = worldServer.getClass();
            worldClass = worldServerClass.getSuperclass();
        } catch (Throwable t) {
            try {
                worldServerClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".WorldServer");
                worldClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".World");
            } catch (ClassNotFoundException ignored) {
            }
        }

        try {
            interactManagerClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PlayerInteractManager");
        } catch (ClassNotFoundException ignored) {
        }

        try {
            enumGamemodeClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".EnumGamemode");
        } catch (ClassNotFoundException ignored) {
        }

        try {
            entityPlayerClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".EntityPlayer");
        } catch (ClassNotFoundException ignored) {
        }
        try {
            entityClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".Entity");
        } catch (ClassNotFoundException ignored) {
        }

        try {
            itemstackClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".ItemStack");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @SneakyThrows
    private void sendPacket(Player player, Object packet) {
        Class<?> playerClass = player.getClass();
        Method getHandle = playerClass.getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field playerConnectionField = nmsPlayer.getClass().getField("playerConnection");
        Object playerConnection = playerConnectionField.get(nmsPlayer);
        Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + Reflection.getVersion() + ".Packet"));
        sendPacketMethod.invoke(playerConnection, packet);
    }

    protected Object mcServer = null;
    protected Class enumGamemodeClass;
    protected Class<?> mcServerClass, entityClass, gameProfileClass, worldClass, worldServerClass, interactManagerClass, entityPlayerClass, itemstackClass;
    protected Field idField, locXField, locYField, locZField, yawField, pitchField;

    private final Map<Integer, Object> entityPlayers = new ConcurrentHashMap<>();

    @SneakyThrows
    private Object getOrCreateEntityPlayer(FakeNpc npc) {
        if (entityPlayers.containsKey(npc.getEntityId()))
            return entityPlayers.get(npc.getEntityId());

        Object worldServer = npc.getLocation().getWorld().getClass().getMethod("getHandle").invoke(npc.getLocation().getWorld());

        // Create PlayerInteractManager
        Constructor<?> imCtor = interactManagerClass.getConstructor(worldClass);
        Object interactManager = imCtor.newInstance(worldServer);

        Field gamemodeField = interactManagerClass.getDeclaredField("gamemode");
        gamemodeField.setAccessible(true);
        gamemodeField.set(interactManager, Enum.valueOf(enumGamemodeClass, "CREATIVE"));

        // Create EntityPlayer
        Constructor<?> epCtor = entityPlayerClass.getConstructor(mcServerClass, worldServerClass, gameProfileClass, interactManagerClass);
        Object entityPlayer = epCtor.newInstance(mcServer, worldServer, npc.getProfile().getHandle(), interactManager);

        if (idField == null) {
            idField = entityClass.getDeclaredField("id");
            idField.setAccessible(true);
        }
        idField.set(entityPlayer, npc.getEntityId());

        if (locXField == null) {
            locXField = entityClass.getField("locX");
            locXField.setAccessible(true);
        }
        locXField.set(entityPlayer, npc.getLocation().getX());

        if (locYField == null) {
            locYField = entityClass.getField("locY");
            locYField.setAccessible(true);
        }
        locYField.set(entityPlayer, npc.getLocation().getY());

        if (locZField == null) {
            locZField = entityClass.getField("locZ");
            locZField.setAccessible(true);
        }
        locZField.set(entityPlayer, npc.getLocation().getZ());

        if (yawField == null) {
            yawField = entityClass.getField("yaw");
            yawField.setAccessible(true);
        }
        yawField.set(entityPlayer, npc.getLocation().getYaw());

        if (pitchField == null) {
            pitchField = entityClass.getField("pitch");
            pitchField.setAccessible(true);
        }
        pitchField.set(entityPlayer, npc.getLocation().getPitch());

        entityPlayers.put(npc.getEntityId(), entityPlayer);
        return entityPlayer;
    }

    @Override
    @SneakyThrows
    public int getNextEntityId() {
        Field field = entityClass.getDeclaredField("entityCount");
        field.setAccessible(true);
        int id = (int) field.get(null);
        field.set(null, id + 1);
        return id;
    }

    @Override
    @SneakyThrows
    public void addToTab(Player player, FakeNpc npc) {
        final Object entityPlayer = getOrCreateEntityPlayer(npc);

        Class<?> infoPacketClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutPlayerInfo");
        Object infoPacket;
        try {
            Method addPlayer = infoPacketClass.getMethod("addPlayer", entityPlayerClass);
            infoPacket = addPlayer.invoke(null, entityPlayer);
        } catch (Throwable t) {
            Constructor<?> infoPacketConstructor = infoPacketClass.getConstructor(String.class, boolean.class, int.class);
            infoPacket = infoPacketConstructor.newInstance(npc.getProfile().getName().substring(0, 16), true, 0);
        }

        sendPacket(player, infoPacket);
    }

    @Override
    @SneakyThrows
    public void spawnNpc(Player player, FakeNpc npc) {
        final Object entityPlayer = getOrCreateEntityPlayer(npc);

        // Spawn packet
        Class<?> spawnPacket = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutNamedEntitySpawn");
        Constructor<?> spawnConstructor = spawnPacket.getConstructor(entityPlayerClass.getSuperclass());
        Object spawnPacketObject = spawnConstructor.newInstance(entityPlayer);

        sendPacket(player, spawnPacketObject);

        // Entity head rotation packet
        Class<?> rotationPacket = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutEntityHeadRotation");
        Constructor<?> rotationConstructor = rotationPacket.getConstructor(entityClass, byte.class);
        Object rotationPacketObject = rotationConstructor.newInstance(entityPlayer, (byte) (npc.getLocation().getYaw() * (256.0F / 360.0F)));

        sendPacket(player, rotationPacketObject);

        // in 1.7 there were no skin parts in the metadata so we don't send the packet here
    }

    @Override
    @SneakyThrows
    public void removeFromTab(Player player, FakeNpc npc) {
        final Object entityPlayer = getOrCreateEntityPlayer(npc);

        Class<?> infoPacketClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutPlayerInfo");
        Object infoPacket;
        try {
            Method removePlayer = infoPacketClass.getMethod("removePlayer", entityPlayerClass);
            infoPacket = removePlayer.invoke(null, entityPlayer);
        } catch (Throwable t) {
            Constructor<?> infoPacketConstructor = infoPacketClass.getConstructor(String.class, boolean.class, int.class);
            infoPacket = infoPacketConstructor.newInstance(npc.getProfile().getName().substring(0, 16), false, 0);
        }

        sendPacket(player, infoPacket);
    }

    @Override
    @SneakyThrows
    public void destroyNpc(Player player, FakeNpc npc) {
        getOrCreateEntityPlayer(npc);

        Class<?> destroyPacketClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutEntityDestroy");
        Constructor<?> constructor = destroyPacketClass.getConstructor(int[].class);
        Object destroyPacket = constructor.newInstance(new int[]{npc.getEntityId()});

        sendPacket(player, destroyPacket);
    }

    @Override
    @SneakyThrows
    public void rotate(Player player, FakeNpc npc, float yaw, float pitch) {
        final Object entityPlayer = getOrCreateEntityPlayer(npc);

        Class<?> rotationPacketClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutEntityHeadRotation");
        Constructor<?> rotationPacketConstructor = rotationPacketClass.getConstructor(entityClass, byte.class);
        Object rotationPacket = rotationPacketConstructor.newInstance(entityPlayer, (byte) (yaw * (256.0F / 360.0F)));

        sendPacket(player, rotationPacket);

        Class<?> lookPacketClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutEntityLook");
        Constructor<?> lookPacketConstructor = lookPacketClass.getConstructor(int.class, byte.class, byte.class, boolean.class);
        Object lookPacket = lookPacketConstructor.newInstance(npc.getEntityId(), (byte) (yaw * (256.0F / 360.0F)), (byte) (pitch * (256.0F / 360.0F)), true);

        sendPacket(player, lookPacket);
    }

    @Override
    @SneakyThrows
    public void changeEquipment(Player player, FakeNpc npc, Map<ItemSlot, ItemStack> equipment) {
        if (equipment == null || equipment.isEmpty()) return;

        getOrCreateEntityPlayer(npc);

        for (Map.Entry<ItemSlot, ItemStack> entry : equipment.entrySet()) {
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + Reflection.getVersion() + ".inventory.CraftItemStack");
            Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object itemstack = asNMSCopy.invoke(null, entry.getValue());

            Class<?> packetClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutEntityEquipment");
            Constructor<?> constructor = packetClass.getConstructor(int.class, int.class, itemstackClass);

            Object packet = constructor.newInstance(npc.getEntityId(), entry.getKey().toPacketSlot(), itemstack);
            sendPacket(player, packet);
        }
    }

    @Override
    @SneakyThrows
    public void teleport(Player player, FakeNpc npc, Location location) {
        final Object entityPlayer = getOrCreateEntityPlayer(npc);

        Class<?> teleportPacketClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutEntityTeleport");
        Constructor<?> teleportPacketClassConstructor = teleportPacketClass.getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class);
        Object teleportPacket = teleportPacketClassConstructor.newInstance(
                npc.getEntityId(),
                (int) Math.floor(location.getX() * 32),
                (int) Math.floor(location.getY() * 32),
                (int) Math.floor(location.getZ() * 32),
                (byte) (location.getYaw() * (256.0F / 360.0F)),
                (byte) (location.getPitch() * (256.0F / 360.0F)),
                false
        );

        sendPacket(player, teleportPacket);

        Class<?> rotationPacketClass = Class.forName("net.minecraft.server." + Reflection.getVersion() + ".PacketPlayOutEntityHeadRotation");
        Constructor<?> rotationPacketConstructor = rotationPacketClass.getConstructor(entityClass, byte.class);
        Object rotationPacket = rotationPacketConstructor.newInstance(entityPlayer, (byte) (location.getYaw() * (256.0F / 360.0F)));

        sendPacket(player, rotationPacket);
    }

}
