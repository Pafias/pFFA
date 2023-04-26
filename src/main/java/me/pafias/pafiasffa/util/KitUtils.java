package me.pafias.pafiasffa.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KitUtils {

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
        if (item.getItemMeta().hasDisplayName())
            json.addProperty("displayname", item.getItemMeta().getDisplayName());
        if (item.getItemMeta().hasLore())
            json.add("lore", new JsonParser().parse(new GsonBuilder().create().toJson(Optional.ofNullable(item.getItemMeta().getLore()).orElse(new ArrayList<>()))));
        return json;
    }

    public static ItemStack jsonToInvItem(JsonObject json) {
        ItemStack is = new ItemStack(getMaterial(json.get("material").getAsString()), json.get("amount").getAsInt());
        if (json.has("enchantments")) {
            ItemMeta meta = is.getItemMeta();
            List<JsonObject> enchantments = new ArrayList<>();
            for (int i = 0; i < json.get("enchantments").getAsJsonArray().size(); i++)
                enchantments.add(json.get("enchantments").getAsJsonArray().get(i).getAsJsonObject());
            for (JsonObject enchantment : enchantments)
                meta.addEnchant(Enchantment.getByName(enchantment.get("name").getAsString()), enchantment.get("level").getAsInt(), true);
            is.setItemMeta(meta);
        }
        return is;
    }

    public static JsonObject invItemToJson(int slot, ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("slot", slot);
        json.addProperty("material", item.getType().name());
        json.addProperty("amount", item.getAmount());
        JsonArray array = new JsonArray();
        item.getEnchantments().keySet().forEach(enchantment -> {
            JsonObject json2 = new JsonObject();
            int level = item.getEnchantments().get(enchantment);
            json2.addProperty("name", enchantment.getName());
            json2.addProperty("level", level);
            array.add(json2);
        });
        json.add("enchantments", array);
        return json;
    }

    private static Material getMaterial(String name) {
        Material material = Material.getMaterial(name);
        if (plugin.parseVersion() >= 1.13 && material == null)
            return Material.getMaterial("LEGACY_" + name);
        else return material;
    }

}
