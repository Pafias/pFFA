package me.pafias.pffa.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.pafias.pffa.pFFA;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {

    private static final pFFA plugin = pFFA.get();

    public static int getPing(Player player) {
        if (plugin.serverVersion() >= 17) {
            try {
                return (int) player.getClass().getDeclaredMethod("getPing").invoke(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return -1;
            }
        } else {
            try {
                Object o = player.getClass().getDeclaredMethod("getHandle").invoke(player);
                Field f = o.getClass().getDeclaredField("ping");
                return f.getInt(o);
            } catch (Exception ex) {
                ex.printStackTrace();
                return -1;
            }
        }
    }

    public static void sendActionbar(Player player, String text) {
        if (plugin.serverVersion() > 9)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
        else {
            try {
                String version = getVersion();
                Class cs = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
                Object chatComponent = cs.getDeclaredMethod("a", String.class).invoke(null, "{\"text\": \"" + text + "\"}");
                Class cp = player.getClass();
                Object ep = cp.getDeclaredMethod("getHandle").invoke(player);
                Object nm = ep.getClass().getDeclaredField("playerConnection").get(ep);
                Method sendPacket = nm.getClass().getDeclaredMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
                Class packetclass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
                Constructor constructor = packetclass.getDeclaredConstructor(Class.forName("net.minecraft.server." + version + ".IChatBaseComponent"), byte.class);
                Object packet = constructor.newInstance(chatComponent, (byte) 2);
                sendPacket.invoke(nm, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static ItemStack getSkull() {
        if (plugin.serverVersion() < 13) {
            try {
                Class clazz = Class.forName("org.bukkit.inventory.ItemStack");
                Constructor constructor = clazz.getConstructor(int.class, int.class, short.class);
                Object itemstack = constructor.newInstance(397, 1, (short) SkullType.PLAYER.ordinal());
                return (ItemStack) itemstack;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            return new ItemStack(Material.getMaterial("PLAYER_HEAD"));
        }
    }

    public static void playSound(Player player, Sound sound, double x, double y, double z, float volume, float pitch) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
        packet.getModifier().write(0, sound.ordinal());
        packet.getModifier().write(1, 0);
        packet.getIntegers().write(2, (int) x);
        packet.getIntegers().write(3, (int) y);
        packet.getIntegers().write(4, (int) z);
        packet.getFloat().write(0, volume);
        packet.getFloat().write(1, pitch);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    private static String getVersion() {
        return plugin.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit", "").replace(".", "");
    }

}
