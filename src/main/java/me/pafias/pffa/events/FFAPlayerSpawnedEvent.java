package me.pafias.pffa.events;

import lombok.Getter;
import lombok.Setter;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired after a player is teleported into an FFA spawn, with their kit already applied.
 */
@Getter
@Setter
public class FFAPlayerSpawnedEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @Nullable
    private final Spawn spawn;

    @Nullable
    private final Kit kit;

    public FFAPlayerSpawnedEvent(@NotNull Player player, @Nullable Spawn spawn, @Nullable Kit kit) {
        super(player);
        this.spawn = spawn;
        this.kit = kit;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
