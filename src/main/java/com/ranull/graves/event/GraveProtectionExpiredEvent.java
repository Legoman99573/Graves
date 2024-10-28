package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The GraveProtectionExpiredEvent class represents an event that occurs when the protection of a grave expires.
 */
public class GraveProtectionExpiredEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new GraveProtectionExpiredEvent.
     *
     * @param grave The grave whose protection is expiring.
     */
    public GraveProtectionExpiredEvent(Grave grave) {
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