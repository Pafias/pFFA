package me.pafias.pffa.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.Serializer;
import me.pafias.putils.CC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class SpawnManager {

    private final pFFA plugin;

    public SpawnManager(pFFA plugin) {
        this.plugin = plugin;
        loadSpawns();
    }

    @Getter
    private final LinkedHashMap<String, Spawn> spawns = new LinkedHashMap<>();

    /**
     * Get all spawns that the player has permission to.
     *
     * @param player The player to check permissions for. If null, all spawns are returned.
     * @return A map of spawn names to Spawn objects, filtered by permission.
     */
    public Map<String, Spawn> getSpawns(@Nullable Player player) {
        if (player == null)
            return spawns;
        return spawns.entrySet().stream()
                .filter(entry -> !entry.getValue().hasPermission() || player.hasPermission(entry.getValue().getPermission()))
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
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

    public Spawn getDefaultSpawn() {
        return spawns.values().stream().findFirst().orElse(null);
    }

    public void saveNewSpawn(Player player, String name, String permission) throws IOException {
        if (exists(name)) return;
        File file = new File(plugin.getDataFolder() + "/spawns", name + ".json");
        if (file.exists()) return;
        file.createNewFile();
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        if (permission != null && !permission.isEmpty())
            json.addProperty("permission", permission);
        json.add("gui_item", Serializer.guiItemToJson(player.getInventory().getItemInMainHand()));
        json.add("location", Serializer.locationToJson(player.getLocation()));
        FileWriter writer = new FileWriter(file);
        writer.write(json.toString());
        writer.close();
        loadSpawn(file);
    }

    public Spawn loadSpawn(File file) {
        try {
            String jsonText = readAll(new FileReader(file));
            JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
            String name = json.get("name").getAsString();
            String permission = json.has("permission") ?
                    json.get("permission").getAsString() : null;
            ItemStack gui_item = Serializer.jsonToGuiItem(json.get("gui_item").getAsJsonObject());
            Location location = Serializer.jsonToLocation(json.get("location").getAsJsonObject());
            double playerDetectionRadius = json.has("player_detection_radius") ?
                    json.get("player_detection_radius").getAsDouble() : 20;
            Spawn spawn = new Spawn(name, permission, gui_item, location, playerDetectionRadius);
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
        spawns.clear();
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
