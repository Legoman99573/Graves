package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.event.GraveAutoLootEvent;
import com.ranull.graves.event.GraveBreakEvent;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listens for BlockBreakEvent to handle interactions with grave blocks and
 * prevent block breaking within a specified radius of a grave.
 */
public class BlockBreakListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a new BlockBreakListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public BlockBreakListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles BlockBreakEvent to manage interactions with grave blocks when a block is broken.
     *
     * Checks if the broken block is associated with a grave. If so, verifies if breaking the grave
     * is allowed and if the player has permission to do so. If not a grave block, it checks if
     * the block is within a 15-block radius of any grave and cancels the event if so.
     *
     * @param event The BlockBreakEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Grave grave = plugin.getBlockManager().getGraveFromBlock(block);

        if (grave != null) {
            if (isGraveBreakAllowed(grave)) {
                if (plugin.getEntityManager().canOpenGrave(player, grave)) {
                    handleGraveBreak(event, player, block, grave);
                } else {
                    plugin.getEntityManager().sendMessage("message.protection", player, player.getLocation(), grave);
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else if (isNearGrave(block.getLocation(), player)) {
            event.setCancelled(true);
            plugin.getEntityManager().sendMessage("message.grave-protection-break-deny", player);
        }
    }

    /**
     * Checks if breaking the grave is allowed based on the configuration.
     *
     * @param grave The grave to check.
     * @return True if breaking the grave is allowed, false otherwise.
     */
    private boolean isGraveBreakAllowed(Grave grave) {
        return plugin.getConfig("grave.break", grave).getBoolean("grave.break");
    }

    /**
     * Handles the process of breaking a grave.
     *
     * @param event   The BlockBreakEvent.
     * @param player  The player breaking the block.
     * @param block   The block being broken.
     * @param grave   The grave associated with the block.
     */
    private void handleGraveBreak(BlockBreakEvent event, Player player, Block block, Grave grave) {
        GraveBreakEvent graveBreakEvent = new GraveBreakEvent(block, player, grave);
        graveBreakEvent.setDropItems(plugin.getConfig("drop.break", grave).getBoolean("drop.break"));
        plugin.getServer().getPluginManager().callEvent(graveBreakEvent);

        if (!graveBreakEvent.isCancelled() && !graveBreakEvent.isAddon()) {
            if (plugin.getConfig("drop.auto-loot.enabled", grave).getBoolean("drop.auto-loot.enabled")) {
                handleAutoLoot(event, player, block, grave, graveBreakEvent);
            } else if (graveBreakEvent.isDropItems()) {
                plugin.getGraveManager().breakGrave(block.getLocation(), grave);
            } else {
                plugin.getGraveManager().removeGrave(grave);
            }

            if (graveBreakEvent.getBlockExp() > 0) {
                plugin.getGraveManager().dropGraveExperience(block.getLocation(), grave);
            }

            finalizeGraveBreak(player, block, grave);
        } else if (graveBreakEvent.isCancelled() && !graveBreakEvent.isAddon()) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the auto-loot process when breaking a grave.
     *
     * @param event           The BlockBreakEvent.
     * @param player          The player breaking the block.
     * @param block           The block being broken.
     * @param grave           The grave associated with the block.
     * @param graveBreakEvent The GraveBreakEvent.
     */
    private void handleAutoLoot(BlockBreakEvent event, Player player, Block block, Grave grave, GraveBreakEvent graveBreakEvent) {
        GraveAutoLootEvent graveAutoLootEvent = new GraveAutoLootEvent(player, block.getLocation(), grave);
        plugin.getServer().getPluginManager().callEvent(graveAutoLootEvent);

        if (!graveAutoLootEvent.isCancelled() && !graveAutoLootEvent.isAddon()) {
            plugin.getGraveManager().autoLootGrave(player, block.getLocation(), grave);

            if (graveBreakEvent.isDropItems() && plugin.getConfig("drop.auto-loot.break", grave).getBoolean("drop.auto-loot.break")) {
                plugin.getGraveManager().breakGrave(block.getLocation(), grave);
                if (plugin.getIntegrationManager().hasNoteBlockAPI()) {
                    if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                        plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
                    }
                    if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                        plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Finalizes the process of breaking a grave by closing the grave, playing effects, and running commands.
     *
     * @param player The player breaking the block.
     * @param block  The block being broken.
     * @param grave  The grave associated with the block.
     */
    private void finalizeGraveBreak(Player player, Block block, Grave grave) {
        plugin.getGraveManager().closeGrave(grave);
        plugin.getGraveManager().playEffect("effect.loot", block.getLocation(), grave);
        plugin.getEntityManager().spawnZombie(block.getLocation(), player, player, grave);
        plugin.getEntityManager().runCommands("event.command.break", player, block.getLocation(), grave);
    }

    /**
     * Checks if the given location is within 15 blocks of any grave.
     *
     * @param location The location to check.
     * @return True if the location is within 15 blocks of any grave, false otherwise.
     */
    private boolean isNearGrave(Location location, Player player) {
        try {
            for (Grave grave : plugin.getCacheManager().getGraveMap().values()) {
                Location graveLocation = plugin.getGraveManager().getGraveLocation(player.getLocation(), grave);
                if (graveLocation != null) {
                    double distance = location.distance(graveLocation);
                    if (plugin.getConfig("grave.protection-radius", grave).getInt("grave.protection-radius") != 0 && distance <= plugin.getConfig("grave.protection-radius", grave).getInt("grave.protection-radius")) {
                        return true;
                    }
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Assuming grave is in another world
        }
        return false;
    }
}