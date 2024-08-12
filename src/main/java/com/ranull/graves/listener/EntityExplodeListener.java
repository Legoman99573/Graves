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

/**
 * Listener for handling entity explosion events and managing graves affected by explosions.
 */
public class EntityExplodeListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs an EntityExplodeListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public EntityExplodeListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the EntityExplodeEvent to manage graves affected by the explosion.
     *
     * @param event The EntityExplodeEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();

        try {
            while (iterator.hasNext()) {
                Block block = iterator.next();
                Grave grave = plugin.getBlockManager().getGraveFromBlock(block);

                if (grave != null) {
                    Location location = block.getLocation().clone();

                    if (isNewGrave(grave)) {
                        iterator.remove();
                    } else if (shouldExplode(grave)) {
                        handleGraveExplosion(event, iterator, block, grave, location);
                    } else {
                        iterator.remove();
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // End the loop if the exception occurs
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

        if (!graveExplodeEvent.isCancelled()) {
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
        } else {
            iterator.remove();
        }
    }
}