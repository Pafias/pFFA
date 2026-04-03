package me.pafias.pffa.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import me.pafias.pffa.objects.Spawn;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.Serializer;
import me.pafias.putils.LCC;
import me.pafias.putils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SpawnManager {

    private final pFFA plugin;

    public SpawnManager(pFFA plugin) {
        this.plugin = plugin;
        loadSpawns();

        // Nearby players update task
        final Predicate<Player> predicate = p -> !plugin.getSM().getUserManager().getUser(p).isInSpawn();
        Tasks.runRepeatingSync(60, 60, () -> {
            for (final Spawn spawn : spawns.values()) {
                final Collection<Entity> nearbyEntities = spawn.getLocation().getWorld().getNearbyEntities(spawn.getLocation(), spawn.getPlayerDetectionRadius(), spawn.getPlayerDetectionRadius(), spawn.getPlayerDetectionRadius());
                spawn.setNearbyPlayers(
                        nearbyEntities
                                .stream()
                                .filter(e -> e instanceof Player)
                                .map(e -> (Player) e)
                                .filter(predicate)
                                .collect(Collectors.toList())
                );
            }
        });
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

        Map<String, Spawn> spawnsMap = new HashMap<>(spawns.size());
        for (Map.Entry<String, Spawn> entry : spawns.entrySet()) {
            final Spawn spawn = entry.getValue();
            if (!spawn.hasPermission() || player.hasPermission(spawn.getPermission()))
                spawnsMap.put(entry.getKey(), spawn);
        }
        return spawnsMap;
    }

    public boolean exists(String name) {
        if (spawns.containsKey(name.toLowerCase()))
            return true;
        for (String spawnName : spawns.keySet()) {
            if (name.toLowerCase().contains(spawnName.toLowerCase()))
                return true;
        }
        return false;
    }

    public Spawn getSpawn(String name) {
        Spawn spawn = spawns.get(name.toLowerCase());
        if (spawn == null)
            for (Map.Entry<String, Spawn> entry : spawns.entrySet()) {
                if (name.toLowerCase().contains(entry.getKey().toLowerCase()))
                    spawn = entry.getValue();
            }
        return spawn;
    }

    public Spawn getDefaultSpawn() {
        return spawns.isEmpty() ? null : spawns.values().iterator().next();
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
        json.add("gui_item", Serializer.guiItemToJson(player.getInventory().getItemInHand()));
        json.add("location", Serializer.locationToJson(player.getLocation()));
        FileWriter writer = new FileWriter(file);
        writer.write(json.toString());
        writer.close();
        loadSpawn(file);
    }

    public Spawn loadSpawn(File file) {
        try {
            String jsonText = readAll(new FileReader(file));
            JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
            String name = json.get("name").getAsString();
            String permission = json.has("permission") ?
                    json.get("permission").getAsString() : null;
            ItemStack gui_item = Serializer.jsonToGuiItem(json.get("gui_item").getAsJsonObject());
            Location location = Serializer.jsonToLocation(json.get("location").getAsJsonObject());
            double playerDetectionRadius = json.has("player_detection_radius") ?
                    json.get("player_detection_radius").getAsDouble() : 20;
            Spawn spawn = new Spawn(name, permission, gui_item, location, playerDetectionRadius, new HashSet<>());
            spawns.put(name.toLowerCase(), spawn);
            return spawn;
        } catch (IOException ex) {
            ex.printStackTrace();
            plugin.getServer().getLogger().log(Level.WARNING, "");
            plugin.getServer().getLogger().log(Level.WARNING, LCC.t("&cUnable to parse spawn json. Read stacktrace above."));
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
