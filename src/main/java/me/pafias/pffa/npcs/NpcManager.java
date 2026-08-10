package me.pafias.pffa.npcs;

import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface NpcManager {

    void createNpc(String npcName, Component npcNametag, String npcSkinPlayerName, Location location, @Nullable Kit kit);

    void removeNpc(Location location);

    /**
     * Triggers the NPC interaction based on the entity clicked and the user.
     *
     * @param entity     The entity that was clicked. Nullable.
     * @param entityName The name of the entity that was clicked.
     * @param user       The user who clicked the entity.
     * @param leftClick  Whether the click was a left-click (true) or right-click (false).
     * @return True if a valid NPC was clicked, and the calling event should be canceled, false otherwise.
     */
    boolean trigger(@Nullable Entity entity, String entityName, User user, boolean leftClick);

    /**
     * Checks if the NPC exists in the registry.
     *
     * @param identifier The identifier to check for.
     * @return true if the NPC exists, false otherwise.
     */
    <T> boolean exists(T identifier);

    /**
     * Retrieves the backend-native NPC object for the given identifier (entity id or Bukkit Entity).
     *
     * @param identifier The identifier to look up the NPC by.
     * @return The NPC, or null if none is found.
     */
    <T, R> R getNpc(T identifier);

    void shutdown();

}
