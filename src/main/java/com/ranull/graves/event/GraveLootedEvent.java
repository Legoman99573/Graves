package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that occurs when an inventory associated with a grave is completely looted.
 * <p>
 * This event extends {@link GraveEvent} and provides information about the grave
 * and the player involved when the inventory is completely looted.
 * </p>
 */
public class GraveLootedEvent extends GraveEvent {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new {@code GraveLootedEvent}.
     *
     * @param inventoryView The inventory view that has been fully looted.
     * @param grave         The grave associated with the inventory view.
     * @param player        The player who is closing the inventory.
     */
    public GraveLootedEvent(InventoryView inventoryView, Grave grave, Player player) {
        super(grave, player, grave.getLocationDeath(), inventoryView, player, null, null, null, player);
    }

    /**
     * @deprecated Use {@link #GraveLootedEvent(InventoryView, Grave, Player)} instead.
     * Constructs a new {@code GraveLootedEvent}.
     *
     * @param inventoryView The inventory view that has been fully looted.
     * @param grave         The grave associated with the inventory view.
     * @param entity        The entity who is closing the inventory.
     */
    @Deprecated
    public GraveLootedEvent(Grave grave, InventoryView inventoryView, Entity entity) {
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