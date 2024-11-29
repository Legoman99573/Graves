package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Listens for PlayerTeleportEvent to handle interactions with grave blocks and
 * remove specific items from the player's inventory if they teleport into a grave location.
 */
public class PlayerTeleportListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a new PlayerTeleportListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public PlayerTeleportListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles PlayerTeleportEvent to manage interactions with grave blocks when a player teleports.
     *
     * Checks if the player's new location is within a 15-block radius of any grave and removes
     * specific compass items from their inventory if so.
     *
     * @param event The PlayerTeleportEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location newLocation = event.getTo();

        // Check if the teleport destination is a grave location
        if (plugin.getGraveManager().isNearGrave(newLocation, player)) {
            removeSpecificCompassNearGrave(player, newLocation);
        }
    }

    /**
     * Removes a specific type of compass (e.g., RECOVERY_COMPASS) from the player's inventory if within a configured block radius of a grave.
     *
     * @param player   The player to check.
     * @param location The player's current location.
     */
    private void removeSpecificCompassNearGrave(Player player, Location location) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();

        for (ItemStack item : items) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    // Check if the item is a compass with the specific name
                    if ((item.getType() == Material.valueOf(String.valueOf(plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS"))))
                            && itemMeta.hasDisplayName()) {

                        UUID graveUUID = getGraveUUIDFromItemStack(item);

                        if (graveUUID != null) {
                            Grave grave = plugin.getCacheManager().getGraveMap().get(graveUUID);
                            try {
                                if (grave != null && location.getWorld() != null) {
                                    Location graveLocation = plugin.getGraveManager().getGraveLocation(player.getLocation(), grave);
                                    if (graveLocation != null && location.distance(graveLocation) <= 15) {
                                        // Remove the specific item from the inventory
                                        String compassName;
                                        if (plugin.getIntegrationManager().hasMiniMessage()) {
                                            String compassNameNew = StringUtil.parseString("&f" + plugin
                                                    .getConfig("compass.name", grave).getString("compass.name"), grave, plugin);
                                            compassName = MiniMessage.parseString(compassNameNew);
                                        } else {
                                            compassName = StringUtil.parseString("&f" + plugin
                                                    .getConfig("compass.name", grave).getString("compass.name"), grave, plugin);
                                        }
                                        if (itemMeta.getDisplayName().equals(compassName)) {
                                            inventory.remove(item);
                                        }
                                    }
                                }
                            }  catch (IllegalArgumentException | NullPointerException ignored) {
                                // ignored
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the Grave UUID from the item stack.
     *
     * @param itemStack The item stack to check.
     * @return The UUID of the grave associated with the item stack, or null if not found.
     */
    private UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            if (itemStack.getItemMeta() == null) return null;
            String uuidString = itemStack.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING);
            return uuidString != null ? UUID.fromString(uuidString) : null;
        }
        return null;
    }
}