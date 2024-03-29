package me.pafias.pffa.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.util.CC;
import me.pafias.pffa.util.KitUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class KitManager {

    private final pFFA plugin;

    public KitManager(pFFA plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                loadKits();
            }
        }.runTaskAsynchronously(plugin);
    }

    private final LinkedHashMap<String, Kit> kits = new LinkedHashMap<>();

    private Kit defaultKit;

    public LinkedHashMap<String, Kit> getKits() {
        return kits;
    }

    public boolean exists(String name) {
        boolean found = kits.containsKey(name.toLowerCase());
        if (!found)
            found = kits.keySet().stream().anyMatch(n -> name.toLowerCase().contains(n.toLowerCase()));
        return found;
    }

    public Kit getDefaultKit() {
        return defaultKit;
    }

    public Kit getKit(String name) {
        Kit kit = kits.get(name.toLowerCase());
        if (kit == null)
            kit = kits.values().stream().filter(k -> name.toLowerCase().contains(k.getName().toLowerCase())).findAny().orElse(null);
        return kit;
    }

    public Kit getKit(ItemStack guiItem) {
        return kits.values().stream().filter(kit -> kit.getGUIItem().equals(guiItem)).findAny().orElse(null);
    }

    public void saveNewKit(Player player, String name) throws IOException {
        File file = new File(plugin.getDataFolder() + "/kits", name + ".json");
        if (!file.exists())
            file.createNewFile();
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.add("gui_item", KitUtils.guiItemToJson(player.getItemInHand()));
        JsonArray items = new JsonArray();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().equals(Material.AIR)) continue;
            items.add(KitUtils.invItemToJson(i, item));
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
            ItemStack gui_item = KitUtils.jsonToGuiItem(json.get("gui_item").getAsJsonObject());
            Map<Integer, ItemStack> items = new HashMap<>();
            for (int i = 0; i < json.get("items").getAsJsonArray().size(); i++) {
                JsonObject item = json.get("items").getAsJsonArray().get(i).getAsJsonObject();
                items.put(item.get("slot").getAsInt(), KitUtils.jsonToInvItem(item));
            }
            Collection<PotionEffect> potionEffects = new ArrayList<>();
            if (json.has("effects")) {
                for (JsonElement e : json.get("effects").getAsJsonArray()) {
                    JsonObject pe = e.getAsJsonObject();
                    PotionEffectType type = PotionEffectType.getByName(pe.get("type").getAsString().trim());
                    int duration = pe.get("duration").getAsInt();
                    int amplifier = pe.get("amplifier").getAsInt();
                    PotionEffect potionEffect = new PotionEffect(type, duration, amplifier, false, false);
                    potionEffects.add(potionEffect);
                }
            }
            kits.put(name.toLowerCase(), new Kit(name, gui_item, items, potionEffects));
        } catch (IOException ex) {
            ex.printStackTrace();
            plugin.getServer().getLogger().log(Level.WARNING, "");
            plugin.getServer().getLogger().log(Level.WARNING, CC.t("&cUnable to parse kit json. Read stacktrace above."));
            plugin.getServer().getLogger().log(Level.WARNING, "");
        }
    }

    public void loadKits() {
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
        defaultKit = kits.get(kits.keySet().iterator().next());
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
