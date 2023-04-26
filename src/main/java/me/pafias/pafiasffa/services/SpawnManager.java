package me.pafias.pafiasffa.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pafias.pafiasffa.PafiasFFA;
import me.pafias.pafiasffa.objects.Spawn;
import me.pafias.pafiasffa.util.CC;
import me.pafias.pafiasffa.util.SpawnUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SpawnManager {

    private final PafiasFFA plugin;

    public SpawnManager(PafiasFFA plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                loadSpawns();
            }
        }.runTaskAsynchronously(plugin);
    }

    private final List<Spawn> spawns = new ArrayList<>();

    public List<Spawn> getSpawns() {
        return spawns;
    }

    public boolean exists(String name) {
        return spawns.stream().anyMatch(spawn -> spawn.getName().equalsIgnoreCase(name) || spawn.getName().toLowerCase().contains(name.toLowerCase()) || name.toLowerCase().contains(spawn.getName().toLowerCase()));
    }

    public Spawn getSpawn(String name) {
        return spawns.stream().filter(spawn -> spawn.getName().equalsIgnoreCase(name) || spawn.getName().toLowerCase().contains(name.toLowerCase()) || name.toLowerCase().contains(spawn.getName().toLowerCase())).findAny().orElse(null);
    }

    public Spawn getSpawn(ItemStack guiItem) {
        return spawns.stream().filter(spawn -> spawn.getGUIItem().equals(guiItem)).findAny().orElse(null);
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
        loadSpawn(file);
    }

    public void loadSpawn(File file) {
        try {
            String jsonText = readAll(new FileReader(file));
            JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
            String name = json.get("name").getAsString();
            ItemStack gui_item = SpawnUtils.jsonToGuiItem(json.get("gui_item").getAsJsonObject());
            Location location = SpawnUtils.jsonToLocation(json.get("location").getAsJsonObject());
            spawns.add(new Spawn(name, gui_item, location));
        } catch (IOException ex) {
            ex.printStackTrace();
            plugin.getServer().getLogger().log(Level.WARNING, "");
            plugin.getServer().getLogger().log(Level.WARNING, CC.t("&cUnable to parse spawn json. Read stacktrace above."));
            plugin.getServer().getLogger().log(Level.WARNING, "");
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
