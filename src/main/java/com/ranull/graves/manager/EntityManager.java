package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilitySoundEnum;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.event.*;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import java.util.*;

/**
 * Manages the operations and lifecycle of entities within the Graves plugin.
 */
public final class EntityManager extends EntityDataManager {
    /**
     * The main plugin instance associated with Graves.
     * <p>
     * This {@link Graves} instance represents the core plugin that this Graves is part of. It provides access
     * to the plugin's functionality, configuration, and other services.
     * </p>
     */
    private final Graves plugin;

    /**
     * Initializes the EntityManager with the specified plugin instance.
     *
     * @param plugin the Graves plugin instance.
     */
    public EntityManager(Graves plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Swings the main hand of the specified player.
     *
     * @param player the player whose main hand to swing.
     */
    public void swingMainHand(Player player) {
        if (plugin.getVersionManager().hasSwingHand()) {
            player.swingMainHand();
        } else {
            ReflectionUtil.swingMainHand(player);
        }
    }

    /**
     * Creates a grave compass for the specified player, location, and grave.
     *
     * @param player   the player for whom the compass is created.
     * @param location the location to set on the compass.
     * @param grave    the grave associated with the compass.
     * @return the created compass item stack.
     */
    public ItemStack createGraveCompass(Player player, Location location, Grave grave) {
        GraveCompassAddEvent graveCompassAddEvent = new GraveCompassAddEvent(player, grave);
        plugin.getServer().getPluginManager().callEvent(graveCompassAddEvent);

        if (!graveCompassAddEvent.isCancelled() && !graveCompassAddEvent.isAddon()) {
            if (plugin.getVersionManager().hasPersistentData()) {
                Material material = Material.COMPASS;

                if (plugin.getConfig("compass.recovery", grave).getBoolean("compass.recovery")) {
                    try {
                        material = Material.valueOf("RECOVERY_COMPASS");
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                ItemStack itemStack = new ItemStack(material);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta != null) {
                    if (plugin.getVersionManager().hasCompassMeta() && itemMeta instanceof CompassMeta) {
                        CompassMeta compassMeta = (CompassMeta) itemMeta;

                        compassMeta.setLodestoneTracked(false);
                        compassMeta.setLodestone(location);
                    } else if (itemStack.getType().name().equals("RECOVERY_COMPASS")) {
                        try {
                            player.setLastDeathLocation(location);
                        } catch (NoSuchMethodError ignored) {
                        }
                    }

                    List<String> loreList = new ArrayList<>();
                    int customModelData = plugin.getConfig("compass.model-data", grave).getInt("compass.model-data", -1);

                    if (customModelData > -1) {
                        itemMeta.setCustomModelData(customModelData);
                    }

                    if (plugin.getConfig("compass.glow", grave).getBoolean("compass.glow")) {
                        Enchantment enchantment = plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");
                        itemMeta.addEnchant(enchantment, 1, true);
                        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    if (plugin.getIntegrationManager().hasMiniMessage()) {
                        String compass_name = StringUtil.parseString("&f" + plugin
                                .getConfig("compass.name", grave).getString("compass.name"), grave, plugin);
                        itemMeta.setDisplayName(MiniMessage.parseString(compass_name));
                        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "graveUUID"),
                                PersistentDataType.STRING, grave.getUUID().toString());

                        for (String string : plugin.getConfig("compass.lore", grave).getStringList("compass.lore")) {
                            String compass_lore = StringUtil.parseString("&7" + string, location, grave, plugin);
                            loreList.add(MiniMessage.parseString(compass_lore));
                        }
                    } else {
                        itemMeta.setDisplayName(ChatColor.WHITE + StringUtil.parseString(plugin
                                .getConfig("compass.name", grave).getString("compass.name"), grave, plugin));
                        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "graveUUID"),
                                PersistentDataType.STRING, grave.getUUID().toString());

                        for (String string : plugin.getConfig("compass.lore", grave).getStringList("compass.lore")) {
                            loreList.add(ChatColor.GRAY + StringUtil.parseString(string, location, grave, plugin));
                        }
                    }

                    itemMeta.setLore(loreList);
                    itemStack.setItemMeta(itemMeta);
                }

                return itemStack;
            }
        }

