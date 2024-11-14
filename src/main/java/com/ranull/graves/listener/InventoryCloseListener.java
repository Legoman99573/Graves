package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.event.GraveCloseEvent;
import com.ranull.graves.event.GraveLootedEvent;
import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Listener for handling InventoryCloseEvent to manage actions when a grave inventory is closed.
 */
public class InventoryCloseListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs an InventoryCloseListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public InventoryCloseListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the InventoryCloseEvent to perform actions when a grave inventory is closed.
     * Calls a custom GraveCloseEvent and manages the state of the grave based on its item amount.
     *
     * @param event The InventoryCloseEvent to handle.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isGraveInventory(event) && isPlayer(event.getPlayer())) {
            Player player = (Player) event.getPlayer();
            Entity entity = event.getPlayer();
            Inventory topInventory = CompatibilityInventoryView.getTopInventory(event);
            Grave grave = (Grave) topInventory.getHolder();

            // Call the custom GraveCloseEvent
            callGraveCloseEvent(event, grave, player, entity);

            if (grave != null && isEmptyGrave(grave)) {
                handleEmptyGrave(event, player, grave, entity);
            }

            // Play a sound effect related to closing the inventory
            plugin.getEntityManager().playWorldSound("sound.close", player, grave);
        }
    }

    /**
     * Checks if the event's inventory holder is a grave.
     *
     * @param event The InventoryCloseEvent.
     * @return True if the inventory holder is a grave, false otherwise.
     */
    private boolean isGraveInventory(InventoryCloseEvent event) {
        Inventory topInventory = CompatibilityInventoryView.getTopInventory(event);
        return topInventory.getHolder() instanceof Grave;
    }

    /**
     * Checks if the entity is a player.
     *
     * @param entity The entity to check.
     * @return True if the entity is a player, false otherwise.
     */
    private boolean isPlayer(Object entity) {
        return entity instanceof Player;
    }

    /**
     * Calls the custom GraveCloseEvent.
     *
     * @param event  The InventoryCloseEvent.
     * @param grave  The grave associated with the inventory.
     * @param player The player who closed the inventory.
     */
    private void callGraveCloseEvent(InventoryCloseEvent event, Grave grave, Player player, Entity entity) {
        GraveCloseEvent graveCloseEvent = new GraveCloseEvent(event.getView(), grave, player);
        if (!graveCloseEvent.isAddon()) {
            plugin.getServer().getPluginManager().callEvent(graveCloseEvent);
        }
    }

    /**
     * Calls the custom GraveCloseEvent.
     *
     * @param event  The InventoryCloseEvent.
     * @param grave  The grave associated with the inventory.
     * @param player The player who closed the inventory.
     */
    private void callGraveLootedEvent(InventoryCloseEvent event, Grave grave, Player player, Entity entity) {
        GraveLootedEvent graveLootedEvent = new GraveLootedEvent(event.getView(), grave, player);
        plugin.getServer().getPluginManager().callEvent(graveLootedEvent);
        if (plugin.getIntegrationManager().hasNoteBlockAPI()) {
            if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
            }
            if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
            }
        }
    }

    /**
     * Checks if the grave is empty.
     *
     * @param grave The grave to check.
     * @return True if the grave is empty, false otherwise.
     */
    private boolean isEmptyGrave(Grave grave) {
        return grave.getItemAmount() <= 0;
    }

    /**
     * Handles actions for an empty grave.
     *
     * @param player The player who closed the inventory.
     * @param grave  The empty grave.
     */
    private void handleEmptyGrave(InventoryCloseEvent event, Player player, Grave grave, Entity entity) {
        GraveLootedEvent graveLootedEvent = new GraveLootedEvent(event.getView(), grave, player);
        plugin.getServer().getPluginManager().callEvent(graveLootedEvent);

        // Remove the player from the grave's viewers
        grave.getInventory().getViewers().remove(player);

        if (!graveLootedEvent.isCancelled() && !graveLootedEvent.isAddon()) {
            // Execute commands and send messages related to the grave
            plugin.getEntityManager().runCommands("event.command.loot", player, player.getLocation(), grave);
            plugin.getEntityManager().sendMessage("message.looted", player, player.getLocation(), grave);

            // Spawn a zombie at the grave's death location
            plugin.getEntityManager().spawnZombie(grave.getLocationDeath(), player, player, grave);

            // Award experience and remove the grave
            plugin.getGraveManager().giveGraveExperience(player, grave);
            plugin.getGraveManager().removeGrave(grave);
            if (plugin.getIntegrationManager().hasNoteBlockAPI()) {
                if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                    plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
                }
                if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                    plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                }
            }
        }
    }
}