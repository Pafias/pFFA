package me.pafias.pffa.util;

import me.pafias.pffa.pFFA;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reflection {

    private static final pFFA plugin = pFFA.get();

    private static Method actionbarMethod, spigotMethod, sendMessageMethod;

    public static void sendActionbar(final Player player, final String text) {
        try {
            if (actionbarMethod == null)
                actionbarMethod = player.getClass().getMethod("sendActionBar", String.class);
            actionbarMethod.invoke(player, text);
        } catch (Throwable ignored) {
            try {
                if (spigotMethod == null) {
                    spigotMethod = player.getClass().getMethod("spigot");
                    Object spigot = spigotMethod.invoke(player);
                    sendMessageMethod = spigot.getClass().getMethod(
                            "sendMessage",
                            Class.forName("net.md_5.bungee.api.ChatMessageType"),
                            Class.forName("[Lnet.md_5.bungee.api.chat.BaseComponent;")
                    );
                }
                Object spigot = spigotMethod.invoke(player);
                Object chatMessageType = Enum.valueOf(
                        (Class<Enum>) Class.forName("net.md_5.bungee.api.ChatMessageType"),
                        "ACTION_BAR"
                );
                Object[] baseComponents = (Object[]) Class
                        .forName("[Lnet.md_5.bungee.api.chat.BaseComponent;")
                        .cast(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(text));
                sendMessageMethod.invoke(spigot, chatMessageType, baseComponents);
            } catch (Throwable ignored2) {
                try {
                    final String version = getVersion();
                    final Class<?> cs = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
                    final Object chatComponent = cs.getDeclaredMethod("a", String.class).invoke(null, "{\"text\": \"" + text + "\"}");
                    final Class<? extends Player> cp = player.getClass();
                    final Object ep = cp.getDeclaredMethod("getHandle").invoke(player);
                    final Object nm = ep.getClass().getDeclaredField("playerConnection").get(ep);
                    final Method sendPacket = nm.getClass().getDeclaredMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
                    final Class<?> packetclass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
                    final Constructor<?> constructor = packetclass.getDeclaredConstructor(Class.forName("net.minecraft.server." + version + ".IChatBaseComponent"), byte.class);
                    final Object packet = constructor.newInstance(chatComponent, (byte) 2);
                    sendPacket.invoke(nm, packet);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    private static Method getHandleMethod;
    private static Field killerField;

    public static void setKiller(Player player, Player other) {
        try {
            if (getHandleMethod == null)
                getHandleMethod = player.getClass().getMethod("getHandle");

            Object entityPlayerPlayer = getHandleMethod.invoke(player);
            Object entityPlayerOther = getHandleMethod.invoke(other);

            if (killerField == null) {
                killerField = entityPlayerPlayer.getClass().getSuperclass().getSuperclass().getDeclaredField("killer");
                killerField.setAccessible(true);
            }

            killerField.set(entityPlayerPlayer, entityPlayerOther);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String version;

    public static String getVersion() {
        if (version == null)
            version = plugin.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit", "").replace(".", "");
        return version;
    }

    public static double getServerVersion() {
        try {
            Object server = plugin.getServer();
            Method getMinecraftVersion = server.getClass().getMethod("getMinecraftVersion");
            String version = (String) getMinecraftVersion.invoke(server); // 1.12.2
            String[] var = version.split("\\.", 2); // [1, 12.2]
            return Double.parseDouble(var[1]); // 12.2
        } catch (Throwable ignored) {
            try {
                String version = plugin.getServer().getBukkitVersion(); // 1.12.2-R0.1-SNAPSHOT
                String[] var = version.split("\\.", 2); // [1, 12.2-R0.1-SNAPSHOT]
                String[] var2 = var[1].split("-"); // [12.2, R0.1, SNAPSHOT]
                return Double.parseDouble(var2[0]); // 12.2
            } catch (Throwable ignored2) {
                try {
                    Pattern versionPattern = Pattern.compile(".*\\(.*MC.\\s*([A-Za-z0-9\\-\\.]+)\\s*\\)");
                    Matcher matcher = versionPattern.matcher(plugin.getServer().getVersion());
                    if (matcher.matches() && matcher.group(1) != null) {
                        String version = matcher.group(1); // e.g. "1.7.10"
                        String[] var = version.split("\\.", 2); // ["1", "7.10"]
                        return Double.parseDouble(var[1]); // 7.10
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        plugin.getLogger().severe("Failed to get server version.");
        return -1;
    }

}
