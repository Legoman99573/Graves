package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The GraveBreakEvent class represents an event where a grave block is broken
 * by a player. This event extends the GraveEvent and includes additional
 * information about the grave and whether items should drop upon breaking the grave block.
 */
public class GraveBreakEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new GraveBreakEvent.
     *
     * @param block  The block being broken.
     * @param player The player breaking the block.
     * @param grave  The grave associated with the block being broken.
     */
    public GraveBreakEvent(Block block, Player player, Grave grave) {
        super(grave, player, grave.getLocationDeath(), null, player, null, block, null, player);
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