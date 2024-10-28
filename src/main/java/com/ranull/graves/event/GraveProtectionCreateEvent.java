package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when grave protection is created for an entity.
 * <p>
 * This event extends {@link GraveEvent} and is cancellable, allowing event listeners
 * to prevent the creation of the grave protection if necessary.
 * </p>
 */
public class GraveProtectionCreateEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveProtectionCreateEvent}.
     *
     * @param entity The entity for which the grave protection is being created.
     * @param grave  The grave being protected.
     */
    public GraveProtectionCreateEvent(Entity entity, Grave grave) {
        super(grave, entity, grave.getLocationDeath(), null, (LivingEntity) entity, null, null, null, (Player) entity);
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