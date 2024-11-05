package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.inventory.GraveList;
import com.ranull.graves.inventory.GraveMenu;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.InventoryUtil;
import com.ranull.graves.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Listener for handling InventoryClickEvent to manage grave-related inventory interactions.
 */
public class InventoryClickListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs an InventoryClickListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public InventoryClickListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the InventoryClickEvent to perform actions based on the type of inventory holder.
     * Updates grave inventories and handles interactions with GraveList and GraveMenu inventories.
     *
     * @param event The InventoryClickEvent to handle.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        if (inventoryHolder != null) {
            if (inventoryHolder instanceof Grave) {
                handleGraveInventoryClick(event, player, (Grave) inventoryHolder);
            } else if (event.getWhoClicked() instanceof Player) {
                handlePlayerInventoryClick(event, player, inventoryHolder);
            }
            ClickType clickType = event.getClick();
            if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                handleShiftClick(event);
            }
            isCompassItem(event);

        }
    }

    /**
     * Checks if a specific type of compass (e.g., RECOVERY_COMPASS) was clicked in the player's inventory.
     * Also checks if the clicked inventory involves XP-granting inventories like FURNACE, ANVIL, etc.
     *
     * @param event The InventoryClickEvent to check.
     */
    private void isCompassItem(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;


        ItemStack item = event.getCursor();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) return;

        if (item.getType() == Material.valueOf(String.valueOf(plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS")))) {
            UUID graveUUID = getGraveUUIDFromItemStack(item);
            if (graveUUID != null) {
                Grave grave = plugin.getCacheManager().getGraveMap().get(graveUUID);
                if (grave != null) {

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
                        InventoryType inventoryType = event.getInventory().getType();
                        if (checkIfXPGivingInventory(inventoryType)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle shift-clicking logic, preventing players from shift-clicking the compass into a restricted inventory.
     *
     * @param event The InventoryClickEvent triggered by the player shift-clicking.
     */
    private void handleShiftClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) return;

        if (item.getType() == Material.valueOf(String.valueOf(plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS")))) {
            UUID graveUUID = getGraveUUIDFromItemStack(item);
            if (graveUUID != null) {
                Grave grave = plugin.getCacheManager().getGraveMap().get(graveUUID);
                if (grave != null) {

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
                        InventoryType inventoryType = event.getInventory().getType();
                        if (checkIfXPGivingInventory(inventoryType)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the inventory type is one that grants XP (e.g., Furnace, Anvil, Grindstone).
     *
     * @param inventoryType The type of the inventory.
     * @return true if the inventory grants XP, false otherwise.
     */
    private boolean checkIfXPGivingInventory(InventoryType inventoryType) {
        switch (inventoryType.name()) {
            case "FURNACE":
            case "BLAST_FURNACE":
            case "SMOKER":
            case "ANVIL":
            case "GRINDSTONE":
            case "HOPPER":
                return true;
            default:
                return false;
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

    /**
     * Handles inventory clicks when the inventory holder is a Grave.
     *
     * @param event  The InventoryClickEvent.
     * @param grave  The Grave inventory holder.
     */
//    private void handleGraveInventoryClick(InventoryClickEvent event, Player player, Grave grave) {
//        if (!grave.getGravePreview()) {
//            if (plugin.getEntityManager().canOpenGrave(player, grave)) {
//                // Schedule a task to update the grave's inventory in the data manager
//                plugin.getServer().getScheduler().runTaskLater(plugin, () ->
//                        plugin.getDataManager().updateGrave(grave, "inventory",
//                                InventoryUtil.inventoryToString(grave.getInventory())), 1L);
//            } else {
//                event.setCancelled(true);
//            }
//        } else {
//            event.setCancelled(true);
//        }
//    }
    private void handleGraveInventoryClick(InventoryClickEvent event, Player player, Grave grave) {
        if (!grave.getGravePreview()) {
            if (plugin.getEntityManager().canOpenGrave(player, grave)) {
                Inventory clickedInventory = event.getClickedInventory();
                InventoryAction action = event.getAction();
                Inventory topInventory = CompatibilityInventoryView.getTopInventory(event); // The grave's inventory
                Inventory bottomInventory = CompatibilityInventoryView.getBottomInventory(event); // The player's inventory
                Grave.StorageMode storageMode = plugin.getGraveManager().getStorageMode(plugin.getConfig("storage.mode", grave)
                        .getString("storage.mode"));

                // If the player is interacting with the grave's inventory
                if (clickedInventory != null) {
                    if (storageMode == Grave.StorageMode.EXACT) {
                        // Block shift-clicking items INTO the grave from player's inventory
                        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedInventory.equals(bottomInventory)) {
                            event.setCancelled(true);
                            return;
                        }


                    // Block manual placement of items into the grave
                    if (clickedInventory.equals(grave.getInventory())) {
                        if (action == InventoryAction.PLACE_ALL
                                || action == InventoryAction.PLACE_SOME
                                || action == InventoryAction.PLACE_ONE
                                || action == InventoryAction.SWAP_WITH_CURSOR) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                        plugin.getDataManager().updateGrave(grave, "inventory",
                                InventoryUtil.inventoryToString(grave.getInventory())), 1L);
                    }

                        // Allow shift-clicking or picking items OUT of the grave
                        if (action == InventoryAction.PICKUP_ALL
                                || action == InventoryAction.PICKUP_SOME
                                || action == InventoryAction.PICKUP_HALF
                                || action == InventoryAction.PICKUP_ONE
                                || action == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedInventory.equals(topInventory)) {
                            // Schedule a task to update the grave's inventory in the data manager
                            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                                    plugin.getDataManager().updateGrave(grave, "inventory",
                                            InventoryUtil.inventoryToString(grave.getInventory())), 1L);
                        }
                    }
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }


    /**
     * Handles inventory clicks when the player interacts with GraveList or GraveMenu inventories.
     *
     * @param event           The InventoryClickEvent.
     * @param player          The player interacting with the inventory.
     * @param inventoryHolder The inventory holder.
     */
    private void handlePlayerInventoryClick(InventoryClickEvent event, Player player, InventoryHolder inventoryHolder) {
        if (inventoryHolder instanceof GraveList) {
            handleGraveListClick(event, player, (GraveList) inventoryHolder);
        } else if (inventoryHolder instanceof GraveMenu) {
            handleGraveMenuClick(event, player, (GraveMenu) inventoryHolder);
        }
    }

    /**
     * Handles inventory clicks for GraveList inventories.
     *
     * @param event     The InventoryClickEvent.
     * @param player    The player interacting with the inventory.
     * @param graveList The GraveList inventory holder.
     */
    private void handleGraveListClick(InventoryClickEvent event, Player player, GraveList graveList) {
        Grave grave = graveList.getGrave(event.getSlot());

        if (grave != null) {
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                event.setCancelled(true); // Prevents items from being put in inventory
                return;
            }

            // Run function associated with the clicked slot in GraveList
            plugin.getEntityManager().runFunction(player, plugin.getConfig("gui.menu.list.function", grave)
                    .getString("gui.menu.list.function", "menu"), grave);
            plugin.getGUIManager().setGraveListItems(graveList.getInventory(), graveList.getUUID());
        }

        event.setCancelled(true);
    }

    /**
     * Handles inventory clicks for GraveMenu inventories.
     *
     * @param event     The InventoryClickEvent.
     * @param player    The player interacting with the inventory.
     * @param graveMenu The GraveMenu inventory holder.
     */
    private void handleGraveMenuClick(InventoryClickEvent event, Player player, GraveMenu graveMenu) {
        Grave grave = graveMenu.getGrave();
        try {
            if (grave != null) {
                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    event.setCancelled(true); // Prevents items from being put in inventory
                    return;
                }

                // Run function associated with the clicked slot in GraveMenu
                if (plugin.getConfig("gui.menu.grave.slot." + event.getSlot() + ".enabled", grave).getBoolean("gui.menu.grave.slot." + event.getSlot() + ".enabled")) {
                    plugin.getEntityManager().runFunction(player,
                            plugin.getConfig("gui.menu.grave.slot." + event.getSlot() + ".function", grave)
                                    .getString("gui.menu.grave.slot." + event.getSlot()
                                            + ".function", "none"), grave);
                    plugin.getGUIManager().setGraveMenuItems(graveMenu.getInventory(), grave);
                }
            }

            event.setCancelled(true);
        } catch (NullPointerException | IllegalArgumentException ignored) {
            // Likely grave doesn't exist. Ignore this.
            event.getWhoClicked().closeInventory();
            event.setCancelled(true);
        }
    }
}