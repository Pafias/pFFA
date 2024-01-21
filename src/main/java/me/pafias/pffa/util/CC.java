package me.pafias.pffa.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CC {

    public static String t(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String tf(String s, Object... o) {
        return t(String.format(s, o));
    }

    public static List<String> t(ArrayList<String> list) {
        return list.stream().map(CC::t).collect(Collectors.toList());
    }

}
