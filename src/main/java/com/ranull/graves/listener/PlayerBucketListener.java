package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * Listener for handling PlayerBucketEmptyEvent and PlayerBucketFillEvent
 * to prevent buckets from being emptied or filled on grave blocks.
 */
public class PlayerBucketListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a PlayerBucketListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public PlayerBucketListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the PlayerBucketEmptyEvent to prevent emptying buckets on grave blocks.
     * If the block being interacted with is part of a grave, the event is cancelled
     * and the block's state is updated to reflect the prevention of the bucket emptying.
     *
     * @param event The PlayerBucketEmptyEvent to handle.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = getTargetBlock(event);

        if (isGraveBlock(block)) {
            preventBucketUsage(event, block);
        } else if (plugin.getGraveManager().isNearGrave(block.getLocation(), block)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the PlayerBucketFillEvent to prevent filling buckets on grave blocks.
     * If the block being interacted with is part of a grave, the event is cancelled
     * and the block's state is updated to reflect the prevention of the bucket filling.
     *
     * @param event The PlayerBucketFillEvent to handle.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Block block = event.getBlockClicked();

        if (isGraveBlock(block)) {
            preventBucketUsage(event, block);
        } else if (plugin.getGraveManager().isNearGrave(block.getLocation(), block)) {
            event.setCancelled(true);
        }
    }

    /**
     * Gets the block that the player is interacting with when emptying the bucket.
     *
     * @param event The PlayerBucketEmptyEvent.
     * @return The block being interacted with.
     */
    private Block getTargetBlock(PlayerBucketEmptyEvent event) {
        return event.getBlockClicked().getRelative(event.getBlockFace());
    }

    /**
     * Checks if the block is part of a grave.
     *
     * @param block The block to check.
     * @return True if the block is part of a grave, false otherwise.
     */
    private boolean isGraveBlock(Block block) {
        return plugin.getBlockManager().getGraveFromBlock(block) != null;
    }

    /**
     * Prevents the bucket from being emptied or filled on the grave block by cancelling the event
     * and updating the block's state.
     *
     * @param event The event to cancel.
     * @param block The block being interacted with.
     */
    private void preventBucketUsage(org.bukkit.event.Event event, Block block) {
        block.getState().update();
        if (event instanceof PlayerBucketEmptyEvent) {
            ((PlayerBucketEmptyEvent) event).setCancelled(true);
        } else if (event instanceof PlayerBucketFillEvent) {
            ((PlayerBucketFillEvent) event).setCancelled(true);
        }
    }
}