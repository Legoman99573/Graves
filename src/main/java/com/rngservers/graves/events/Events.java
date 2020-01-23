package com.rngservers.graves.events;

import com.rngservers.graves.Main;
import com.rngservers.graves.grave.Grave;
import com.rngservers.graves.grave.GraveManager;
import com.rngservers.graves.grave.Messages;
import com.rngservers.graves.gui.GUIManager;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Iterator;
import java.util.List;

public class Events implements Listener {
    private Main plugin;
    private GraveManager graveManager;
    private GUIManager guiManager;
    private Messages messages;

    public Events(Main plugin, GraveManager chestManager, GUIManager guiManager, Messages messages) {
        this.plugin = plugin;
        this.graveManager = chestManager;
        this.guiManager = guiManager;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!event.getEntity().hasPermission("graves.place")) {
            return;
        }
        List<String> graveEntities = plugin.getConfig().getStringList("settings.graveEntities");
        if (graveEntities.contains(event.getEntity().getType().toString()) || graveEntities.contains("ALL")) {
            if (graveManager.getItemAmount(event.getEntity().getInventory()) > 0) {
                Grave grave = graveManager.createGrave(event.getEntity());
                if (grave != null) {
                    event.getDrops().clear();
                    event.getEntity().getInventory().clear();
                    if (event.getEntity().hasPermission("graves.experience")) {
                        Boolean expStore = plugin.getConfig().getBoolean("settings.expStore");
                        if (expStore) {
                            grave.setLevel(event.getEntity().getLevel());
                            event.setNewExp(0);
                            event.setNewTotalExp(0);
                            event.setDroppedExp(0);
                            event.setKeepLevel(false);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        List<String> graveEntities = plugin.getConfig().getStringList("settings.graveEntities");
        if (graveEntities.contains(event.getEntity().getType().toString()) || graveEntities.contains("ALL")) {
            if (event.getDrops().size() > 0) {
                Grave grave = graveManager.createGrave(event.getEntity(), event.getDrops());
                if (grave != null) {
                    event.getDrops().clear();
                    // TODO
                    /*
                    Boolean expStore = plugin.getConfig().getBoolean("settings.expStore");
                    if (expStore) {
                        //grave.setExperience(event.getDroppedExp());
                        event.setDroppedExp(0);
                    }
                     */
                }
            }
        }
    }

    @EventHandler
    public void onGraveOpen(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        if (event.getPlayer().hasPermission("graves.autoloot")) {
            if (event.getPlayer().isSneaking()) {
                return;
            }
        }
        Grave grave = graveManager.getGrave(event.getClickedBlock().getLocation());
        if (grave != null) {
            if (!event.getPlayer().hasPermission("graves.open")) {
                messages.permissionDenied(event.getPlayer());
                event.setCancelled(true);
                return;
            }
            if (graveManager.hasPermission(grave, event.getPlayer())) {
                event.getPlayer().openInventory(grave.getInventory());
                messages.graveOpen(grave.getLocation());
            } else {
                messages.graveProtected(event.getPlayer(), event.getClickedBlock().getLocation());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGraveSneakOpen(PlayerInteractEvent event) {
        if (!event.getPlayer().hasPermission("graves.autoloot")) {
            return;
        }
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        Grave grave = graveManager.getGrave(event.getClickedBlock().getLocation());
        if (grave != null) {
            if (graveManager.hasPermission(grave, event.getPlayer())) {
                graveManager.autoLoot(grave, event.getPlayer());
                messages.graveOpen(grave.getLocation());
            } else {
                messages.graveProtected(event.getPlayer(), grave.getLocation());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHologramOpen(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().hasPermission("graves.autoloot")) {
            if (event.getPlayer().isSneaking()) {
                return;
            }
        }
        if (event.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {
            Boolean hologramOpen = plugin.getConfig().getBoolean("settings.hologramOpen");
            if (hologramOpen) {
                ArmorStand armorStand = (ArmorStand) event.getRightClicked();
                Grave grave = graveManager.getGraveFromHologram(armorStand);
                if (grave != null) {
                    if (!event.getPlayer().hasPermission("graves.open")) {
                        messages.permissionDenied(event.getPlayer());
                        event.setCancelled(true);
                        return;
                    }
                    if (graveManager.hasPermission(grave, event.getPlayer())) {
                        event.getPlayer().openInventory(grave.getInventory());
                        messages.graveOpen(grave.getLocation());
                    } else {
                        messages.graveProtected(event.getPlayer(), grave.getLocation());
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onHologramSneakOpen(PlayerInteractAtEntityEvent event) {
        if (!event.getPlayer().hasPermission("graves.autoloot")) {
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        if (event.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {
            Boolean hologramOpen = plugin.getConfig().getBoolean("settings.hologramOpen");
            if (hologramOpen) {
                ArmorStand armorStand = (ArmorStand) event.getRightClicked();
                Grave grave = graveManager.getGraveFromHologram(armorStand);
                if (grave != null) {
                    if (graveManager.hasPermission(grave, event.getPlayer())) {
                        graveManager.autoLoot(grave, event.getPlayer());
                        messages.graveOpen(grave.getLocation());
                    } else {
                        messages.graveProtected(event.getPlayer(), grave.getLocation());
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onGraveClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Grave) {
            Grave grave = (Grave) event.getInventory().getHolder();
            messages.graveClose(grave.getLocation());
            if (grave.getItemAmount() == 0) {
                Player player = (Player) event.getPlayer();
                grave.getInventory().getViewers().remove(player);
                messages.graveLoot(grave.getLocation(), player);
                graveManager.giveExperience(grave, player);
                graveManager.removeHologram(grave);
                graveManager.replaceGrave(grave);
                graveManager.removeGrave(grave);
            }
        }
    }

    @EventHandler
    public void onGraveBreak(BlockBreakEvent event) {
        Grave grave = graveManager.getGrave(event.getBlock().getLocation());
        if (grave != null) {
            if (graveManager.hasPermission(grave, event.getPlayer())) {
                graveManager.dropGrave(grave);
                graveManager.dropExperience(grave);
                graveManager.removeHologram(grave);
                graveManager.replaceGrave(grave);
                graveManager.removeGrave(grave);
            } else {
                messages.graveProtected(event.getPlayer(), event.getBlock().getLocation());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onGraveBreakNaturally(BlockFromToEvent event) {
        Grave grave = graveManager.getGrave(event.getToBlock().getLocation());
        if (grave != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGraveExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Grave grave = graveManager.getGrave(block.getLocation());
            if (grave != null) {
                Boolean graveExplode = plugin.getConfig().getBoolean("settings.graveExplode");
                if (graveExplode) {
                    graveManager.dropGrave(grave);
                    graveManager.dropExperience(grave);
                    graveManager.removeHologram(grave);
                    graveManager.replaceGrave(grave);
                    graveManager.removeGrave(grave);
                } else {
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getView().getPlayer();
        if (title.startsWith("§5§3§1§6§r§0§r")) {
            if (event.getSlotType().equals(InventoryType.SlotType.CONTAINER)) {
                event.setCancelled(true);
                Boolean graveTeleport = plugin.getConfig().getBoolean("settings.graveTeleport");
                if (event.getCurrentItem() != null) {
                    if (event.getClick().equals(ClickType.RIGHT)) {
                        Boolean graveProtectedChange = plugin.getConfig().getBoolean("settings.graveProtectedChange");
                        if (graveProtectedChange) {
                            Grave grave = graveManager.getGrave(guiManager.getGraveLocation(event.getCurrentItem()));
                            Long diff = System.currentTimeMillis() - grave.getCreatedTime();
                            if (grave != null) {
                                if (grave.getProtectTime() == null || diff < grave.getProtectTime()) {
                                    if (grave.getProtected()) {
                                        graveManager.protectGrave(grave, false);
                                    } else {
                                        graveManager.protectGrave(grave, true);
                                    }
                                    graveManager.updateHologram(grave);
                                    guiManager.openGraveGUI(player, grave.getPlayer());
                                    return;
                                }
                            }
                        }
                    }
                    if (graveTeleport) {
                        if (player.hasPermission("graves.teleport")) {
                            guiManager.teleportGrave(player, event.getCurrentItem());
                        } else {
                            messages.permissionDenied(player);
                        }
                    } else {
                        if (player.hasPermission("graves.bypass")) {
                            guiManager.teleportGrave(player, event.getCurrentItem());
                        } else {
                            String graveTeleportDisabled = plugin.getConfig().getString("settings.graveTeleportDisabled")
                                    .replace("&", "§");
                            if (!graveTeleportDisabled.equals("")) {
                                player.sendMessage(graveTeleportDisabled);
                            }
                        }
                    }
                }
            }
        }
    }
}
