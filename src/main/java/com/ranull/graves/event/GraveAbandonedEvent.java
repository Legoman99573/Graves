package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when a grave is abandoned.
 * <p>
 * This event extends {@link GraveEvent} and provides information about the grave
 * that is abandoned.
 * </p>
 */
public class GraveAbandonedEvent extends GraveEvent {

    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveAbandonedEvent}.
     *
     * @param grave The grave that is abandoned. The location of the grave at the time
     *              of abandonment is automatically set from the grave's death location.
     */
    public GraveAbandonedEvent(Grave grave) {
        super(grave, null, grave.getLocationDeath(), null, null, null, null, null, null);
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