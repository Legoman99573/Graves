package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The GraveOpenEvent class represents an event where an inventory associated
 * with a grave is opened. This event extends the InventoryOpenEvent and includes
 * additional information about the grave.
 */
public class GraveOpenEvent extends InventoryOpenEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Grave grave;
    private final Entity entity;

    /**
     * Constructs a new GraveOpenEvent.
     *
     * @param inventoryView The inventory view that is being opened.
     * @param grave         The grave associated with the inventory view.
     */
    public GraveOpenEvent(InventoryView inventoryView, Grave grave, Entity entity) {
        super(inventoryView);
        this.grave = grave;
        this.entity = entity;
    }

    /**
     * Gets the list of handlers for this event.
     *
     * @return The list of handlers.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Gets the list of handlers for this event.
     *
     * @return The list of handlers.
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the grave associated with the event.
     *
     * @return The grave associated with the event.
     */
    public Grave getGrave() {
        return grave;
    }

    /**
     * Gets the entity that caused the explosion, if any.
     *
     * @return The entity that caused the explosion, or null if there is no entity.
     */
    @Nullable
    public Entity getEntity() {
        return entity;
    }
}