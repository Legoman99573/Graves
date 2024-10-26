package com.ranull.graves.manager;

import com.ranull.graves.data.ChunkData;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CacheManager {
    /**
     * A map of grave UUIDs to their corresponding {@link Grave} objects.
     * <p>
     * This {@link Map} associates each {@link UUID} with a {@link Grave} instance, allowing for quick retrieval
     * of grave information based on its unique identifier.
     * </p>
     */
    private final Map<UUID, Grave> graveMap;

    /**
     * A map of chunk identifiers to their corresponding {@link ChunkData} objects.
     * <p>
     * This {@link Map} associates each chunk identifier (as a {@link String}) with {@link ChunkData}, which holds
     * information about the specific chunk.
     * </p>
     */
    private final Map<String, ChunkData> chunkMap;

    /**
     * A map of entity UUIDs to their last known {@link Location}.
     * <p>
     * This {@link Map} tracks the most recent {@link Location} for each entity identified by its {@link UUID}.
     * </p>
     */
    private final Map<UUID, Location> lastLocationMap;

    /**
     * A map of entity UUIDs to lists of removed {@link ItemStack} objects.
     * <p>
     * This {@link Map} associates each entity's {@link UUID} with a {@link List} of {@link ItemStack} objects
     * that have been removed from the entity.
     * </p>
     */
    private final Map<UUID, List<ItemStack>> removedItemStackMap;

    /**
     * A map of block identifiers to their corresponding {@link Location} objects where the block was right-clicked.
     * <p>
     * This {@link Map} tracks the locations of blocks that have been right-clicked, identified by a {@link String}
     * representing the block identifier.
     * </p>
     */
    private final Map<String, Location> rightClickedBlocks = new HashMap<>();

    /**
     * Constructs a new {@link CacheManager} with initialized maps.
     * <p>
     * The constructor initializes all the maps used for caching data related to graves, chunks, locations, and items
     * </p>
     */
    public CacheManager() {
        this.graveMap = new HashMap<>();
        this.chunkMap = new HashMap<>();
        this.lastLocationMap = new HashMap<>();
        this.removedItemStackMap = new HashMap<>();
    }

    /**
     * Returns the map of grave UUIDs to their corresponding {@link Grave} objects.
     * @return the map of graves
     */
    public Map<UUID, Grave> getGraveMap() {
        return graveMap;
    }

    /**
     * Adds a right-clicked block location for a specified player.
     * @param playerName the name of the player
     * @param location the location of the right-clicked block
     */
    public void addRightClickedBlock(String playerName, Location location) {
        rightClickedBlocks.put(playerName, location);
    }

    /**
     * Retrieves the location of the right-clicked block for a specified player.
     * @param playerName the name of the player
     * @return the location of the right-clicked block, or {@code null} if not found
     */
    public Location getRightClickedBlock(String playerName) {
        return rightClickedBlocks.get(playerName);
    }

    /**
     * Removes the right-clicked block location for a specified player.
     * @param playerName the name of the player
     * @param location the location of the right-clicked block
     */
    public void removeRightClickedBlock(String playerName, Location location) {
        rightClickedBlocks.remove(playerName, location);
    }

    /**
     * Checks if a right-clicked block location exists for a specified player.
     * @param playerName the name of the player
     * @return {@code true} if the right-clicked block location exists, {@code false} otherwise
     */
    public boolean hasRightClickedBlock(String playerName) {
        return rightClickedBlocks.containsKey(playerName);
    }

    /**
     * Returns the map of chunk identifiers to their corresponding {@link ChunkData} objects.
     * @return the map of chunk data
     */
    public Map<String, ChunkData> getChunkMap() {
        return chunkMap;
    }

    /**
     * Returns the map of entity UUIDs to their last known {@link Location}.
     * @return the map of last known locations
     */
    public Map<UUID, Location> getLastLocationMap() {
        return lastLocationMap;
    }

    /**
     * Returns the map of entity UUIDs to lists of removed {@link ItemStack} objects.
     * @return the map of removed item stacks
     */
    public Map<UUID, List<ItemStack>> getRemovedItemStackMap() {
        return removedItemStackMap;
    }

    /**
     * Returns the oldest grave for a given player.
     * @param playerUUID The UUID of the player whose graves to consider.
     * @return The oldest grave for the specified player.
     */
    public Grave getOldestGrave(UUID playerUUID) {
        long oldestTime = Long.MAX_VALUE;
        Grave oldestGrave = null;

        for (Grave cur : graveMap.values()) { // Iterate over all graves
            if (cur.getOwnerUUID().equals(playerUUID)) { // Check if the grave belongs to the specified player
                long curTime = cur.getTimeCreation();
                if (curTime < oldestTime) {
                    oldestTime = curTime;
                    oldestGrave = cur;
                }
            }
        }

        return oldestGrave;
    }
}