package me.pafias.pafiasffa.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;

public class CC {

    public static String t(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String tf(String s, Object... o){
        return t(String.format(s, o));
    }

    public static ArrayList<String> t(ArrayList<String> list) {
        ArrayList<String> l = new ArrayList<>();
        list.forEach(s -> l.add(t(s)));
        return l;
    }

}
