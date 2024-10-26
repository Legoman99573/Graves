package com.ranull.graves.data;

import org.bukkit.Location;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents block data associated with a grave.
 */
public class BlockData implements Serializable {
    /**
     * The location of the grave in the world.
     * <p>
     * This {@link Location} represents the coordinates where the grave is situated.
     * </p>
     */
    private final Location location;

    /**
     * The unique identifier for the grave.
     * <p>
     * This {@link UUID} uniquely identifies the grave entity.
     * </p>
     */
    private final UUID graveUUID;

    /**
     * The material used to replace the grave block.
     * <p>
     * This {@link String} specifies the material to replace the block at the grave's location.
     * </p>
     */
    private final String replaceMaterial;

    /**
     * The data associated with the replacement material.
     * <p>
     * This {@link String} provides additional data or properties for the replacement material, if applicable.
     * </p>
     */
    private final String replaceData;

    /**
     * Constructs a new BlockData instance.
     *
     * @param location        The location of the block.
     * @param graveUUID       The UUID of the associated grave.
     * @param replaceMaterial The material to replace the block with.
     * @param replaceData     The data to replace the block with.
     */
    public BlockData(Location location, UUID graveUUID, String replaceMaterial, String replaceData) {
        this.location = location;
        this.graveUUID = graveUUID;
        this.replaceMaterial = replaceMaterial;
        this.replaceData = replaceData;
    }

    /**
     * Gets the location of the block.
     *
     * @return The location of the block.
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * Gets the UUID of the associated grave.
     *
     * @return The UUID of the grave.
     */
    public UUID getGraveUUID() {
        return graveUUID;
    }

    /**
     * Gets the material to replace the block with.
     *
     * @return The replacement material.
     */
    public String getReplaceMaterial() {
        return replaceMaterial;
    }

    /**
     * Gets the data to replace the block with.
     *
     * @return The replace data.
     */
    public String getReplaceData() {
        return replaceData;
    }

    /**
     * Enum representing the type of block.
     */
    public enum BlockType {
        DEATH,
        NORMAL
    }
}
