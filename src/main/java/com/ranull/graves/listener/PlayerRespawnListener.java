package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Listener for handling PlayerRespawnEvent to manage grave-related functionality upon player respawn.
 */
public class PlayerRespawnListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a PlayerRespawnListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public PlayerRespawnListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the PlayerRespawnEvent to perform actions related to graves when a player respawns.
     *
     * This method:
     * - Runs a scheduled task to execute a function configured for respawn events.
     * - Checks if a compass should be given to the player based on the respawn time and config settings.
     * - Applies a potion effect if the player respawns within the allowed time.
     *
     * @param event The PlayerRespawnEvent to handle.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<String> permissionList = plugin.getPermissionList(player);
        List<Grave> graveList = plugin.getGraveManager().getGraveList(player);

        if (!graveList.isEmpty()) {
            Grave grave = graveList.get(graveList.size() - 1);

            // Schedule a function to run after player respawn
            scheduleRespawnFunction(player, permissionList, grave);

            // Apply potion effect if within allowed time
            applyPotionEffectIfWithinTime(player, permissionList, grave);

            // Check if a compass should be given to the player
            if (shouldGiveCompass(player, permissionList, grave)) {
                giveCompassToPlayer(event, player, grave);
            }
        }
    }

    /**
     * Applies a potion effect to the player if they respawn within the allowed time.
     *
     * @param player The player who respawned.
     * @param permissionList The list of permissions for the player.
     * @param grave The grave associated with the player.
     */
    private void applyPotionEffectIfWithinTime(Player player, List<String> permissionList, Grave grave) {
        // Schedule the task to run after the player has respawned
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Check if potion effect is enabled and player has the appropriate permission
            boolean isPotionEffectEnabled = plugin.getConfig("respawn.potion-effect", player, permissionList)
                    .getBoolean("respawn.potion-effect");
            boolean hasPotionEffectPermission = plugin.hasGrantedPermission("graves.potion-effect", player.getPlayer());

            if (!isPotionEffectEnabled || !hasPotionEffectPermission) {
                return;
            }

            // Get the respawn time limit
            long respawnTimeLimit = plugin.getConfig("respawn.potion-effect-time-limit", player, permissionList)
                    .getInt("respawn.potion-effect-time-limit") * 1000L;

            // Check if the grave's lived time is within the respawn time limit
            if (grave.getLivedTime() <= respawnTimeLimit) {
                // Get the potion effect duration
                int effectDuration = plugin.getConfig("respawn.potion-effect-duration", player, permissionList)
                        .getInt("respawn.potion-effect-duration") * 20; // Duration in ticks (20 ticks = 1 second)

                // Create and apply potion effects
                PotionEffect potionEffect = new PotionEffect(plugin.getVersionManager().getPotionEffectTypeFromVersion("RESISTANCE"), effectDuration, 4);
                PotionEffect potionEffect2 = new PotionEffect(plugin.getVersionManager().getPotionEffectTypeFromVersion("FIRE_RESISTANCE"), effectDuration, 0);
                player.addPotionEffect(potionEffect);
                player.addPotionEffect(potionEffect2);
            }
        }, 1L); // Run 1 tick after respawn
    }

    /**
     * Schedules a function to run after the player respawns.
     *
     * @param player The player who respawned.
     * @param permissionList The list of permissions for the player.
     * @param grave The grave associated with the player.
     */
    private void scheduleRespawnFunction(Player player, List<String> permissionList, Grave grave) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getEntityManager().runFunction(player, plugin
                    .getConfig("respawn.function", player, permissionList)
                    .getString("respawn.function", "none"), grave);
        }, 1L);
    }

    /**
     * Checks if a compass should be given to the player based on the respawn time and configuration settings.
     *
     * @param player The player who respawned.
     * @param permissionList The list of permissions for the player.
     * @param grave The grave associated with the player.
     * @return True if a compass should be given, false otherwise.
     */
    private boolean shouldGiveCompass(Player player, List<String> permissionList, Grave grave) {
        return plugin.getVersionManager().hasCompassMeta()
                && plugin.getConfig("respawn.compass", player, permissionList)
                .getBoolean("respawn.compass")
                && grave.getLivedTime() <= plugin.getConfig("respawn.compass-time", player, permissionList)
                .getInt("respawn.compass-time") * 1000L;
    }

    /**
     * Gives a compass to the player that points to the location of their grave.
     *
     * @param event The PlayerRespawnEvent.
     * @param player The player who respawned.
     * @param grave The grave associated with the player.
     */
    private void giveCompassToPlayer(PlayerRespawnEvent event, Player player, Grave grave) {
        List<Location> locationList = plugin.getGraveManager()
                .getGraveLocationList(event.getRespawnLocation(), grave);

        if (!locationList.isEmpty()) {
            ItemStack itemStack = plugin.getEntityManager().createGraveCompass(player, locationList.get(0), grave);

            if (itemStack != null) {
                player.getInventory().addItem(itemStack);
            }
        }
    }
}
