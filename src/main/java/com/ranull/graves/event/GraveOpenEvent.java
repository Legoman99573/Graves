package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when a player opens an inventory associated
 * with a grave.
 * <p>
 * This event extends {@link GraveEvent} and includes additional information
 * about the grave and the inventory view that is being opened.
 * </p>
 */
public class GraveOpenEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveOpenEvent}.
     *
     * @param inventoryView The inventory view that is being opened.
     * @param grave         The grave associated with the inventory view.
     * @param player        The player who is opening the inventory.
     */
    public GraveOpenEvent(InventoryView inventoryView, Grave grave, Player player) {
        super(grave, player, grave.getLocationDeath(), inventoryView, player, null, grave.getLocationDeath().getBlock(), null, player);
    }

    /**
     * @deprecated          Use {@link #GraveOpenEvent(InventoryView, Grave, Player)} instead for better player logging.
     * Constructs a new {@code GraveOpenEvent}.
     *
     * @param inventoryView The inventory view that is being opened.
     * @param grave         The grave associated with the inventory view.
     * @param entity        The entity who is opening the inventory.
     */
    @Deprecated
    public GraveOpenEvent(InventoryView inventoryView, Grave grave, Entity entity) {
        this(inventoryView, grave, entity instanceof Player ? (Player) entity : null);
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