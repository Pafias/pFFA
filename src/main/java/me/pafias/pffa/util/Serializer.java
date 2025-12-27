package me.pafias.pffa.util;

import com.google.gson.*;
import me.pafias.pffa.pFFA;
import me.pafias.putils.CC;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class Serializer {

    private static final pFFA plugin = pFFA.get();

    // Kits
    public static ItemStack jsonToInvItem(JsonObject json) {
        try {
            final Material material = Material.getMaterial(json.get("material").getAsString());
            if (material == null)
                throw new IllegalArgumentException("Invalid material: " + json.get("material").getAsString());
            final int amount = json.get("amount").getAsInt();
            final ItemStack is = new ItemStack(material, amount);
            if (json.has("enchantments")) {
                final ItemMeta meta = is.getItemMeta();
                for (final JsonElement enchantmentJsonElement : json.get("enchantments").getAsJsonArray()) {
                    if (!enchantmentJsonElement.isJsonObject()) {
                        plugin.getLogger().warning("Invalid enchantment found. Skipping it.");
                        continue;
                    }
                    final JsonObject enchantmentJson = enchantmentJsonElement.getAsJsonObject();
                    final Enchantment enchantment = enchantmentJson.has("key") ?
                            Enchantment.getByKey(NamespacedKey.fromString(enchantmentJson.get("key").getAsString())) :
                            Enchantment.getByName(enchantmentJson.get("name").getAsString());
                    if (enchantment == null) {
                        plugin.getLogger().warning("Invalid enchantment found. Skipping it.");
                        continue;
                    }
                    meta.addEnchant(enchantment, enchantmentJson.get("level").getAsInt(), true);
                }
                is.setItemMeta(meta);
            }
            if(json.has("potion-effects")){
                final JsonObject potionEffectsObject = json.getAsJsonObject("potion-effects");
                final PotionMeta meta = (PotionMeta) is.getItemMeta();
                final String typeString = potionEffectsObject.get("type").getAsString();
                if(typeString != null) {
                    final PotionType type = PotionType.valueOf(typeString);
                    if(type != null){
                        final boolean extended = potionEffectsObject.get("extended").getAsBoolean();
                        final boolean upgraded = potionEffectsObject.get("upgraded").getAsBoolean();
                        meta.setBasePotionData(new PotionData(type, extended, upgraded));
                        final JsonArray potionEffectsArray = potionEffectsObject.getAsJsonArray("custom-effects");
                        if (potionEffectsArray != null) {
                            for (final JsonElement potionEffectJsonElement : potionEffectsArray) {
                                String effectString = potionEffectJsonElement.getAsString();
                                String[] splitPotions = effectString.split(":");
                                String name = splitPotions[0];
                                if (name != null) {
                                    PotionEffectType potionType = PotionEffectType.getByName(name);
                                    int amplifier = Integer.parseInt(splitPotions[1]);
                                    int duration = Integer.parseInt(splitPotions[2]) * 20;
                                    if (potionType != null)
                                        meta.addCustomEffect(new PotionEffect(potionType, amplifier, duration), true);
                                }
                            }
                        }
                    }
                }
                is.setItemMeta(meta);
            }
            return is;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ItemStack(Material.STONE);
        }
    }

    public static JsonObject invItemToJson(int slot, ItemStack item) {
        try {
            final JsonObject json = new JsonObject();
            json.addProperty("slot", slot);
            json.addProperty("material", item.getType().name());
            json.addProperty("amount", item.getAmount());
            final JsonArray enchantmentsArray = new JsonArray();
            for (Enchantment enchantment : item.getEnchantments().keySet()) {
                final JsonObject json2 = new JsonObject();
                int level = item.getEnchantments().get(enchantment);
                json2.addProperty("key", enchantment.getKey().toString());
                json2.addProperty("name", enchantment.getName());
                json2.addProperty("level", level);
                enchantmentsArray.add(json2);
            }
            json.add("enchantments", enchantmentsArray);
            if (item.getItemMeta() instanceof PotionMeta) {
                final JsonObject potionEffectsObject = new JsonObject();
                final PotionMeta meta = (PotionMeta) item.getItemMeta();
                potionEffectsObject.addProperty("type", meta.getBasePotionData().getType().name());
                potionEffectsObject.addProperty("extended", meta.getBasePotionData().isExtended());
                potionEffectsObject.addProperty("upgraded", meta.getBasePotionData().isUpgraded());
                final JsonArray customEffectsArray = new JsonArray();
                for(final PotionEffect effect : meta.getCustomEffects()){
                    final String effectString = effect.getType().getName() + ":" + effect.getAmplifier() + ":" + (effect.getDuration() / 20);
                    customEffectsArray.add(effectString);
                }
                if(!customEffectsArray.isEmpty())
                    potionEffectsObject.add("custom-effects", customEffectsArray);
                json.add("potion-effects", potionEffectsObject);
            }
            return json;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Spawns

    public static Location jsonToLocation(JsonObject json) {
        final World world = plugin.getServer().getWorld(json.get("world").getAsString());
        final double x = json.get("x").getAsDouble();
        final double y = json.get("y").getAsDouble();
        final double z = json.get("z").getAsDouble();
        final float yaw = json.get("yaw").getAsFloat();
        final float pitch = json.get("pitch").getAsFloat();
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static JsonObject locationToJson(Location location) {
        final JsonObject json = new JsonObject();
        json.addProperty("world", location.getWorld().getName());
        json.addProperty("x", location.getBlockX() + 0.5);
        json.addProperty("y", location.getBlockY() + 0.1);
        json.addProperty("z", location.getBlockZ() + 0.5);
        json.addProperty("yaw", (int) location.getYaw());
        json.addProperty("pitch", location.getPitch() > 10 || location.getPitch() < -10 ? 0 : location.getPitch());
        return json;
    }

    // Both

    public static ItemStack jsonToGuiItem(JsonObject json) {
        try {
            final Material material = Material.getMaterial(json.get("material").getAsString());
            if (material == null)
                throw new IllegalArgumentException("Invalid material: " + json.get("material").getAsString());
            final ItemStack is = new ItemStack(material);
            final ItemMeta meta = is.getItemMeta();
            if (json.has("displayname"))
                meta.displayName(CC.a(json.get("displayname").getAsString()));
            if (json.has("lore")) {
                final List<Component> lore = new ArrayList<>();
                for (final JsonElement loreElement : json.getAsJsonArray("lore")) {
                    if (!loreElement.isJsonPrimitive()) continue;
                    lore.add(CC.a(loreElement.getAsString()));
                }
                meta.lore(lore);
            }
            is.setItemMeta(meta);
            return is;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ItemStack(Material.STONE);
        }
    }

    public static JsonObject guiItemToJson(ItemStack item) {
        try {
            final JsonObject json = new JsonObject();
            json.addProperty("material", item.getType().name());
            if (item.getItemMeta().hasDisplayName())
                json.addProperty("displayname", CC.serialize(item.getItemMeta().displayName()));
            if (item.getItemMeta().hasLore())
                json.add("lore", JsonParser.parseString(new GsonBuilder().create().toJson(item.getItemMeta().lore().stream().map(CC::serialize).toList())));
            return json;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Config

    public static Location parseConfigLocation(String path) {
        if (path == null) return null;
        final String worldName = plugin.getConfig().getString(path + ".world");
        final double x = plugin.getConfig().getDouble(path + ".x");
        final double y = plugin.getConfig().getDouble(path + ".y");
        final double z = plugin.getConfig().getDouble(path + ".z");
        final float yaw = (float) plugin.getConfig().getDouble(path + ".yaw");
        final float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");
        World world = null;
        if (worldName != null)
            world = plugin.getServer().getWorld(worldName);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static ConfigurationSection locationToConfig(String sectionName, Location location) {
        if (sectionName == null || location == null) return null;
        final ConfigurationSection section = plugin.getConfig().createSection(sectionName);
        if (location.getWorld() != null)
            section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
        return section;
    }

}
