package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when a zombie spawns at a grave.
 * <p>
 * This event extends {@link GraveEvent} and provides details about the location of the spawn
 * and the entity that the zombie is targeting.
 * </p>
 */
public class GraveZombieSpawnEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveZombieSpawnEvent}.
     *
     * @param location        The location where the zombie is spawning.
     * @param targetEntity    The entity that the zombie is targeting.
     * @param grave           The grave associated with the event.
     */
    public GraveZombieSpawnEvent(Location location, LivingEntity targetEntity, Grave grave) {
        super(grave, targetEntity, location, null, targetEntity, null, grave.getLocationDeath().getBlock(), targetEntity, (Player) targetEntity);
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