package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.event.GraveExplodeEvent;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Listens for BlockExplodeEvent to handle interactions with grave blocks when they are affected by explosions.
 */
public class BlockExplodeListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a new BlockExplodeListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public BlockExplodeListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles BlockExplodeEvent to manage grave interactions when blocks are exploded.
     *
     * @param event The BlockExplodeEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> affectedBlocks = event.blockList();
        Iterator<Block> iterator = affectedBlocks.iterator();
        boolean cancelEvent = false;

        while (iterator.hasNext()) {
            Block block = iterator.next();
            Location blockLocation = block.getLocation();

            Grave grave = plugin.getBlockManager().getGraveFromBlock(block);
            if (grave != null) {
                if (isNewGrave(grave)) {
                    if (isNearGrave(blockLocation, block)) {
                        cancelEvent = true;
                        iterator.remove();
                    }
                } else if (shouldExplode(grave)) {
                    handleGraveExplosion(event, iterator, block, grave, blockLocation);
                } else if (isNearGrave(blockLocation, block)) {
                    cancelEvent = true;
                    iterator.remove();
                } else {
                    cancelEvent = true;
                    iterator.remove();
                }
            }
        }

        if (cancelEvent) {
            event.setCancelled(true);
            event.blockList().clear();
        }
    }

    /**
     * Checks if the grave is newly created.
     *
     * @param grave The grave to check.
     * @return True if the grave is newly created, false otherwise.
     */
    private boolean isNewGrave(Grave grave) {
        return (System.currentTimeMillis() - grave.getTimeCreation()) < 1000;
    }

    /**
     * Checks if the grave should explode based on the configuration.
     *
     * @param grave The grave to check.
     * @return True if the grave should explode, false otherwise.
     */
    private boolean shouldExplode(Grave grave) {
        return plugin.getConfig("explode", grave).getBoolean("explode");
    }

    /**
     * Handles the explosion of a grave.
     *
     * @param event     The BlockExplodeEvent.
     * @param iterator  The iterator for the blocks in the explosion.
     * @param block     The block that exploded.
     * @param grave     The grave associated with the block.
     * @param location  The location of the grave.
     */
    private void handleGraveExplosion(BlockExplodeEvent event, Iterator<Block> iterator, Block block, Grave grave, Location location) {
        GraveExplodeEvent graveExplodeEvent = new GraveExplodeEvent(location, null, grave);
        plugin.getServer().getPluginManager().callEvent(graveExplodeEvent);

        if (!graveExplodeEvent.isCancelled()) {
            if (plugin.getConfig("drop.explode", grave).getBoolean("drop.explode")) {
                plugin.getGraveManager().breakGrave(location, grave);
            } else {
                plugin.getGraveManager().removeGrave(grave);
            }

            plugin.getGraveManager().closeGrave(grave);
            plugin.getGraveManager().playEffect("effect.loot", location, grave);
            plugin.getEntityManager().runCommands("event.command.explode", block.getType().name(), location, grave);

            if (plugin.getConfig("zombie.explode", grave).getBoolean("zombie.explode")) {
                plugin.getEntityManager().spawnZombie(location, grave);
            }
        } else {
            iterator.remove();
        }
    }

    /**
     * Checks if the given location is within 15 blocks of any grave.
     *
     * @param location The location to check.
     * @return True if the location is within 15 blocks of any grave, false otherwise.
     */
    private boolean isNearGrave(Location location, Block block) {
        try {
            for (Grave grave : plugin.getCacheManager().getGraveMap().values()) {
                Location graveLocation = plugin.getGraveManager().getGraveLocation(block.getLocation(), grave);
                if (graveLocation != null) {
                    double distance = location.distance(graveLocation);
                    if (plugin.getConfig("grave.protection-radius", grave).getInt("grave.protection-radius") != 0 && distance <= plugin.getConfig("grave.protection-radius", grave).getInt("grave.protection-radius")) {
                        return true;
                    }
                }
            }
        }  catch (Exception ignored) {
            // ignore
        }
        return false;
    }
}