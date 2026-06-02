package me.pafias.pffa.events;

import lombok.Getter;
import lombok.Setter;
import me.pafias.pffa.objects.Kit;
import me.pafias.pffa.objects.Spawn;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player is gonna be teleported into an FFA spawn with a kit.
 */
@Getter
@Setter
public class FFAPlayerSpawnEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private Spawn spawn;

    @Nullable
    private Kit kit;

    public FFAPlayerSpawnEvent(@NotNull Player player, @NotNull Spawn spawn, @Nullable Kit kit) {
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

    private boolean cancelled;

}
