package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 * Listens for BlockBurnEvent and BlockIgniteEvent to prevent lava from burning or destroying blocks within a grave's radius.
 */
public class BlockBurnAndIgniteListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a new BlockBurnAndIgniteListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public BlockBurnAndIgniteListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles BlockSpreadEvent to prevent from spreading if within a grave's radius, like fire.
     *
     * @param event The BlockSpreadEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles BlockBurnEvent to prevent blocks from burning if within a grave's radius.
     *
     * @param event The BlockBurnEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getIgnitingBlock()) || plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles BlockIgniteEvent to prevent blocks from being ignited within a grave's radius.
     *
     * @param event The BlockIgniteEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getIgnitingBlock()) ||
                plugin.getGraveManager().isNearGrave(event.getBlock().getLocation(), event.getBlock())) {
            event.setCancelled(true);
        }
    }
}