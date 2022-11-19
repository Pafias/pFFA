package me.pafias.pafiasffa.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.Kit;
import me.pafias.pafiasffa.util.CC;
import me.pafias.pafiasffa.util.KitUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class KitManager {

    private final PafiasFFA plugin;

    public KitManager(PafiasFFA plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                loadKits();
            }
        }.runTaskLaterAsynchronously(plugin, (2 * 20));
    }

    private final List<Kit> kits = new ArrayList<>();

    public List<Kit> getKits() {
        return kits;
    }

    public boolean exists(String name) {
        return kits.stream().anyMatch(kit -> kit.getName().equalsIgnoreCase(name) || kit.getName().toLowerCase().contains(name.toLowerCase()) || name.toLowerCase().contains(kit.getName().toLowerCase()));
    }

    public Kit getDefaultKit() {
        return kits.get(0);
    }

    public Kit getKit(String name) {
        return kits.stream().filter(kit -> kit.getName().equalsIgnoreCase(name) || kit.getName().toLowerCase().contains(name.toLowerCase()) || name.toLowerCase().contains(kit.getName().toLowerCase())).findAny().orElse(null);
    }

    public Kit getKit(ItemStack guiItem) {
        return kits.stream().filter(kit -> kit.getGUIItem().equals(guiItem)).findAny().orElse(null);
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

            kits.add(new Kit(name, gui_item, items));
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
