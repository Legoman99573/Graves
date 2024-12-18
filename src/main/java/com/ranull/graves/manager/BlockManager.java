package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The BlockManager class is responsible for managing block data and operations related to graves.
 */
public final class BlockManager {
    private final Graves plugin;

    /**
     * Initializes a new instance of the BlockManager class.
     *
     * @param plugin The plugin instance.
     */
    public BlockManager(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the BlockData associated with the specified block.
     *
     * @param block The block to get the data for.
     * @return The BlockData associated with the block, or null if not found.
     */
    public BlockData getBlockData(Block block) {
        if (plugin.getDataManager().hasChunkData(block.getLocation())) {
            ChunkData chunkData = plugin.getDataManager().getChunkData(block.getLocation());

            if (chunkData.getBlockDataMap().containsKey(block.getLocation())) {
                return chunkData.getBlockDataMap().get(block.getLocation());
            }
        }

        return null;
    }

    /**
     * Gets the grave associated with the specified block.
     *
     * @param block The block to get the grave for.
     * @return The grave associated with the block, or null if not found.
     */
    public Grave getGraveFromBlock(Block block) {
        BlockData blockData = getBlockData(block);

        return blockData != null && plugin.getCacheManager().getGraveMap().containsKey(blockData.getGraveUUID())
                ? plugin.getCacheManager().getGraveMap().get(blockData.getGraveUUID()) : null;
    }

    /**
     * Gets the grave block associated with the specified location.
     *
     * @param location The location to get the grave for.
     * @return The grave associated with the location, or null if not found.
     */
    public Grave getGraveFromBlockLocation(Location location) {
        if (location == null) return null;

        Block block = location.getBlock();
        BlockData blockData = getBlockData(block);

        return blockData != null && plugin.getCacheManager().getGraveMap().containsKey(blockData.getGraveUUID())
                ? plugin.getCacheManager().getGraveMap().get(blockData.getGraveUUID()) : null;
    }

    /**
     * Creates a block at the specified location for the given grave.
     *
     * @param location The location to create the block.
     * @param grave    The grave associated with the block.
     */
    public void createBlock(Location location, Grave grave) {
        location = LocationUtil.roundLocation(location);

        if (location.getWorld() != null) {
            Material material;

            if (plugin.getConfig("block.enabled", grave).getBoolean("block.enabled")) {
                String materialString = plugin.getConfig("block.material", grave)
                        .getString("block.material", "CHEST");

                if (materialString.equals("PLAYER_HEAD") && !plugin.getVersionManager().hasBlockData()) {
                    materialString = "SKULL";
                }

                material = Material.matchMaterial(materialString);
            } else {
                material = null;
            }

            int offsetX = plugin.getConfig("block.offset.x", grave).getInt("block.offset.x");
            int offsetY = plugin.getConfig("block.offset.y", grave).getInt("block.offset.y");
            int offsetZ = plugin.getConfig("block.offset.z", grave).getInt("block.offset.z");

            location.add(offsetX, offsetY, offsetZ);

            BlockData blockData = plugin.getCompatibility().setBlockData(location,
                    material, grave, plugin);

            plugin.getDataManager().addBlockData(blockData);

            if (plugin.getIntegrationManager().hasMultiPaper()) {
                plugin.getIntegrationManager().getMultiPaper().notifyBlockCreation(blockData);
            }

            if (plugin.getIntegrationManager().hasItemsAdder()) {
                plugin.getIntegrationManager().getItemsAdder().createBlock(location, grave);
            }

            if (plugin.getIntegrationManager().hasOraxen()) {
                plugin.getIntegrationManager().getOraxen().createBlock(location, grave);
            }

            if (material != null) {
                plugin.debugMessage("Placing grave block for " + grave.getUUID() + " at "
                        + location.getWorld().getName() + ", " + (location.getBlockX() + 0.5) + "x, "
                        + (location.getBlockY() + 0.5) + "y, " + (location.getBlockZ() + 0.5) + "z", 1);
            } else {
                plugin.debugMessage("Placing access location for " + grave.getUUID() + " at "
                        + location.getWorld().getName() + ", " + (location.getBlockX() + 0.5) + "x, "
                        + (location.getBlockY() + 0.5) + "y, " + (location.getBlockZ() + 0.5) + "z", 1);
            }
        }
    }

    /**
     * Gets a list of BlockData associated with the given grave.
     *
     * @param grave The grave to get the BlockData list for.
     * @return A list of BlockData associated with the grave.
     */
    public List<BlockData> getBlockDataList(Grave grave) {
        List<BlockData> blockDataList = new ArrayList<>();

        for (Map.Entry<String, ChunkData> chunkDataEntry : plugin.getCacheManager().getChunkMap().entrySet()) {
            for (BlockData blockData : new ArrayList<>(chunkDataEntry.getValue().getBlockDataMap().values())) {
                if (grave.getUUID().equals(blockData.getGraveUUID())) {
                    blockDataList.add(blockData);
                }
            }
        }

        return blockDataList;
    }

    /**
     * Gets a list of locations of blocks associated with the given grave.
     *
     * @param grave The grave to get the block locations for.
     * @return A list of locations of blocks associated with the grave.
     */
    public List<Location> getBlockList(Grave grave) {
        List<Location> locationList = new ArrayList<>();

        for (Map.Entry<String, ChunkData> chunkDataEntry : plugin.getCacheManager().getChunkMap().entrySet()) {
            for (BlockData blockData : new ArrayList<>(chunkDataEntry.getValue().getBlockDataMap().values())) {
                if (grave.getUUID().equals(blockData.getGraveUUID())) {
                    locationList.add(blockData.getLocation());
                }
            }
        }

        return locationList;
    }

    /**
     * Removes all blocks associated with the given grave.
     *
     * @param grave The grave to remove the blocks for.
     */
    public void removeBlock(Grave grave) {
        for (ChunkData chunkData : plugin.getCacheManager().getChunkMap().values()) {

            if (chunkData.isLoaded()) {
                for (BlockData blockData : new ArrayList<>(chunkData.getBlockDataMap().values())) {
                    if (grave.getUUID().equals(blockData.getGraveUUID())) {
                        removeBlock(blockData);
                    }
                }
            }
        }
    }

    /**
     * Removes a specific block represented by the given BlockData.
     *
     * @param blockData The BlockData representing the block to remove.
     */
    public void removeBlock(BlockData blockData) {
        Location location = blockData.getLocation();

        if (plugin.getIntegrationManager().hasItemsAdder() && plugin.getIntegrationManager().getItemsAdder()
                .isCustomBlock(location)) {
            plugin.getIntegrationManager().getItemsAdder().removeBlock(location);
        }

        if (plugin.getIntegrationManager().hasOraxen() && plugin.getIntegrationManager().getOraxen()
                .isCustomBlock(location)) {
            plugin.getIntegrationManager().getOraxen().removeBlock(location);
        }

        if (location.getWorld() != null) {
            if (blockData.getReplaceMaterial() != null) {
                Material material = Material.matchMaterial(blockData.getReplaceMaterial());

                if (material != null) {
                    blockData.getLocation().getBlock().setType(material);
                }
            } else {
                blockData.getLocation().getBlock().setType(Material.AIR);
            }

            if (blockData.getReplaceData() != null) {
                blockData.getLocation().getBlock().setBlockData(plugin.getServer()
                        .createBlockData(blockData.getReplaceData()));
            }

            plugin.getDataManager().removeBlockData(location);
            plugin.debugMessage("Replacing grave block for " + blockData.getGraveUUID() + " at "
                    + location.getWorld().getName() + ", " + (location.getBlockX() + 0.5) + "x, "
                    + (location.getBlockY() + 0.5) + "y, " + (location.getBlockZ() + 0.5) + "z", 1);
        }
    }
}