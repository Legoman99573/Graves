package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.event.GraveExplodeEvent;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Listens for EntityExplodeEvent to handle interactions with grave blocks when they are affected by entity explosions.
 */
public class EntityExplodeListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a new EntityExplodeListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public EntityExplodeListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles EntityExplodeEvent to manage grave interactions when blocks are exploded by entities.
     *
     * @param event The EntityExplodeEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> affectedBlocks = event.blockList();

        Iterator<Block> iterator = affectedBlocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Location blockLocation = block.getLocation();

            Grave grave = plugin.getBlockManager().getGraveFromBlock(block);
            if (grave != null) {
                if (plugin.getConfig("grave.explode-protection", grave).getBoolean("grave.explode-protection")){
                    for (Block blocks : affectedBlocks) {
                        Location blockLocations = block.getLocation();
                        Grave graves = plugin.getBlockManager().getGraveFromBlock(blocks);

                        if (graves != null) {
                            Location graveLocation = plugin.getGraveManager().getGraveLocation(blockLocations, graves);
                            if (graveLocation != null) {
                                try {
                                    double distance = blockLocation.distance(graveLocation);
                                    int protectionRadius = plugin.getConfig("grave.protection-radius", grave).getInt("grave.protection-radius");
                                    if (protectionRadius != 0 && distance + 15 <= protectionRadius + 15) {
                                        event.blockList().clear();
                                        event.setCancelled(true);
                                    }
                                } catch (IllegalArgumentException ignored) {

                                }
                            }
                        }
                    }
                } else {
                    Location graveHeadLocation = grave.getLocationDeath();
                    if (graveHeadLocation.equals(blockLocation)) {
                        iterator.remove();
                    }
                }

                if (shouldExplode(grave)) {
                    handleGraveExplosion(event, iterator, block, grave, blockLocation);
                }
            }
        }
    }

    /**
     * Checks if the grave should explode based on the configuration.
     *
     * @param grave The grave to check.
     * @return True if the grave should explode, false otherwise.
     */
    private boolean shouldExplode(Grave grave) {
        return plugin.getConfig("grave.explode", grave).getBoolean("grave.explode");
    }

    /**
     * Handles the explosion of a grave.
     *
     * @param event     The EntityExplodeEvent.
     * @param iterator  The iterator for the blocks in the explosion.
     * @param block     The block that exploded.
     * @param grave     The grave associated with the block.
     * @param location  The location of the grave.
     */
    private void handleGraveExplosion(EntityExplodeEvent event, Iterator<Block> iterator, Block block, Grave grave, Location location) {
        GraveExplodeEvent graveExplodeEvent = new GraveExplodeEvent(location, event.getEntity(), grave);
        plugin.getServer().getPluginManager().callEvent(graveExplodeEvent);

        if (!graveExplodeEvent.isCancelled() && !graveExplodeEvent.isAddon()) {
            if (plugin.getConfig("drop.explode", grave).getBoolean("drop.explode")) {
                plugin.getGraveManager().breakGrave(location, grave);
            } else {
                plugin.getGraveManager().removeGrave(grave);
            }

            plugin.getGraveManager().closeGrave(grave);
            plugin.getGraveManager().playEffect("effect.loot", location, grave);
            plugin.getEntityManager().runCommands("event.command.explode", event.getEntity(), location, grave);

            if (plugin.getConfig("zombie.explode", grave).getBoolean("zombie.explode")) {
                plugin.getEntityManager().spawnZombie(location, grave);
            }
        } else if (graveExplodeEvent.isCancelled() && !graveExplodeEvent.isAddon()) {
            iterator.remove();
        }
    }
}