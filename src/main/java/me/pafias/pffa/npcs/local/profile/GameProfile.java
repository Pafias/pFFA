package me.pafias.pffa.npcs.local.profile;

import lombok.Getter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Getter
public class GameProfile {

    private final Object handle;

    private final UUID id;
    private final String name;
    private final PropertyMap properties = new PropertyMap();
    private boolean legacy;

    public GameProfile(Object handle) {
        try {
            Class<?> gameProfileClass = handle.getClass();
            if (!gameProfileClass.getSimpleName().endsWith("GameProfile"))
                throw new IllegalArgumentException("handle is not a GameProfile instance");
            this.handle = handle;

            this.id = (UUID) gameProfileClass.getMethod("getId").invoke(handle);
            this.name = (String) gameProfileClass.getMethod("getName").invoke(handle);

            Object propertiesHandle = gameProfileClass.getMethod("getProperties").invoke(handle);
            Class<?> propertyMapClass;
            Class<?> propertyClass;

            try {
                propertyMapClass = Class.forName("com.mojang.authlib.properties.PropertyMap");
                propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            } catch (ClassNotFoundException ex) {
                try {
                    propertyMapClass = Class.forName("net.minecraft.util.com.mojang.authlib.properties.PropertyMap");
                    propertyClass = Class.forName("net.minecraft.util.com.mojang.authlib.properties.Property");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not find PropertyMap or Property class", e);
                }
            }

            Method asMapMethod = propertyMapClass.getMethod("asMap");
            Object rawMapObject = asMapMethod.invoke(propertiesHandle);

            if (rawMapObject instanceof java.util.Map) {
                Map<String, ? extends Collection<?>> rawMap =
                        (Map<String, ? extends Collection<?>>) rawMapObject;

                for (Map.Entry<String, ? extends Collection<?>> entry : rawMap.entrySet()) {
                    String key = entry.getKey();
                    for (Object rawProperty : entry.getValue()) {
                        if (!propertyClass.isInstance(rawProperty)) {
                            Bukkit.getLogger().warning("Encountered object in property map that is not a Property instance: " + rawProperty.getClass().getName());
                            continue;
                        }

                        String value = (String) propertyClass.getMethod("getValue").invoke(rawProperty);
                        String signature = null;
                        try {
                            signature = (String) propertyClass.getMethod("getSignature").invoke(rawProperty);
                        } catch (NoSuchMethodException ignored) {
                        }
                        Property property = new Property(key, value, signature);
                        this.properties.put(key, property);
                    }
                }
            }

            try {
                this.legacy = (boolean) gameProfileClass.getMethod("isLegacy").invoke(handle);
            } catch (NoSuchMethodException e) {
                this.legacy = false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GameProfile from handle", e);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            GameProfile that = (GameProfile) o;
            if (this.id != null) {
                if (!this.id.equals(that.id)) {
                    return false;
                }
            } else if (that.id != null) {
                return false;
            }

            if (this.name != null) {
                return this.name.equals(that.name);
            } else return that.name == null;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.id != null ? this.id.hashCode() : 0;
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        return result;
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("id", this.id).append("name", this.name).append("properties", this.properties).append("legacy", this.legacy).toString();
    }

}
