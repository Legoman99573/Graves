package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when a grave compass is used.
 * <p>
 * This event extends {@link GraveEvent} and is cancellable, allowing event listeners
 * to prevent the creation of the grave if necessary.
 * </p>
 */
public class GraveCompassUseEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveCompassUseEvent}.
     *
     * @param player The player for which is using the compass.
     * @param grave  The grave being created.
     */
    public GraveCompassUseEvent(Player player, Grave grave) {
        super(grave, null, grave.getLocationDeath(), null, null, null, null, null, player);
    }

    /**
     * @deprecated Use {@link #GraveCompassUseEvent(Player, Grave)} instead.
     * Constructs a new {@code GraveCompassUseEvent}.
     *
     * @param entity The entity for which is using the compass.
     * @param grave  The grave being created.
     */
    @Deprecated
    public GraveCompassUseEvent(Entity entity, Grave grave) {
        this(entity instanceof Player ? (Player) entity : null, grave);
    }

    /**
     * Gets the list of handlers for this event.
     *
     * @return The handler list for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Gets the list of handlers for this event.
     *
     * @return The handler list for this event.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
