package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when a grave times out.
 * <p>
 * This event extends {@link GraveEvent} and is cancellable, allowing event listeners
 * to prevent the grave timeout from occurring if necessary.
 * </p>
 */
public class GraveTimeoutEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveTimeoutEvent}.
     *
     * @param grave The grave that is timing out. The location of the grave at the time
     *              of timeout is automatically set from the grave's death location.
     */
    public GraveTimeoutEvent(Grave grave) {
        super(grave, null, grave.getLocationDeath(), null, null, null, grave.getLocationDeath().getBlock(), null, null);
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