package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;

/**
 * Listener for handling PlayerQuitEvent to manage player-related data when they leave the game.
 */
public class PlayerQuitListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a PlayerQuitListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public PlayerQuitListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the PlayerQuitEvent to clean up player-related data upon their departure.
     *
     * This method removes the player's last solid location and stops any ongoing modification.
     *
     * @param event The PlayerQuitEvent to handle.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        removeLastSolidLocation(player);
    }

    /**
     * Removes the player's last solid location from the plugin's location manager.
     *
     * @param player The player whose last solid location should be removed.
     */
    private void removeLastSolidLocation(Player player) {
        plugin.getLocationManager().removeLastSolidLocation(player);
    }
}