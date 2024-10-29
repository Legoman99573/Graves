package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.event.GraveOpenEvent;
import com.ranull.graves.type.Grave;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listener for handling InventoryOpenEvent to manage interactions with grave inventories.
 */
public class InventoryOpenListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs an InventoryOpenListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public InventoryOpenListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the InventoryOpenEvent to manage interactions with grave inventories.
     * Creates and triggers a GraveOpenEvent when a Grave inventory is opened,
     * and cancels the open event if the GraveOpenEvent is cancelled.
     *
     * @param event The InventoryOpenEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (isGraveInventory(event)) {
            handleGraveInventoryOpen(event);
        }
    }

    /**
     * Checks if the inventory holder is a Grave.
     *
     * @param event The InventoryOpenEvent.
     * @return True if the inventory holder is a Grave, false otherwise.
     */
    private boolean isGraveInventory(InventoryOpenEvent event) {
        return event.getInventory().getHolder() instanceof Grave;
    }

    /**
     * Handles the opening of a Grave inventory by creating and triggering a GraveOpenEvent.
     *
     * @param event The InventoryOpenEvent.
     */
    private void handleGraveInventoryOpen(InventoryOpenEvent event) {
        Inventory topInventory = CompatibilityInventoryView.getTopInventory(event);
        InventoryHolder inventoryHolder = topInventory.getHolder();

        if (inventoryHolder instanceof Grave) {
            Grave grave = (Grave) inventoryHolder;
            Player player = (Player) event.getPlayer();

            GraveOpenEvent graveOpenEvent = new GraveOpenEvent(event.getView(), grave, player);

            // Call the custom GraveOpenEvent
            plugin.getServer().getPluginManager().callEvent(graveOpenEvent);

            if (graveOpenEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }
}