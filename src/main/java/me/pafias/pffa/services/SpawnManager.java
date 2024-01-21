package me.pafias.pffa.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.CC;
import me.pafias.pffa.util.SpawnUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Level;

public class SpawnManager {

    private final pFFA plugin;

    public SpawnManager(pFFA plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                loadSpawns();
            }
        }.runTaskAsynchronously(plugin);
    }

    private final LinkedHashMap<String, Spawn> spawns = new LinkedHashMap<>();

    private Spawn defaultSpawn;

    public LinkedHashMap<String, Spawn> getSpawns() {
        return spawns;
    }

    public boolean exists(String name) {
        boolean found = spawns.containsKey(name.toLowerCase());
        if (!found)
            found = spawns.keySet().stream().anyMatch(n -> name.toLowerCase().contains(n.toLowerCase()));
        return found;
    }

    public Spawn getSpawn(String name) {
        Spawn spawn = spawns.get(name.toLowerCase());
        if (spawn == null)
            spawn = spawns.values().stream().filter(s -> name.toLowerCase().contains(s.getName().toLowerCase())).findAny().orElse(null);
        return spawn;
    }

    public Spawn getSpawn(ItemStack guiItem) {
        return spawns.values().stream().filter(spawn -> spawn.getGUIItem().equals(guiItem)).findAny().orElse(null);
    }

    public Spawn getDefaultSpawn() {
        return defaultSpawn;
    }

    public void saveNewSpawn(Player player, String name) throws IOException {
        if (exists(name)) return;
        File file = new File(plugin.getDataFolder() + "/spawns", name + ".json");
        if (file.exists()) return;
        file.createNewFile();
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.add("gui_item", SpawnUtils.guiItemToJson(player.getItemInHand()));
        json.add("location", SpawnUtils.locationToJson(player.getLocation()));
        FileWriter writer = new FileWriter(file);
        writer.write(json.toString());
        writer.close();
        Spawn spawn = loadSpawn(file);
        if (defaultSpawn == null)
            defaultSpawn = spawn;
    }

    public Spawn loadSpawn(File file) {
        try {
            String jsonText = readAll(new FileReader(file));
            JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
            String name = json.get("name").getAsString();
            ItemStack gui_item = SpawnUtils.jsonToGuiItem(json.get("gui_item").getAsJsonObject());
            Location location = SpawnUtils.jsonToLocation(json.get("location").getAsJsonObject());
            Spawn spawn = new Spawn(name, gui_item, location);
            spawns.put(name.toLowerCase(), spawn);
            return spawn;
        } catch (IOException ex) {
            ex.printStackTrace();
            plugin.getServer().getLogger().log(Level.WARNING, "");
            plugin.getServer().getLogger().log(Level.WARNING, CC.t("&cUnable to parse spawn json. Read stacktrace above."));
            plugin.getServer().getLogger().log(Level.WARNING, "");
            return null;
        }
    }

    public void loadSpawns() {
        File dir = new File(plugin.getDataFolder() + "/spawns");
        if (!dir.exists())
            dir.mkdirs();
        File[] files = dir.listFiles();
        if (files.length == 0) return;
        Arrays.sort(files);
        for (File file : files) {
            if (!file.isDirectory())
                loadSpawn(file);
        }
        defaultSpawn = spawns.get(spawns.keySet().iterator().next());
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
