package com.ranull.graves.event;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.event.interfaces.Addon;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base class for all grave-related events.
 * <p>
 * This class provides common properties for grave events, such as the grave itself,
 * the location of the event, the entity involved, and additional information like
 * inventory views and blocks. This class is cancellable, allowing event listeners
 * to prevent the event from proceeding.
 * </p>
 */
public abstract class GraveEvent extends Event implements Cancellable, Addon {
    /**
     * A static final instance of {@link HandlerList} used to manage event handlers.
     * <p>
     * This {@link HandlerList} is used to register and manage the handlers for events of this type.
     * It provides the mechanism for adding, removing, and invoking event handlers.
     * </p>
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The grave associated with the event.
     * <p>
     * This {@link Grave} represents the specific grave entity involved in the event.
     * </p>
     */
    private final Grave grave;

    /**
     * The entity associated with the event.
     * <p>
     * This {@link Entity} represents the entity that is involved in the event.
     * </p>
     */
    private final Entity entity;

    /**
     * The location related to the event.
     * <p>
     * This {@link Location} represents the position in the world where the event is taking place or is relevant.
     * </p>
     */
    private Location location;

    /**
     * The inventory view associated with the event.
     * <p>
     * This {@link InventoryView} represents the view of the inventory related to the event, such as a player's inventory or a chest.
     * </p>
     */
    private final InventoryView inventoryView;

    /**
     * The living entity involved in the event.
     * <p>
     * This {@link LivingEntity} represents the living entity that is part of the event.
     * </p>
     */
    private final LivingEntity livingEntity;

    /**
     * The target living entity of the event.
     * <p>
     * This {@link LivingEntity} represents the living entity that is the target or affected by the event.
     * </p>
     */
    private final LivingEntity targetEntity;

    /**
     * The type of block data associated with the event.
     * <p>
     * This {@link BlockData.BlockType} represents the type of block data relevant to the event.
     * </p>
     */
    private final BlockData.BlockType blockType;

    /**
     * The block associated with the event.
     * <p>
     * This {@link Block} represents the specific block involved in the event.
     * </p>
     */
    private final Block block;

    /**
     * The player associated with the event.
     * <p>
     * This {@link Player} represents the player involved in or affected by the event.
     * </p>
     */
    private final Player player;

    /**
     * Indicates whether the event has been cancelled.
     * <p>
     * This {@code boolean} flag is used to determine if the event should be cancelled or not.
     * </p>
     */
    private boolean isCancelled;

    /**
     * Indicates whether items should be dropped during the event.
     * <p>
     * This {@code boolean} flag determines if items should be dropped as a result of the event.
     * </p>
     */
    private boolean dropItems;

    /**
     * Indicates whether the event is an Addon
     * <p>
     * This {@code boolean} flag is used to determine if the event should be an addon or not.
     * </p>
     */
    private boolean isAddon;

    /**
     * Constructs a new {@code GraveEvent}.
     *
     * @param grave           The grave associated with the event.
     * @param entity          The entity involved in the event, if any.
     * @param location        The location of the event.
     * @param inventoryView   The inventory view associated with the event, if any.
     * @param livingEntity    The living entity associated with the event, if any.
     * @param blockType       The type of block involved in the event, if any.
     * @param block           The block involved in the event, if any.
     * @param targetEntity    The entity targeted by the event, if any.
     * @param player          The player involved in the event, if any.
     */
    public GraveEvent(Grave grave, @Nullable Entity entity, @Nullable Location location, @Nullable InventoryView inventoryView, @Nullable LivingEntity livingEntity, @Nullable BlockData.BlockType blockType, @Nullable Block block, @Nullable LivingEntity targetEntity, @Nullable Player player) {
        this.grave = grave;
        this.entity = entity;
        this.location = location;
        this.inventoryView = inventoryView;
        this.livingEntity = livingEntity;
        this.blockType = blockType;
        this.block = block;
        this.targetEntity = targetEntity;
        this.player = player;
        this.isCancelled = false;
        this.dropItems = true;
        this.isAddon = false;
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
     * Gets the entity involved in the event.
     *
     * @return The entity involved in the event, or null if not applicable.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the entity targeted by the event.
     *
     * @return The target entity, or null if not applicable.
     */
    public LivingEntity getTargetEntity() {
        return targetEntity;
    }

    /**
     * Gets the type of the target entity.
     *
     * @return The type of the target entity, or null if not applicable.
     */
    public EntityType getEntityType() {
        return targetEntity != null ? targetEntity.getType() : null;
    }

    /**
     * Gets the location of the event.
     *
     * @return The location of the event.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location of the event.
     *
     * @param location The new location of the event.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Gets the inventory view associated with the event.
     *
     * @return The inventory view, or null if not applicable.
     */
    @Nullable
    public InventoryView getInventoryView() {
        return inventoryView;
    }

    /**
     * Gets the living entity associated with the event.
     *
     * @return The living entity, or null if not applicable.
     */
    @Nullable
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * Gets the type of block involved in the event.
     *
     * @return The block type, or null if not applicable.
     */
    @Nullable
    public BlockData.BlockType getBlockType() {
        return blockType;
    }

    /**
     * Gets the block involved in the event.
     *
     * @return The block involved in the event, or null if not applicable.
     */
    @Nullable
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the experience points associated with the grave.
     *
     * @return The experience points.
     */
    public int getBlockExp() {
        return grave.getExperience();
    }

    /**
     * Sets the experience points associated with the grave.
     */
    public void setBlockExp(int experience) {
        grave.setExperience(experience);
    }

    /**
     * Checks whether items should drop upon breaking the grave block.
     *
     * @return True if items should drop, false otherwise.
     */
    public boolean isDropItems() {
        return this.dropItems;
    }

    /**
     * Sets whether items should drop upon breaking the grave block.
     *
     * @param dropItems True if items should drop, false otherwise.
     */
    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    /**
     * Gets the player involved in the event.
     *
     * @return The player involved in the event, or null if not applicable.
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Checks whether the event is cancelled.
     *
     * @return True if the event is cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Sets whether the event is cancelled.
     *
     * @param cancel True to cancel the event, false otherwise.
     */
    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    /**
     * Checks if the event is an addon hook.
     *
     * @return {@code true} if this is an addon, {@code false} otherwise.
     */
    @Override
    public boolean isAddon() {
        return isAddon;
    }

    /**
     * Sets the addon status for the current event.
     *
     * @param addon {@code true} to mark as an addon, {@code false} otherwise.
     */
    @Override
    public void setAddon(boolean addon) {
        isAddon = addon;
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

    /**
     * Gets the static list of handlers for this event.
     *
     * @return The static handler list for this event.
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}