        return null;
    }

    /**
     * Retrieves a map of compasses and their associated UUIDs from a player's inventory.
     *
     * @param player the player whose inventory to check.
     * @return a map of compasses and their associated UUIDs.
     */
    public Map<ItemStack, UUID> getCompassesFromInventory(HumanEntity player) {
        Map<ItemStack, UUID> itemStackUUIDMap = new HashMap<>();

        if (plugin.getVersionManager().hasPersistentData()) {
            for (ItemStack itemStack : player.getInventory().getContents()) {
                UUID uuid = getGraveUUIDFromItemStack(itemStack);

                if (uuid != null) {
                    itemStackUUIDMap.put(itemStack, uuid);
                }
            }
        }

        return itemStackUUIDMap;
    }

    /**
     * Retrieves the grave UUID from an item stack.
     *
     * @param itemStack the item stack to check.
     * @return the grave UUID, or null if not found.
     */
    public UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
        if (plugin.getVersionManager().hasPersistentData() && itemStack != null && itemStack.getItemMeta() != null) {
            if (itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "graveUUID"),
                    PersistentDataType.STRING)) {
                return UUIDUtil.getUUID(itemStack.getItemMeta().getPersistentDataContainer()
                        .get(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING));
            }
        }

        return null;
    }

    public void teleportEntity(Entity entity, Location location, Grave grave) {
        location = LocationUtil.roundLocation(location);
        BlockFace blockFace = BlockFaceUtil.getYawBlockFace(grave.getYaw());
        Location locationTeleport = location.clone().getBlock().getRelative(blockFace).getRelative(blockFace)
                .getLocation().add(0.5, 0, 0.5);

        if (plugin.getLocationManager().isLocationSafePlayer(locationTeleport)) {
            locationTeleport.setYaw(BlockFaceUtil.getBlockFaceYaw(blockFace.getOppositeFace()));
            locationTeleport.setPitch(20);
        } else {
            locationTeleport = plugin.getLocationManager()
                    .getSafeTeleportLocation(entity, location.add(0, 1, 0), grave, plugin);

            if (locationTeleport != null) {
                locationTeleport.add(0.5, 0, 0.5);
                locationTeleport.setYaw(BlockFaceUtil.getBlockFaceYaw(blockFace));
                locationTeleport.setPitch(90);
            }
        }

        final long delayTicks = plugin.getConfig("teleport.delay", grave).getLong("teleport.delay");

        if (locationTeleport != null && locationTeleport.getWorld() != null) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                final Location initialLocation = player.getLocation();

                if (plugin.getIntegrationManager().hasVault() && plugin.getIntegrationManager().hasVaultEconomy()) {
                    double teleportCost = getTeleportCost(entity.getLocation(), locationTeleport, grave);
                    if (plugin.getIntegrationManager().getVault().hasBalance(player, teleportCost)) {
                        GraveTeleportEvent graveTeleportEvent = new GraveTeleportEvent(grave, entity);

                        plugin.getServer().getPluginManager().callEvent(graveTeleportEvent);
                        if (!graveTeleportEvent.isCancelled() && !graveTeleportEvent.isAddon()) {
                            if (!plugin.hasGrantedPermission("graves.teleport.delay-bypass", player.getPlayer()) && delayTicks > 0) {
                                Location finalLocationTeleport1 = locationTeleport;
                                BossBar bossBar;
                                if (plugin.getIntegrationManager().hasMiniMessage()) {
                                    String legacyBossBar = StringUtil.parseString(plugin.getConfig("message.teleport-waiting", grave)
                                            .getString("message.teleport-waiting"), location, grave, plugin);
                                    bossBar = plugin.getServer().createBossBar(MiniMessage.parseString(legacyBossBar), BarColor.RED, BarStyle.SOLID);
                                } else {
                                    bossBar = plugin.getServer().createBossBar(StringUtil.parseString(plugin.getConfig("message.teleport-waiting", grave)
                                            .getString("message.teleport-waiting"), location, grave, plugin), BarColor.RED, BarStyle.SOLID);
                                }
                                bossBar.addPlayer(player);

                                new BukkitRunnable() {
                                    int ticksRemaining = (int) delayTicks;

                                    @Override
                                    public void run() {
                                        Location currentLocation = player.getLocation();

                                        // Check if player has moved (excluding yaw/pitch unless teleport.strict is true)
                                        boolean hasMoved = plugin.getConfig().getBoolean("teleport.strict")
                                                ? !currentLocation.equals(initialLocation)  // Compare full location if strict
                                                : !currentLocation.getBlock().equals(initialLocation.getBlock());  // Compare block location only

                                        if (!player.isOnline() || hasMoved) {
                                            bossBar.removeAll();
                                            plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                                            this.cancel();
                                            return;
                                        }

                                        if (ticksRemaining > 0) {
                                            double progress = (double) ticksRemaining / delayTicks;
                                            bossBar.setProgress(Math.max(0, Math.min(1, progress)));  // Clamp the progress between 0 and 1
                                            ticksRemaining--;
                                        } else {
                                            if (plugin.getIntegrationManager().getVault().hasBalance(player, teleportCost)
                                                    && plugin.getIntegrationManager().getVault().withdrawBalance(player, teleportCost)) {
                                                player.teleport(finalLocationTeleport1);
                                                plugin.getEntityManager().sendMessage("message.teleport", player, finalLocationTeleport1, grave);
                                                plugin.getEntityManager().playPlayerSound("sound.teleport", player, finalLocationTeleport1, grave);
                                            } else {
                                                plugin.getEntityManager().sendMessage("message.no-money", player, player.getLocation(), grave);
                                            }
                                            bossBar.removeAll();
                                            this.cancel();
                                        }
                                    }
                                }.runTaskTimer(plugin, 0, 20L);  // Runs every second (20 ticks)
                            } else {
                                // Immediate teleportation, no delay
                                if (player.isOnline() && player.getLocation().equals(initialLocation)) {
                                    if (plugin.getIntegrationManager().getVault().hasBalance(player, teleportCost)
                                            && plugin.getIntegrationManager().getVault().withdrawBalance(player, teleportCost)) {
                                        player.teleport(locationTeleport);
                                        plugin.getEntityManager().sendMessage("message.teleport", player, locationTeleport, grave);
                                        plugin.getEntityManager().playPlayerSound("sound.teleport", player, locationTeleport, grave);
                                    } else {
                                        plugin.getEntityManager().sendMessage("message.no-money", player, player.getLocation(), grave);
                                    }
                                } else {
                                    plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                                }
                            }
                        }
                    } else {
                        plugin.getEntityManager().sendMessage("message.no-money", player, player.getLocation(), grave);
                    }
                } else {
                    GraveTeleportEvent graveTeleportEvent = new GraveTeleportEvent(grave, entity);
                    plugin.getServer().getPluginManager().callEvent(graveTeleportEvent);
                    if (!graveTeleportEvent.isCancelled() && !graveTeleportEvent.isAddon()) {
                        if (!plugin.hasGrantedPermission("graves.teleport.delay-bypass", player.getPlayer()) && delayTicks > 0) {
                            Location finalLocationTeleport = locationTeleport;
                            BossBar bossBar;
                            if (plugin.getIntegrationManager().hasMiniMessage()) {
                                String legacyBossBar = StringUtil.parseString(plugin.getConfig("message.teleport-waiting", grave)
                                        .getString("message.teleport-waiting"), location, grave, plugin);
                                bossBar = plugin.getServer().createBossBar(MiniMessage.parseString(legacyBossBar), BarColor.RED, BarStyle.SOLID);
                            } else {
                                bossBar = plugin.getServer().createBossBar(StringUtil.parseString(plugin.getConfig("message.teleport-waiting", grave)
                                        .getString("message.teleport-waiting"), location, grave, plugin), BarColor.RED, BarStyle.SOLID);
                            }
                            bossBar.addPlayer(player);

                            new BukkitRunnable() {
                                int ticksRemaining = (int) delayTicks;

                                @Override
                                public void run() {
                                    Location currentLocation = player.getLocation();

                                    // Check if player has moved (excluding yaw/pitch unless teleport.strict is true)
                                    boolean hasMoved = plugin.getConfig().getBoolean("teleport.strict")
                                            ? !currentLocation.equals(initialLocation)  // Compare full location if strict
                                            : !currentLocation.getBlock().equals(initialLocation.getBlock());  // Compare block location only

                                    if (!player.isOnline() || hasMoved) {
                                        bossBar.removeAll();
                                        plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                                        this.cancel();
                                        return;
                                    }

                                    if (ticksRemaining > 0) {
                                        double progress = (double) ticksRemaining / delayTicks;
                                        bossBar.setProgress(Math.max(0, Math.min(1, progress)));  // Clamp the progress between 0 and 1
                                        ticksRemaining--;
                                    } else {
                                        player.teleport(finalLocationTeleport);
                                        plugin.getEntityManager().sendMessage("message.teleport", player, finalLocationTeleport, grave);
                                        plugin.getEntityManager().playPlayerSound("sound.teleport", player, finalLocationTeleport, grave);
                                        bossBar.removeAll();
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 0, 20L);  // Runs every second (20 ticks)
                        } else {
                            if (player.isOnline() && player.getLocation().equals(initialLocation)) {
                                player.teleport(locationTeleport);
                                plugin.getEntityManager().sendMessage("message.teleport", player, locationTeleport, grave);
                            } else {
                                plugin.getEntityManager().sendMessage("message.teleport-cancelled", player, player.getLocation(), grave);
                            }
                        }
                    }
                }
            } else {
                GraveTeleportEvent graveTeleportEvent = new GraveTeleportEvent(grave, entity);

                plugin.getServer().getPluginManager().callEvent(graveTeleportEvent);
                if (!graveTeleportEvent.isCancelled() && !graveTeleportEvent.isAddon()) {
                    if (delayTicks > 0) {
                        Location finalLocationTeleport2 = locationTeleport;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (entity.isValid()) {
                                    entity.teleport(finalLocationTeleport2);
                                    plugin.getEntityManager().sendMessage("message.teleport", entity, entity.getLocation(), grave);
                                }
                            }
                        }.runTaskLater(plugin, delayTicks * 20L);
                    } else {
                        if (entity.isValid()) {
                            entity.teleport(locationTeleport);
                            plugin.getEntityManager().sendMessage("message.teleport", entity, entity.getLocation(), grave);
                        }
                    }
                }
            }
        } else {
            plugin.getEntityManager().sendMessage("message.teleport-failure", entity, location, grave);
        }
    }

    /**
     * Calculates the cost of teleportation between two locations.
     *
     * @param location1 the starting location.
     * @param location2 the destination location.
     * @param grave     the grave associated with the teleportation.
     * @return the teleportation cost.
     */
    public double getTeleportCost(Location location1, Location location2, Grave grave) {
        double cost = plugin.getConfig("teleport.cost", grave).getDouble("teleport.cost");

        if (plugin.getConfig("teleport.cost", grave).isString("teleport.cost")) {
            String costString = StringUtil.parseString(plugin.getConfig("teleport.cost", grave)
                    .getString("teleport.cost"), location2, grave, plugin);

            try {
                cost = Double.parseDouble(costString);
            } catch (NumberFormatException ignored) {
                plugin.debugMessage(costString + " cost is not a double", 1);
            }
        }

        double costDifferentWorld = plugin.getConfig("teleport.cost-different-world", grave)
                .getDouble("teleport.cost-different-world");

        if (plugin.getConfig("teleport.cost-distance-increase", grave)
                .getBoolean("teleport.cost-distance-increase")) {
            double distance = Math.sqrt(NumberConversions.square(location1.getBlockX() - location2.getBlockX())
                    + NumberConversions.square(location1.getBlockZ() - location2.getBlockZ()));
            cost = Math.round(cost * (distance / 16));
        }

        if (location1.getWorld() != null && location2.getWorld() != null && costDifferentWorld > 0
                && !location1.getWorld().getName().equals(location2.getWorld().getName())) {
            cost += costDifferentWorld;
        }
        return cost;
    }

    /**
     * Plays a world sound at the player's location.
     *
     * @param string the sound identifier.
     * @param player the player whose location to play the sound at.
     */
    public void playWorldSound(String string, Player player) {
        playWorldSound(string, player.getLocation(), null);
    }

    /**
     * Plays a world sound at the player's location, associated with a grave.
     *
     * @param string the sound identifier.
     * @param player the player whose location to play the sound at.
     * @param grave  the grave associated with the sound.
     */
    public void playWorldSound(String string, Player player, Grave grave) {
        playWorldSound(string, player.getLocation(), grave);
    }

    /**
     * Plays a world sound at a specified location, associated with a grave.
     *
     * @param string   the sound identifier.
     * @param location the location to play the sound at.
     * @param grave    the grave associated with the sound.
     */
    public void playWorldSound(String string, Location location, Grave grave) {
        playWorldSound(string, location, grave != null ? grave.getOwnerType() : null, grave != null
                ? grave.getPermissionList() : null, 1, 1);
    }

    /**
     * Plays a world sound at a specified location with additional parameters.
     *
     * @param string         the sound identifier.
     * @param location       the location to play the sound at.
     * @param entityType     the type of entity associated with the sound.
     * @param permissionList the list of permissions associated with the sound.
     * @param volume         the volume of the sound.
     * @param pitch          the pitch of the sound.
     */
    public void playWorldSound(String string, Location location, EntityType entityType, List<String> permissionList,
                               float volume, float pitch) {
        if (location.getWorld() != null) {
            string = plugin.getConfig(string, entityType, permissionList).getString(string);

            if (string != null && !string.equals("")) {
                try {
                    location.getWorld().playSound(location, Objects.requireNonNull(CompatibilitySoundEnum.valueOf(string.toUpperCase())), volume, pitch);
                } catch (IllegalArgumentException exception) {
                    plugin.debugMessage(string.toUpperCase() + " is not a Sound ENUM", 1);
                }
            }
        }
    }

    /**
     * Plays a player-specific sound at the entity's location.
     *
     * @param string the sound identifier.
     * @param entity the entity to play the sound for.
     * @param grave  the grave associated with the sound.
     */
    public void playPlayerSound(String string, Entity entity, Grave grave) {
        playPlayerSound(string, entity, entity.getLocation(), grave.getPermissionList(), 1, 1);
    }

    /**
     * Plays a player-specific sound at a specified location for an entity.
     *
     * @param string   the sound identifier.
     * @param entity   the entity to play the sound for.
     * @param location the location to play the sound at.
     * @param grave    the grave associated with the sound.
     */
    public void playPlayerSound(String string, Entity entity, Location location, Grave grave) {
        playPlayerSound(string, entity, location, grave.getPermissionList(), 1, 1);
    }

    /**
     * Plays a player-specific sound at the entity's location with a permission list.
     *
     * @param string         the sound identifier.
     * @param entity         the entity to play the sound for.
     * @param permissionList the list of permissions associated with the sound.
     */
    public void playPlayerSound(String string, Entity entity, List<String> permissionList) {
        playPlayerSound(string, entity, entity.getLocation(), permissionList, 1, 1);
    }

    /**
     * Plays a player-specific sound at a specified location with a permission list.
     *
     * @param string         the sound identifier.
     * @param entity         the entity to play the sound for.
     * @param location       the location to play the sound at.
     * @param permissionList the list of permissions associated with the sound.
     */
    public void playPlayerSound(String string, Entity entity, Location location, List<String> permissionList) {
        playPlayerSound(string, entity, location, permissionList, 1, 1);
    }

    /**
     * Plays a player-specific sound at a specified location with additional parameters.
     *
     * @param string         the sound identifier.
     * @param entity         the entity to play the sound for.
     * @param location       the location to play the sound at.
     * @param permissionList the list of permissions associated with the sound.
     * @param volume         the volume of the sound.
     * @param pitch          the pitch of the sound.
     */
    public void playPlayerSound(String string, Entity entity, Location location, List<String> permissionList,
                                float volume, float pitch) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            string = plugin.getConfig(string, entity, permissionList).getString(string);

            if (string != null && !string.equals("")) {
                try {
                    player.playSound(location, Objects.requireNonNull(CompatibilitySoundEnum.valueOf(string.toUpperCase())), volume, pitch);
                } catch (IllegalArgumentException exception) {
                    plugin.debugMessage(string.toUpperCase() + " is not a Sound ENUM", 1);
                }
            }
        }
    }

    /**
     * Sends a message to a command sender.
     *
     * @param string        the message identifier.
     * @param commandSender the command sender to send the message to.
     */
    public void sendMessage(String string, CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            sendMessage(string, player, player.getLocation(), null, plugin.getPermissionList(player));
        }
    }

    /**
     * Sends a message to an entity.
     *
     * @param string the message identifier.
     * @param entity the entity to send the message to.
     */
    public void sendMessage(String string, Entity entity) {
        sendMessage(string, entity, entity.getLocation(), null, plugin.getPermissionList(entity));
    }

    /**
     * Sends a message to an entity with a permission list.
     *
     * @param string         the message identifier.
     * @param entity         the entity to send the message to.
     * @param permissionList the list of permissions associated with the message.
     */
    public void sendMessage(String string, Entity entity, List<String> permissionList) {
        sendMessage(string, entity, entity.getLocation(), null, permissionList);
    }

    /**
     * Sends a message to an entity at a specified location with a permission list.
     *
     * @param string         the message identifier.
     * @param entity         the entity to send the message to.
     * @param location       the location associated with the message.
     * @param permissionList the list of permissions associated with the message.
     */
    public void sendMessage(String string, Entity entity, Location location, List<String> permissionList) {
        sendMessage(string, entity, location, null, permissionList);
    }

    /**
     * Sends a message to an entity at a specified location associated with a grave.
     *
     * @param string   the message identifier.
     * @param entity   the entity to send the message to.
     * @param location the location associated with the message.
     * @param grave    the grave associated with the message.
     */
    public void sendMessage(String string, Entity entity, Location location, Grave grave) {
        sendMessage(string, entity, location, grave, null);
    }

    /**
     * Sends a message to an entity with a custom name and a permission list.
     *
     * @param string         the message identifier.
     * @param entity         the entity to send the message to.
     * @param name           the custom name associated with the message.
     * @param location       the location associated with the message.
     * @param permissionList the list of permissions associated with the message.
     */
    public void sendMessage(String string, Entity entity, String name, Location location, List<String> permissionList) {
        sendMessage(string, entity, name, location, null, permissionList);
    }

    private void sendMessage(String string, Entity entity, Location location, Grave grave, List<String> permissionList) {
        sendMessage(string, entity, getEntityName(entity), location, grave, permissionList);
    }

    private void sendMessage(String string, Entity entity, String name, Location location, Grave grave, List<String> permissionList) {
        if (entity instanceof Player) {
            Player player = (Player) entity;

            String originalConfigString = string;
            if (grave != null) {
                string = plugin.getConfig(string, grave).getString(string);
            } else {
                string = plugin.getConfig(string, entity.getType(), permissionList).getString(string);
            }

            String prefix = plugin.getConfig("message.prefix", entity.getType(), permissionList)
                    .getString("message.prefix");

            // Check if the original string is empty before applying the prefix
            if (string != null && !string.isEmpty()) {
                if (prefix != null && !prefix.equals("")) {
                    if (plugin.getIntegrationManager().hasMiniMessage()) {
                        string = prefix + "<white>" + string;
                    } else {
                        string = prefix + string;
                    }
                } else {
                    if (plugin.getIntegrationManager().hasMiniMessage()) {
                        string = "<white>" + string;
                    } else {
                        string = "&r" + string;
                    }
                }

                String message = StringUtil.parseString(string, entity, name, location, grave, plugin);
                plugin.debugMessage("Message found for " + string + " in grave.yml. Sending message to " + entity.getName() + ".", 2);
                if (plugin.getIntegrationManager().hasMiniMessage()) {
                    MiniMessage.sendMessage(player, message);
                } else {
                    player.sendMessage(message); // assuming the server doesn't support MiniMessage or integration is disabled.
                }
            } else {
                plugin.debugMessage("Original string " + originalConfigString + " is empty, no message sent.", 2);
            }
        }
    }

    /**
     * Runs commands associated with an entity, location, and grave.
     *
     * @param string   the command identifier.
     * @param entity   the entity associated with the command.
     * @param location the location associated with the command.
     * @param grave    the grave associated with the command.
     */
    public void runCommands(String string, Entity entity, Location location, Grave grave) {
        runCommands(string, entity, null, location, grave);
    }

    /**
     * Runs commands associated with a name, location, and grave.
     *
     * @param string   the command identifier.
     * @param name     the name associated with the command.
     * @param location the location associated with the command.
     * @param grave    the grave associated with the command.
     */
    public void runCommands(String string, String name, Location location, Grave grave) {
        runCommands(string, null, name, location, grave);
    }

    private void runCommands(String string, Entity entity, String name, Location location, Grave grave) {
        for (String command : plugin.getConfig(string, grave).getStringList(string)) {
            if (command != null && !command.equals("")) {
                runConsoleCommand(StringUtil.parseString(command, entity, name, location, grave, plugin));
            }
        }
    }

    private void runConsoleCommand(String string) {
        if (string != null && !string.equals("")) {
            ServerCommandEvent serverCommandEvent = new ServerCommandEvent(plugin.getServer().getConsoleSender(), string);

            plugin.getServer().getPluginManager().callEvent(serverCommandEvent);

            if ((plugin.getVersionManager().is_v1_7() || plugin.getVersionManager().is_v1_8())
                    || !serverCommandEvent.isCancelled()) {
                plugin.getServer().getScheduler().callSyncMethod(plugin, () -> plugin.getServer()
                        .dispatchCommand(serverCommandEvent.getSender(), serverCommandEvent.getCommand()));
                plugin.debugMessage("Running console command " + string, 1);
            }
        }
    }

    /**
     * Runs a function associated with an entity and a specified function name.
     *
     * @param entity   the entity to run the function for.
     * @param function the name of the function to run.
     * @return true if the function was run successfully, false otherwise.
     */
    public boolean runFunction(Entity entity, String function) {
        return runFunction(entity, function, null);
    }

    /**
     * Runs a function associated with an entity, a specified function name, and a grave.
     *
     * @param entity   the entity to run the function for.
     * @param function the name of the function to run.
     * @param grave    the grave associated with the function.
     * @return true if the function was run successfully, false otherwise.
     */
    public boolean runFunction(Entity entity, String function, Grave grave) {
        switch (function.toLowerCase()) {
            case "list": {
                plugin.getGUIManager().openGraveList(entity);
                return true;
            }
            case "menu": {
                plugin.getGUIManager().openGraveMenu(entity, grave);
                return true;
            }
            case "teleport":
            case "teleportation": {
                if (plugin.getConfig("teleport.enabled", grave).getBoolean("teleport.enabled")
                        && (plugin.hasGrantedPermission("graves.teleport", ((Player) entity).getPlayer())
                        || plugin.getConfig("teleport.enabled", grave).getBoolean("teleport.enabled")
                        && plugin.hasGrantedPermission("graves.teleport.world." + grave.getLocationDeath().getWorld().getName(), ((Player) entity).getPlayer())
                        || plugin.hasGrantedPermission("graves.bypass", ((Player) entity).getPlayer()))
                        || plugin.hasGrantedPermission("graves.teleport.bypass", ((Player) entity).getPlayer())) {
                    if (plugin.hasGrantedPermission("graves.bypass", ((Player) entity).getPlayer()) && grave.getOwnerUUID() != entity.getUniqueId()
                        || plugin.hasGrantedPermission("graves.teleport.bypass", ((Player) entity).getPlayer()) && grave.getOwnerUUID() != entity.getUniqueId()) {
                        GraveTeleportEvent graveTeleportEvent = new GraveTeleportEvent(grave, entity);

                        plugin.getServer().getPluginManager().callEvent(graveTeleportEvent);
                        if (!graveTeleportEvent.isCancelled()) {
                            entity.teleport(plugin.getGraveManager().getGraveLocation(grave.getLocationDeath().add(1, 0, 1), grave));
                        }
                    } else {
                        GraveTeleportEvent graveTeleportEvent = new GraveTeleportEvent(grave, entity);

                        plugin.getServer().getPluginManager().callEvent(graveTeleportEvent);
                        if (!graveTeleportEvent.isCancelled()) {
                            plugin.getEntityManager().teleportEntity(entity, plugin.getGraveManager()
                                    .getGraveLocationList(entity.getLocation(), grave).get(0), grave);
                        }
                    }
                } else {
                    plugin.getEntityManager().sendMessage("message.teleport-disabled", entity,
                            entity.getLocation(), grave);
                }
                return true;
            }
            case "protect":
            case "protection": {
                if (grave.getTimeProtectionRemaining() > 0 || grave.getTimeProtectionRemaining() < 0) {
                    plugin.getGraveManager().toggleGraveProtection(grave);
                    playPlayerSound("sound.protection-change", entity, grave);
                    plugin.getGUIManager().openGraveMenu(entity, grave, false);
                }
                return true;
            }
            case "abandoned": {
                Location location = plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
                if (location != null) {
                    playPlayerSound("sound.abandoned", entity, grave);
                    plugin.getEntityManager().sendMessage("message.abandoned", entity, location, grave);
                    plugin.getGraveManager().abandonGrave(grave);
                }
                break;
            }
            case "distance": {
                GraveCompassUseEvent graveCompassUseEvent = new GraveCompassUseEvent(entity instanceof Player ? (Player) entity : null, grave);
                plugin.getServer().getPluginManager().callEvent(graveCompassUseEvent);

                if (!graveCompassUseEvent.isCancelled()) {
                    Location location = plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
                    if (location != null) {
                        if (entity.getWorld().equals(location.getWorld())) {
                            plugin.getEntityManager().sendMessage("message.distance", entity, location, grave);
                        } else {
                            plugin.getEntityManager().sendMessage("message.distance-world", entity, location, grave);
                        }
                    }
                }
                return true;
            }
            case "open":
            case "loot":
            case "virtual": {
                if (entity.getLocation().getWorld() == grave.getLocationDeath().getWorld()) {
                    double distance = plugin.getConfig("virtual.distance", grave).getDouble("virtual.distance");
                    if (distance < 0) {
                        plugin.getGraveManager().openGrave(entity, entity.getLocation(), grave);
                    } else {
                        Location location = plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
                        if (location != null && entity.getLocation().getWorld() == grave.getLocationDeath().getWorld()) {
                            if (entity.getLocation().distance(location) <= distance) {
                                plugin.getGraveManager().openGrave(entity, entity.getLocation(), grave);
                            } else {
                                plugin.getEntityManager().sendMessage("message.distance-virtual", entity, location, grave);
                            }
                        }
                    }
                } else {
                    plugin.getEntityManager().sendMessage("message.distance-virtual", entity, entity.getLocation(), grave);
                }
                return true;
            }
            case "autoloot": {
                GraveAutoLootEvent graveAutoLootEvent = new GraveAutoLootEvent(entity, entity.getLocation(), grave);

                plugin.getServer().getPluginManager().callEvent(graveAutoLootEvent);
                if (!graveAutoLootEvent.isCancelled()) {
                    plugin.getGraveManager().autoLootGrave(entity, entity.getLocation(), grave);
                    if (plugin.getIntegrationManager().hasNoteBlockAPI()) {
                        Player player = (Player) entity;
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
                        }
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                        }
                    }
                }
                return true;
            }
            case "particle":
            case "particles": {
                if (plugin.getConfig("compass.particles.enabled", grave).getBoolean("compass.particles.enabled")) {
                    GraveParticleEvent graveParticleEvent = new GraveParticleEvent(entity instanceof Player ? (Player) entity : null, grave);

                    plugin.getServer().getPluginManager().callEvent(graveParticleEvent);
                    if (!graveParticleEvent.isCancelled()) {
                        Location location = plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);

                        if (location != null && entity.getLocation().getWorld() == grave.getLocationDeath().getWorld()) {
                            plugin.getParticleManager().startParticleTrail(entity.getLocation(), grave.getLocationDeath(), Particle.valueOf(Objects.requireNonNull(plugin.getConfig("compass.particles.particle", grave).getString("compass.particles.particle")).toUpperCase()), plugin.getConfig("compass.particles.count", grave).getInt("compass.particles.count", 5), plugin.getConfig("compass.particles.speed", grave).getDouble("compass.particles.speed", 0.3), plugin.getConfig("compass.particles.duration", grave).getInt("compass.particles.duration"), entity.getUniqueId());
                        }
                    }
                }
                return true;
            }
            case "preview":
            case "sneekpeak": {
                Location location = plugin.getGraveManager().getGraveLocation(entity.getLocation(), grave);
                if (location != null && entity.getLocation().getWorld() == grave.getLocationDeath().getWorld()) {
                    if (plugin.getConfig("grave.preview", grave).getBoolean("grave.preview")) {
                        plugin.getGraveManager().openGrave(entity, entity.getLocation(), grave, true);
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a player can open a specified grave.
     *
     * @param player the player attempting to open the grave.
     * @param grave  the grave to check.
     * @return true if the player can open the grave, false otherwise.
     */
    public boolean canOpenGrave(Player player, Grave grave) {
        if (grave.getTimeProtectionRemaining() == 0 || plugin.hasGrantedPermission("graves.bypass", player.getPlayer())) {
            return true;
        } else if (grave.getProtection() && grave.getOwnerUUID() != null) {
            if (grave.getOwnerUUID().equals(player.getUniqueId())
                    && plugin.getConfig("protection.open.owner", grave)
                    .getBoolean("protection.open.owner")) {
                return true;
            } else {
                if (grave.getKillerUUID() != null) {
                    if (grave.getKillerUUID().equals(player.getUniqueId())
                            && plugin.getConfig("protection.open.killer", grave)
                            .getBoolean("protection.open.killer")) {
                        return true;
                    } else return !grave.getOwnerUUID().equals(player.getUniqueId())
                            && !grave.getKillerUUID().equals(player.getUniqueId())
                            && plugin.getConfig("protection.open.other", grave)
                            .getBoolean("protection.open.other");
                } else return (grave.getOwnerUUID().equals(player.getUniqueId())
                        && plugin.getConfig("protection.open.missing.owner", grave)
                        .getBoolean("protection.open.missing.owner"))
                        || (!grave.getOwnerUUID().equals(player.getUniqueId())
                        && plugin.getConfig("protection.open.missing.other", grave)
                        .getBoolean("protection.open.missing.other"));
            }
        } else {
            return true;
        }
    }

    /**
     * Spawns a zombie at a specified location, targeting a specified entity, and associated with a grave.
     *
     * @param location     the location to spawn the zombie.
     * @param entity       the entity associated with the zombie spawn.
     * @param targetEntity the entity to be targeted by the zombie.
     * @param grave        the grave associated with the zombie spawn.
     */
    public void spawnZombie(Location location, Entity entity, LivingEntity targetEntity, Grave grave) {
        if ((plugin.getConfig("zombie.spawn-owner", grave).getBoolean("zombie.spawn-owner")
                && grave.getOwnerUUID().equals(entity.getUniqueId())
                || plugin.getConfig("zombie.spawn-other", grave).getBoolean("zombie.spawn-other")
                && !grave.getOwnerUUID().equals(entity.getUniqueId()))) {
            spawnZombie(location, targetEntity, grave);
        }
    }

    /**
     * Spawns a zombie at a specified location, associated with a grave.
     *
     * @param location the location to spawn the zombie.
     * @param grave    the grave associated with the zombie spawn.
     */
    public void spawnZombie(Location location, Grave grave) {
        spawnZombie(location, null, grave);
    }

    @SuppressWarnings("deprecation")
    private void spawnZombie(Location location, LivingEntity targetEntity, Grave grave) {
        if (location != null && location.getWorld() != null && grave.getOwnerType() == EntityType.PLAYER) {
            GraveZombieSpawnEvent graveZombieSpawnEvent = new GraveZombieSpawnEvent(location, targetEntity, grave);

            plugin.getServer().getPluginManager().callEvent(graveZombieSpawnEvent);
            if (graveZombieSpawnEvent.isCancelled() || graveZombieSpawnEvent.isAddon()) return;
            String zombieType = plugin.getConfig("zombie.type", grave)
                    .getString("zombie.type", "ZOMBIE").toUpperCase();
            EntityType entityType = EntityType.ZOMBIE;

            try {
                entityType = EntityType.valueOf(zombieType);
            } catch (IllegalArgumentException exception) {
                plugin.debugMessage(zombieType + " is not a EntityType ENUM", 1);
            }

            if (entityType.name().equals("ZOMBIE") && MaterialUtil.isWater(location.getBlock().getType())) {
                try {
                    entityType = EntityType.valueOf("DROWNED");
                } catch (IllegalArgumentException ignored) {
                }
            }

            Entity entity = location.getWorld().spawnEntity(location, entityType);

            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                if (livingEntity.getEquipment() != null) {
                    if (plugin.getConfig("zombie.owner-head", grave).getBoolean("zombie.owner-head")) {
                        livingEntity.getEquipment().setHelmet(plugin.getCompatibility().getSkullItemStack(grave, plugin));
                    }

                    livingEntity.getEquipment().setChestplate(null);
                    livingEntity.getEquipment().setLeggings(null);
                    livingEntity.getEquipment().setBoots(null);
                }

                livingEntity.setMetadata("GravesX", new FixedMetadataValue(plugin, true)); // So we don't break other plugins

                double zombieHealth = plugin.getConfig("zombie.health", grave).getDouble("zombie.health");

                if (zombieHealth >= 0.5) {
                    livingEntity.setMaxHealth(zombieHealth);
                    livingEntity.setHealth(zombieHealth);
                }

                if (!plugin.getConfig("zombie.pickup", grave).getBoolean("zombie.pickup")) {
                    livingEntity.setCanPickupItems(false);
                }

                String zombieName = StringUtil.parseString(plugin.getConfig("zombie.name", grave)
                        .getString("zombie.name"), location, grave, plugin);

                if (!zombieName.equals("")) {
                    if (plugin.getIntegrationManager().hasMiniMessage()) {
                        livingEntity.setCustomName(MiniMessage.parseString(zombieName));
                    } else {
                        livingEntity.setCustomName(zombieName);
                    }

                }

                setDataByte(livingEntity, "graveZombie");
                setDataString(livingEntity, "graveUUID", grave.getUUID().toString());
                setDataString(livingEntity, "graveEntityType", grave.getOwnerType().name());

                if (grave.getPermissionList() != null && !grave.getPermissionList().isEmpty()) {
                    setDataString(livingEntity, "gravePermissionList", String.join("|", grave.getPermissionList()));
                }

                if (livingEntity instanceof Mob && targetEntity != null && !targetEntity.isInvulnerable()
                        && (!(targetEntity instanceof Player) || ((Player) targetEntity).getGameMode()
                        != GameMode.CREATIVE)) {
                    ((Mob) livingEntity).setTarget(targetEntity);
                }

                if (livingEntity instanceof Zombie) {
                    Zombie zombie = (Zombie) livingEntity;

                    if (zombie.isBaby()) {
                        zombie.setBaby(false);
                    }
                }
            }

            plugin.debugMessage("Zombie type " + getEntityName(entity) + " spawned for grave " + grave.getUUID(), 1);
        }
    }

    /**
     * Creates an armor stand at a specified location associated with a grave.
     *
     * @param location the location to create the armor stand.
     * @param grave    the grave associated with the armor stand.
     */
    public void createArmorStand(Location location, Grave grave) {
        if (!plugin.getVersionManager().is_v1_7()
                && plugin.getConfig("armor-stand.enabled", grave).getBoolean("armor-stand.enabled")) {
            double offsetX = plugin.getConfig("armor-stand.offset.x", grave).getDouble("armor-stand.offset.x");
            double offsetY = plugin.getConfig("armor-stand.offset.y", grave).getDouble("armor-stand.offset.y");
            double offsetZ = plugin.getConfig("armor-stand.offset.z", grave).getDouble("armor-stand.offset.z");
            boolean marker = plugin.getConfig("armor-stand.marker", grave).getBoolean("armor-stand.marker");
            location = LocationUtil.roundLocation(location)
                    .add(offsetX + 0.5, offsetY, offsetZ + 0.5);

            location.setYaw(grave.getYaw());
            location.setPitch(grave.getPitch());

            if (location.getWorld() != null) {
                Material material = Material.matchMaterial(plugin.getConfig("armor-stand.material", grave)
                        .getString("armor-stand.material", "AIR"));

                if (material != null && !MaterialUtil.isAir(material)) {
                    ItemStack itemStack = new ItemStack(material, 1);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    int customModelData = plugin.getConfig("armor-stand.model-data", grave)
                            .getInt("armor-stand.model-data", -1);

                    if (itemMeta != null) {
                        if (customModelData > -1) {
                            itemMeta.setCustomModelData(customModelData);
                        }

                        itemStack.setItemMeta(itemMeta);
                        location.getBlock().setType(Material.AIR);

                        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);

                        createEntityData(location, armorStand.getUniqueId(), grave.getUUID(),
                                EntityData.Type.ARMOR_STAND);

                        if (!plugin.getVersionManager().is_v1_7()) {
                            try {
                                armorStand.setMarker(marker);
                            } catch (NoSuchMethodError ignored) {
                            }
                        }

                        if (!plugin.getVersionManager().is_v1_7() && !plugin.getVersionManager().is_v1_8()) {
                            armorStand.setInvulnerable(true);
                        }

                        if (plugin.getVersionManager().hasScoreboardTags()) {
                            armorStand.getScoreboardTags().add("graveArmorStand");
                            armorStand.getScoreboardTags().add("graveArmorStandUUID:" + grave.getUUID());
                        }

                        armorStand.setVisible(false);
                        armorStand.setGravity(false);
                        armorStand.setCustomNameVisible(false);
                        armorStand.setSmall(plugin.getConfig("armor-stand.small", grave)
                                .getBoolean("armor-stand.small"));

                        if (armorStand.getEquipment() != null) {
                            EquipmentSlot equipmentSlot = EquipmentSlot.HEAD;

                            try {
                                equipmentSlot = EquipmentSlot.valueOf(plugin.getConfig("armor-stand.slot", grave)
                                        .getString("armor-stand.slot", "HEAD"));
                            } catch (IllegalArgumentException ignored) {
                            }

                            armorStand.getEquipment().setItem(equipmentSlot, itemStack);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates an item frame at a specified location associated with a grave.
     *
     * @param location the location to create the item frame.
     * @param grave    the grave associated with the item frame.
     */
    public void createItemFrame(Location location, Grave grave) {
        if (plugin.getConfig("item-frame.enabled", grave).getBoolean("item-frame.enabled")) {
            double offsetX = plugin.getConfig("item-frame.offset.x", grave).getDouble("item-frame.offset.x");
            double offsetY = plugin.getConfig("item-frame.offset.y", grave).getDouble("item-frame.offset.y");
            double offsetZ = plugin.getConfig("item-frame.offset.z", grave).getDouble("item-frame.offset.z");
            location = LocationUtil.roundLocation(location).add(offsetX + 0.5, offsetY, offsetZ + 0.5);

            location.setYaw(grave.getYaw());
            location.setPitch(grave.getPitch());

            if (location.getWorld() != null) {
                Material material = Material.matchMaterial(plugin.getConfig("item-frame.material", grave)
                        .getString("item-frame.material", "AIR"));

                if (material != null && !MaterialUtil.isAir(material)) {
                    ItemStack itemStack = new ItemStack(material, 1);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    int customModelData = plugin.getConfig("item-frame.model-data", grave)
                            .getInt("item-frame.model-data", -1);

                    if (itemMeta != null) {
                        if (customModelData > -1) {
                            itemMeta.setCustomModelData(customModelData);
                        }

                        itemStack.setItemMeta(itemMeta);
                        location.getBlock().setType(Material.AIR);

                        ItemFrame itemFrame = location.getWorld().spawn(location, ItemFrame.class);

                        itemFrame.setFacingDirection(BlockFace.UP);
                        itemFrame.setRotation(BlockFaceUtil.getBlockFaceRotation(BlockFaceUtil
                                .getYawBlockFace(location.getYaw())));
                        itemFrame.setVisible(false);
                        itemFrame.setGravity(false);
                        itemFrame.setCustomNameVisible(false);
                        itemFrame.setItem(itemStack);

                        if (!plugin.getVersionManager().is_v1_7() && !plugin.getVersionManager().is_v1_8()) {
                            itemFrame.setInvulnerable(true);
                        }

                        if (plugin.getVersionManager().hasScoreboardTags()) {
                            itemFrame.getScoreboardTags().add("graveItemFrame");
                            itemFrame.getScoreboardTags().add("graveItemFrameUUID:" + grave.getUUID());
                        }

                        createEntityData(location, itemFrame.getUniqueId(), grave.getUUID(),
                                EntityData.Type.ITEM_FRAME);
                    }
                }
            }
        }
    }

    /**
     * Removes all entities associated with a grave.
     *
     * @param grave the grave whose entities to remove.
     */
    public void removeEntity(Grave grave) {
        removeEntity(getEntityDataMap(getLoadedEntityDataList(grave)));
    }

    /**
     * Removes a map of entity data and their associated entities.
     *
     * @param entityDataMap the map of entity data and entities to remove.
     */
    public void removeEntity(Map<EntityData, Entity> entityDataMap) {
        List<EntityData> entityDataList = new ArrayList<>();

        for (Map.Entry<EntityData, Entity> entry : entityDataMap.entrySet()) {
            if (entry.getKey().getType() == EntityData.Type.ARMOR_STAND
                    || entry.getKey().getType() == EntityData.Type.ITEM_FRAME
                    || entry.getKey().getType() == EntityData.Type.HOLOGRAM) {
                entry.getValue().remove();
                entityDataList.add(entry.getKey());
            }
        }

        plugin.getDataManager().removeEntityData(entityDataList);
    }

    /**
     * Retrieves a map of equipment slots and their corresponding item stacks for a living entity and grave.
     *
     * @param livingEntity the living entity to retrieve the equipment for.
     * @param grave        the grave associated with the equipment.
     * @return a map of equipment slots and their corresponding item stacks.
     */
    public Map<EquipmentSlot, ItemStack> getEquipmentMap(LivingEntity livingEntity, Grave grave) {
        Map<EquipmentSlot, ItemStack> equipmentSlotItemStackMap = new HashMap<>();

        if (livingEntity.getEquipment() != null) {
            EntityEquipment entityEquipment = livingEntity.getEquipment();

            if (entityEquipment.getHelmet() != null
                    && grave.getInventory().contains(entityEquipment.getHelmet())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.HEAD, entityEquipment.getHelmet());
            }

            if (entityEquipment.getChestplate() != null
                    && grave.getInventory().contains(entityEquipment.getChestplate())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.CHEST, entityEquipment.getChestplate());
            }

            if (entityEquipment.getLeggings() != null
                    && grave.getInventory().contains(entityEquipment.getLeggings())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.LEGS, entityEquipment.getLeggings());
            }

            if (entityEquipment.getBoots() != null
                    && grave.getInventory().contains(entityEquipment.getBoots())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.FEET, entityEquipment.getBoots());
            }

            if (plugin.getVersionManager().hasSecondHand()) {
                if (entityEquipment.getItemInMainHand().getType() != Material.AIR
                        && grave.getInventory().contains(entityEquipment.getItemInMainHand())) {
                    equipmentSlotItemStackMap.put(EquipmentSlot.HAND, entityEquipment.getItemInMainHand());
                }

                if (entityEquipment.getItemInOffHand().getType() != Material.AIR
                        && grave.getInventory().contains(entityEquipment.getItemInOffHand())) {
                    equipmentSlotItemStackMap.put(EquipmentSlot.OFF_HAND, entityEquipment.getItemInOffHand());
                }
            } else {
                if (entityEquipment.getItemInHand().getType() != Material.AIR
                        && grave.getInventory().contains(entityEquipment.getItemInHand())) {
                    equipmentSlotItemStackMap.put(EquipmentSlot.HAND, entityEquipment.getItemInHand());
                }
            }
        }

        return equipmentSlotItemStackMap;
    }

    /**
     * Returns the name of the specified entity.
     * <p>
     * This method handles different types of entities, including players and other entities, with legacy support for older versions of Minecraft.
     * </p>
     *
     * @param entity the {@link Entity} whose name is to be retrieved
     * @return the name of the entity, or "null" if the entity is null
     */
    @SuppressWarnings("redundant")
    public String getEntityName(Entity entity) {
        if (entity != null) {
            if (entity instanceof Player) {
                return ((Player) entity).getName(); // Need redundancy for legacy support
            } else if (!plugin.getVersionManager().is_v1_7()) {
                return entity.getName();
            }

            return StringUtil.format(entity.getType().toString());
        }

        return "null";
    }

    /**
     * Checks if the specified entity has a persistent data string with the given key.
     * <p>
     * The method checks for persistent data if supported; otherwise, it checks for metadata.
     * </p>
     *
     * @param entity the {@link Entity} to check
     * @param string the key of the persistent data or metadata
     * @return {@code true} if the entity has the specified data string; {@code false} otherwise
     */
    public boolean hasDataString(Entity entity, String string) {
        return plugin.getVersionManager().hasPersistentData() ? entity.getPersistentDataContainer()
                .has(new NamespacedKey(plugin, string), PersistentDataType.STRING) : entity.hasMetadata(string);
    }

    /**
     * Checks if the specified entity has a persistent data byte with the given key.
     * <p>
     * The method checks for persistent data if supported; otherwise, it checks for metadata.
     * </p>
     *
     * @param entity the {@link Entity} to check
     * @param string the key of the persistent data or metadata
     * @return {@code true} if the entity has the specified data byte; {@code false} otherwise
     */
    public boolean hasDataByte(Entity entity, String string) {
        return plugin.getVersionManager().hasPersistentData() ? entity.getPersistentDataContainer()
                .has(new NamespacedKey(plugin, string), PersistentDataType.BYTE) : entity.hasMetadata(string);
    }

    /**
     * Retrieves the persistent data string associated with the given key from the specified entity.
     * <p>
     * If persistent data is supported, it retrieves the string from the persistent data container; otherwise, it retrieves it from metadata.
     * </p>
     *
     * @param entity the {@link Entity} to retrieve data from
     * @param key the key of the persistent data or metadata
     * @return the data string associated with the key, or {@code null} if not found
     */
    public String getDataString(Entity entity, String key) {
        if (plugin.getVersionManager().hasPersistentData() && entity.getPersistentDataContainer()
                .has(new NamespacedKey(plugin, key), PersistentDataType.STRING)) {
            return entity.getPersistentDataContainer().get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
        } else {
            return entity.getMetadata(key).toString();
        }
    }

    /**
     * Sets a persistent data string for the specified entity with the given key.
     * <p>
     * If persistent data is supported, it sets the string in the persistent data container; otherwise, it sets it in metadata.
     * </p>
     *
     * @param entity the {@link Entity} to set data for
     * @param key the key of the persistent data or metadata
     * @param string the data string to set
     */
    public void setDataString(Entity entity, String key, String string) {
        if (plugin.getVersionManager().hasPersistentData()) {
            entity.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, string);
        } else {
            entity.setMetadata(key, new FixedMetadataValue(plugin, string));
        }
    }

    /**
     * Sets a persistent data byte for the specified entity with the given key.
     * <p>
     * If persistent data is supported, it sets the byte in the persistent data container; otherwise, it sets it in metadata.
     * </p>
     *
     * @param entity the {@link Entity} to set data for
     * @param key the key of the persistent data or metadata
     */
    public void setDataByte(Entity entity, String key) {
        if (plugin.getVersionManager().hasPersistentData()) {
            entity.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.BYTE, (byte) 1);
        } else {
            entity.setMetadata(key, new FixedMetadataValue(plugin, (byte) 1));
        }
    }

    /**
     * Retrieves a {@link Grave} object from the persistent data or metadata of the specified entity.
     * <p>
     * The method checks if persistent data is supported and looks for a "graveUUID" key. If not found, it checks for metadata.
     * </p>
     *
     * @param entity the {@link Entity} from which to retrieve the grave
     * @return the {@link Grave} associated with the entity, or {@code null} if not found
     */
    public Grave getGraveFromEntityData(Entity entity) {
        if (plugin.getVersionManager().hasPersistentData() && entity.getPersistentDataContainer()
                .has(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING)) {
            return plugin.getCacheManager().getGraveMap().get(UUIDUtil.getUUID(entity.getPersistentDataContainer()
                    .get(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING)));
        } else if (entity.hasMetadata("graveUUID")) {
            List<MetadataValue> metadataValue = entity.getMetadata("graveUUID");

            if (!metadataValue.isEmpty()) {
                return plugin.getCacheManager().getGraveMap().get(UUIDUtil.getUUID(metadataValue.get(0).asString()));
            }
        }

        return null;
    }
}
