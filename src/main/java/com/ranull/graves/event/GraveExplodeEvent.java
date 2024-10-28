package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an event that occurs when a grave explodes.
 * <p>
 * This event extends {@link GraveEvent} and is cancellable, allowing event listeners
 * to prevent the explosion from occurring if necessary.
 * </p>
 */
public class GraveExplodeEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveExplodeEvent}.
     *
     * @param location The location where the explosion occurs.
     * @param entity   The entity that caused the explosion, if any. This may be {@code null}
     *                 if no specific entity caused the explosion.
     * @param grave    The grave that is exploding.
     */
    public GraveExplodeEvent(Location location, @Nullable Entity entity, Grave grave) {
        super(grave, entity, location, null, (LivingEntity) entity, null, grave.getLocationDeath().getBlock(), null, (Player) entity);
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