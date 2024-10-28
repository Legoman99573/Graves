package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event where a grave is automatically looted.
 * <p>
 * This event extends {@link GraveEvent} and provides information about the entity involved,
 * the location of the grave, and the grave itself.
 * </p>
 */
public class GraveAutoLootEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveAutoLootEvent}.
     *
     * @param entity   The entity involved in the auto-loot process.
     * @param location The location of the grave being looted.
     * @param grave    The grave that is being looted.
     */
    public GraveAutoLootEvent(Entity entity, Location location, Grave grave) {
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