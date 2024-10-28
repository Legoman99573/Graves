package com.ranull.graves.event;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The GraveBlockPlaceEvent class represents an event where a block associated
 * with a grave is placed in the world. This event is cancellable, meaning it
 * can be prevented from occurring by event listeners.
 */
public class GraveBlockPlaceEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new GraveBlockPlaceEvent.
     *
     * @param grave     The grave associated with the event.
     * @param location  The location where the block is being placed.
     * @param blockType The type of the block being placed.
     * @param block     The block being placed.
     * @param livingEntity The Killer
     */
    public GraveBlockPlaceEvent(Grave grave, Location location, BlockData.BlockType blockType, Block block, LivingEntity livingEntity) {
        super(grave, livingEntity, location, null, livingEntity, blockType, block, livingEntity.getKiller(), (Player) livingEntity);
    }

    /**
     * @deprecated Use {@link GraveBlockPlaceEvent#GraveBlockPlaceEvent(Grave, Location, BlockData.BlockType, Block, LivingEntity)} instead.
     * Constructs a new GraveBlockPlaceEvent.
     *
     * @param grave     The grave associated with the event.
     * @param location  The location where the block is being placed.
     * @param blockType The type of the block being placed.
     */
    @Deprecated
    public GraveBlockPlaceEvent(Grave grave, Location location, BlockData.BlockType blockType) {
        this(grave, location, blockType, null, null); // Delegate to the new constructor with nulls for the missing parameters
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