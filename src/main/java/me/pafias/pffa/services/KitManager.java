package me.pafias.pffa.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.Serializer;
import me.pafias.putils.LCC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class KitManager {

    private final pFFA plugin;

    public KitManager(pFFA plugin) {
        this.plugin = plugin;
        loadKits();
    }

    @Getter
    private final LinkedHashMap<String, Kit> kits = new LinkedHashMap<>();

    /**
     * Get all kits that the player has permission to.
     *
     * @param player The player to check permissions for. If null, all kits are returned.
     * @return A map of kit names to Kit objects, filtered by permission.
     */
    public Map<String, Kit> getKits(@Nullable Player player) {
        if (player == null)
            return kits;

        Map<String, Kit> kitsMap = new HashMap<>(kits.size());
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            final Kit kit = entry.getValue();
            if (!kit.hasPermission() || player.hasPermission(kit.getPermission()))
                kitsMap.put(entry.getKey(), kit);
        }
        return kitsMap;
    }

    public boolean exists(String name) {
        if (kits.containsKey(name.toLowerCase()))
            return true;
        for (String kitName : kits.keySet()) {
            if (name.toLowerCase().contains(kitName.toLowerCase()))
                return true;
        }
        return false;
    }

    public Kit getDefaultKit() {
        return kits.isEmpty() ? null : kits.values().iterator().next();
    }

    public Kit getKit(String name) {
        Kit kit = kits.get(name.toLowerCase());
        if (kit == null)
            for (Map.Entry<String, Kit> entry : kits.entrySet()) {
                if (name.toLowerCase().contains(entry.getKey().toLowerCase()))
                    kit = entry.getValue();
            }
        return kit;
    }

    public void saveNewKit(Player player, String name, String permission) throws IOException {
        File file = new File(plugin.getDataFolder() + "/kits", name + ".json");
        if (!file.exists())
            file.createNewFile();
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        if (permission != null && !permission.isEmpty())
            json.addProperty("permission", permission);
        json.add("gui_item", Serializer.guiItemToJson(player.getInventory().getItemInHand()));
        JsonArray items = new JsonArray();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().equals(Material.AIR)) continue;
            items.add(Serializer.invItemToJson(i, item));
        }
        json.add("items", items);
        if (!player.getActivePotionEffects().isEmpty()) {
            JsonArray effects = new JsonArray();
            for (PotionEffect pe : player.getActivePotionEffects()) {
                JsonObject object = new JsonObject();
                object.addProperty("type", pe.getType().getName());
                object.addProperty("duration", pe.getDuration());
                object.addProperty("amplifier", pe.getAmplifier());
                effects.add(object);
            }
            json.add("effects", effects);
        }
        FileWriter writer = new FileWriter(file);
        writer.write(json.toString());
        writer.close();
        loadKit(file);
    }

    public void loadKit(File file) {
        try {
            String jsonText = readAll(new FileReader(file));
            JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
            String name = json.get("name").getAsString();
            String permission = json.has("permission") ? json.get("permission").getAsString() : null;
            ItemStack gui_item = Serializer.jsonToGuiItem(json.get("gui_item").getAsJsonObject());
            Map<Integer, ItemStack> items = new HashMap<>();
            for (int i = 0; i < json.get("items").getAsJsonArray().size(); i++) {
                JsonObject item = json.get("items").getAsJsonArray().get(i).getAsJsonObject();
                items.put(item.get("slot").getAsInt(), Serializer.jsonToInvItem(item));
            }
            Collection<PotionEffect> potionEffects = new ArrayList<>();
            if (json.has("effects")) {
                for (JsonElement e : json.get("effects").getAsJsonArray()) {
                    JsonObject pe = e.getAsJsonObject();
                    PotionEffectType type = PotionEffectType.getByName(pe.get("type").getAsString().trim());
                    int duration = pe.get("duration").getAsInt();
                    int amplifier = pe.get("amplifier").getAsInt();
                    PotionEffect potionEffect;
                    if (plugin.parseVersion() < 8)
                        potionEffect = new PotionEffect(type, duration, amplifier, false);
                    else
                        potionEffect = new PotionEffect(type, duration, amplifier, false, false);
                    potionEffects.add(potionEffect);
                }
            }
            kits.put(name.toLowerCase(), new Kit(name, permission, gui_item, items, potionEffects));
        } catch (IOException ex) {
            ex.printStackTrace();
            plugin.getServer().getLogger().log(Level.WARNING, "");
            plugin.getServer().getLogger().log(Level.WARNING, LCC.t("&cUnable to parse kit json. Read stacktrace above."));
            plugin.getServer().getLogger().log(Level.WARNING, "");
        }
    }

    public void loadKits() {
        kits.clear();
        File dir = new File(plugin.getDataFolder() + "/kits");
        if (!dir.exists())
            dir.mkdirs();
        File[] files = dir.listFiles();
        if (files.length == 0) return;
        Arrays.sort(files);
        for (File file : files) {
            if (!file.isDirectory())
                loadKit(file);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
