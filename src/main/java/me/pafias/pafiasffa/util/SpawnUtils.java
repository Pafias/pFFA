package me.pafias.pafiasffa.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpawnUtils {

    private static final PafiasFFA plugin = PafiasFFA.get();

    public static ItemStack jsonToGuiItem(JsonObject json) {
        ItemStack is = new ItemStack(getMaterial(json.get("material").getAsString()));
        ItemMeta meta = is.getItemMeta();
        if (json.has("displayname"))
            meta.setDisplayName(CC.t(json.get("displayname").getAsString()));
        List<String> lore = new ArrayList<>();
        if (json.has("lore")) {
            for (int i = 0; i < json.get("lore").getAsJsonArray().size(); i++)
                lore.add(json.get("lore").getAsJsonArray().get(i).getAsString());
            meta.setLore(lore);
        }
        is.setItemMeta(meta);
        return is;
    }

    public static JsonObject guiItemToJson(ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("material", item.getType().name());
        json.addProperty("displayname", item.getItemMeta().getDisplayName());
        json.add("lore", new JsonParser().parse(new GsonBuilder().create().toJson(Optional.ofNullable(item.getItemMeta().getLore()).orElse(new ArrayList<>()))));
        return json;
    }

    public static Location jsonToLocation(JsonObject json) {
        World world = plugin.getServer().getWorld(json.get("world").getAsString());
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float yaw = json.get("yaw").getAsFloat();
        float pitch = json.get("pitch").getAsFloat();
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static JsonObject locationToJson(Location location) {
        JsonObject json = new JsonObject();
        json.addProperty("world", location.getWorld().getName());
        json.addProperty("x", location.getX());
        json.addProperty("y", location.getY());
        json.addProperty("z", location.getZ());
        json.addProperty("yaw", location.getYaw());
        json.addProperty("pitch", location.getPitch());
        return json;
    }

    private static Material getMaterial(String name) {
        Material material = Material.getMaterial(name);
        if (plugin.parseVersion() >= 1.13 && material == null)
            return Material.getMaterial("LEGACY_" + name);
        else return material;
    }

}
