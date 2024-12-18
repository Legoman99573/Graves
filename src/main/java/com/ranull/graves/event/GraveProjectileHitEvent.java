package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when a grave is hit with a projectile.
 * <p>
 * This event extends {@link GraveEvent} and provides information about the grave
 * that is hit with a projectile.
 * </p>
 */
public class GraveProjectileHitEvent extends GraveEvent {

    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveEvent}.
     *
     * @param location      The location of the event.
     * @param player        The player involved in the event, if any.
     * @param grave         The grave associated with the event.
     * @param entity          The entity involved in the event, if any.
     * @param block           The block involved in the event, if any.
     */
    public GraveProjectileHitEvent(Location location, Player player, Grave grave, Entity entity, Block block) {
        super(grave, entity, location, null, null, null, block, null, player);
    }

    /**
     * Constructs a new {@code GraveEvent}.
     *
     * @param location      The location of the event.
     * @param livingEntity  The livingEntity involved in the event, if any.
     * @param grave         The grave associated with the event.
     * @param entity          The entity involved in the event, if any.
     * @param block           The block involved in the event, if any.
     */
    public GraveProjectileHitEvent(Location location, LivingEntity livingEntity, Grave grave, Entity entity, Block block) {
        super(grave, entity, location, null, livingEntity, null, block, null, null);
    }

    /**
     * Constructs a new {@code GraveEvent}.
     *
     * @param location      The location of the event.
     * @param grave         The grave associated with the event.
     * @param entity          The entity involved in the event, if any.
     * @param block           The block involved in the event, if any.
     */
    public GraveProjectileHitEvent(Location location, Grave grave, Entity entity, Block block) {
        super(grave, entity, location, null, null, null, block, null, null);
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
