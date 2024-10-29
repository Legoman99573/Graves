package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.HologramData;
import com.ranull.graves.event.GraveAbandonedEvent;
import com.ranull.graves.event.GraveAutoLootEvent;
import com.ranull.graves.event.GraveProtectionExpiredEvent;
import com.ranull.graves.event.GraveTimeoutEvent;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.inventory.GraveList;
import com.ranull.graves.inventory.GraveMenu;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.ColorUtil;
import com.ranull.graves.util.InventoryUtil;
import com.ranull.graves.util.MaterialUtil;
import com.ranull.graves.util.StringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the operations and lifecycle of graves within the Graves plugin.
 */
public final class GraveManager {
    /**
     * The main plugin instance associated with Graves.
     * <p>
     * This {@link Graves} instance represents the core plugin that this Graves is part of. It provides access
     * to the plugin's functionality, configuration, and other services.
     * </p>
     */
    private final Graves plugin;

    /**
     * A thread-safe map that holds references to scheduled {@link BukkitTask} instances.
     * <p>
     * The key is a unique identifier for the task, typically based on the specific action or entity associated with the task.
     * This map allows for efficient tracking and management of scheduled tasks, including the ability to cancel or reschedule tasks
     * if necessary.
     * </p>
     * <p>
     * Example use case: If you have a task related to a specific grave, you might use a unique identifier for that grave as the key
     * to manage the task associated with it.
     * </p>
     */
    private final ConcurrentHashMap<String, BukkitTask> tasks = new ConcurrentHashMap<>();


    /**
     * Initializes the GraveManager with the specified plugin instance.
     *
     * @param plugin the Graves plugin instance.
     */
    public GraveManager(Graves plugin) {
        this.plugin = plugin;
        startGraveTimer();
    }

    /**
     * Starts the grave timer task that periodically checks and updates graves.
     */
    private void startGraveTimer() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkAndUpdateGraves, 20L, 20L); // 10 ticks = 0.5 seconds
    }

    /**
     * Checks and updates graves, entities, and blocks, removing expired elements and triggering necessary events.
     */
    private void checkAndUpdateGraves() {
        List<Grave> graveRemoveList = new ArrayList<>();
        List<EntityData> entityDataRemoveList = new ArrayList<>();
        List<BlockData> blockDataRemoveList = new ArrayList<>();

        // Process Graves
        processGraves(graveRemoveList);

        // Process Chunks
        processChunks(entityDataRemoveList, blockDataRemoveList);

        // Remove expired graves, entities, and blocks
        removeExpiredElements(graveRemoveList, entityDataRemoveList, blockDataRemoveList);
    }

    /**
     * Processes all graves to check their remaining time and protection status.
     *
     * @param graveRemoveList the list to which graves to be removed will be added.
     */
    private void processGraves(List<Grave> graveRemoveList) {
        for (Grave grave : new ArrayList<>(plugin.getCacheManager().getGraveMap().values())) {

            long remainingTime = grave.getTimeAliveRemaining();

            // If the remaining time is -1, do not activate the event
            if (remainingTime == -1) {
                plugin.debugMessage("Grave " + grave.getUUID() + " has infinite time remaining, skipping timeout handling.", 2);
                return;
            }

            // Log the current state of the grave
            plugin.debugMessage("Checking grave: " + grave.getUUID() + " with remaining time: " + remainingTime, 2);

            // Check if the grave should be removed
            if (remainingTime == 0) {
                handleGraveTimeout(grave, graveRemoveList);
            }

            // Handle grave protection timeout
            if (grave.getProtection() && grave.getTimeProtectionRemaining() == 0) {
                toggleGraveProtection(grave);
            }
        }
    }

    /**
     * Handles the timeout of a grave by calling the GraveTimeoutEvent and removing the grave if not cancelled.
     *
     * @param grave the grave to check for timeout.
     * @param graveRemoveList the list to which graves to be removed will be added.
     */
    private void handleGraveTimeout(Grave grave, List<Grave> graveRemoveList) {
        long remainingTime = grave.getTimeAliveRemaining();
        boolean isAbandoned = grave.isAbandoned();
        plugin.debugMessage("Handling timeout for grave: " + grave.getUUID() + " with remaining time: " + remainingTime, 1);

        if (remainingTime == -1 || isAbandoned) {
            return;
        }

        GraveTimeoutEvent graveTimeoutEvent = new GraveTimeoutEvent(grave);
        plugin.getServer().getPluginManager().callEvent(graveTimeoutEvent);

        if (!graveTimeoutEvent.isCancelled() && !graveTimeoutEvent.isAddon()) {
            plugin.debugMessage("GraveTimeoutEvent not cancelled for grave: " + grave.getUUID(), 2);
            if (plugin.getConfig("drop.timeout", grave).getBoolean("drop.timeout")) {
                if (graveTimeoutEvent.getLocation() != null) {
                    Location location = graveTimeoutEvent.getLocation();
                    Chunk chunk = location.getChunk();
                    if (!chunk.isLoaded()) {
                        plugin.debugMessage("Loaded unloaded chunk x: " + chunk.getX() + ", z: " + chunk.getZ() + ". Graves should dump contents.", 2);
                        chunk.load();
                    }

                    // Schedule synchronous task to drop items and experience
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (chunk.isLoaded()) {
                            dropGraveItems(location, grave);
                            dropGraveExperience(location, grave);
                        }
                    });
                }

                if (grave.getOwnerType() == EntityType.PLAYER && grave.getOwnerUUID() != null) {
                    Player player = plugin.getServer().getPlayer(grave.getOwnerUUID());
                    if (player != null && player.isOnline()) {
                        plugin.getEntityManager().sendMessage("message.timeout", player, graveTimeoutEvent.getLocation(), grave);
                    }
                }

                graveRemoveList.add(grave);
            } else if (plugin.getConfig("drop.abandon", grave).getBoolean("drop.abandon")) {
                GraveAbandonedEvent graveAbandonedEvent = new GraveAbandonedEvent(grave);
                plugin.getServer().getPluginManager().callEvent(graveAbandonedEvent);

                if (!graveAbandonedEvent.isCancelled() && !graveAbandonedEvent.isAddon()) {
                    if (grave.getOwnerType() == EntityType.PLAYER && grave.getOwnerUUID() != null) {
                        Player player = plugin.getServer().getPlayer(grave.getOwnerUUID());
                        if (player != null && player.isOnline()) {
                            plugin.getEntityManager().sendMessage("message.abandoned", player, graveAbandonedEvent.getLocation(), grave);
                        }
                        grave.setTimeAliveRemaining(-1);
                        abandonGrave(grave);
                    }
                } else if (graveAbandonedEvent.isCancelled() && !graveAbandonedEvent.isAddon()) {
                    if (grave.getOwnerType() == EntityType.PLAYER && grave.getOwnerUUID() != null) {
                        Player player = plugin.getServer().getPlayer(grave.getOwnerUUID());
                        if (player != null && player.isOnline()) {
                            plugin.getEntityManager().sendMessage("message.timeout", player, graveTimeoutEvent.getLocation(), grave);
                        }
                    }
                    graveRemoveList.add(grave);
                }
            } else {
                if (grave.getOwnerType() == EntityType.PLAYER && grave.getOwnerUUID() != null) {
                    Player player = plugin.getServer().getPlayer(grave.getOwnerUUID());
                    if (player != null && player.isOnline()) {
                        plugin.getEntityManager().sendMessage("message.timeout", player, graveTimeoutEvent.getLocation(), grave);
                    }
                }
                graveRemoveList.add(grave);
            }
        } else if (graveTimeoutEvent.isCancelled() && !graveTimeoutEvent.isAddon()) {
            // Log the cancellation and set the grave's time to -1
            plugin.debugMessage("GraveTimeoutEvent cancelled for grave: " + grave.getUUID() + ", setting time alive to forever.", 2);
            grave.setTimeAliveRemaining(-1);
        }
    }

    /**
     * Checks if there are any players in the given chunk.
     *
     * @param chunk the chunk to check.
     * @return true if there are players in the chunk, false otherwise.
     */
    private boolean arePlayersInChunk(Chunk chunk) {
        return Arrays.stream(chunk.getEntities()).anyMatch(entity -> entity instanceof Player);
    }

    /**
     * Processes all chunks to handle entities and blocks within them.
     *
     * @param entityDataRemoveList the list to which entity data to be removed will be added.
     * @param blockDataRemoveList the list to which block data to be removed will be added.
     */
    private void processChunks(List<EntityData> entityDataRemoveList, List<BlockData> blockDataRemoveList) {
        for (ChunkData chunkData : plugin.getCacheManager().getChunkMap().values()) {
            if (!chunkData.isLoaded()) {
                continue;
            }

            Location location = new Location(chunkData.getWorld(), chunkData.getX() << 4, 0, chunkData.getZ() << 4);

            // Process Entity Data
            processEntityData(chunkData, entityDataRemoveList, location);

            // Process Block Data
            processBlockData(chunkData, blockDataRemoveList);
        }
    }

    /**
     * Removes expired graves, entities, and blocks from the system.
     *
     * @param graveRemoveList the list of graves to be removed.
     * @param entityDataRemoveList the list of entity data to be removed.
     * @param blockDataRemoveList the list of block data to be removed.
     */
    private void removeExpiredElements(List<Grave> graveRemoveList, List<EntityData> entityDataRemoveList, List<BlockData> blockDataRemoveList) {
        if (plugin.isEnabled()) {
            for (Grave grave : graveRemoveList) {
                plugin.debugMessage("Removing grave: " + grave.getUUID(), 2);
                removeGrave(grave);
            }
            for (EntityData entityData : entityDataRemoveList) {
                if (entityData != null) { // Null check before calling removeEntityData
                    removeEntityData(entityData);
                } else {
                    plugin.debugMessage("Attempted to remove null EntityData", 2);
                }
            }
            for (BlockData blockData : blockDataRemoveList) {
                plugin.getBlockManager().removeBlock(blockData);
            }
            graveRemoveList.clear();
            entityDataRemoveList.clear();
            blockDataRemoveList.clear();
            plugin.getGUIManager().refreshMenus();
        }
    }

    /**
     * Processes the entity data within the given chunk.
     *
     * @param chunkData          the data of the chunk being processed.
     * @param entityDataRemoveList the list to which entity data to be removed will be added.
     * @param location           the location representing the chunk coordinates.
     */
    private void processEntityData(ChunkData chunkData, List<EntityData> entityDataRemoveList, Location location) {
        try {
            for (EntityData entityData : new ArrayList<>(chunkData.getEntityDataMap().values())) {
                if (entityData == null) {
                    plugin.debugMessage("Encountered null EntityData while processing chunk at coordinates: ("
                            + chunkData.getX() + ", " + chunkData.getZ() + ").", 2);
                    continue;
                }

                if (entityData.getUUIDGrave() != null && plugin.getCacheManager().getGraveMap().containsKey(entityData.getUUIDGrave())) {
                    if (plugin.isEnabled() && entityData instanceof HologramData) {
                        processHologramData((HologramData) entityData, location, entityDataRemoveList);
                    }
                } else {
                    entityDataRemoveList.add(entityData);
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // ignored
        }
    }

    /**
     * Processes hologram data within the chunk.
     *
     * @param hologramData       the hologram data to be processed.
     * @param location           the location representing the chunk coordinates.
     * @param entityDataRemoveList the list to which hologram data to be removed will be added.
     */
    private void processHologramData(HologramData hologramData, Location location, List<EntityData> entityDataRemoveList) {
        try {
            Grave grave = plugin.getCacheManager().getGraveMap().get(hologramData.getUUIDGrave());

            if (grave != null) {
                List<String> lineList = plugin.getConfig("hologram.line", grave).getStringList("hologram.line");
                Collections.reverse(lineList);

                for (Entity entity : hologramData.getLocation().getChunk().getEntities()) {
                    if (entity.getUniqueId().equals(hologramData.getUUIDEntity())) {
                        if (hologramData.getLine() < lineList.size()) {
                            if (plugin.getIntegrationManager().hasMiniMessage()) {
                                String newHologramLine = StringUtil.parseString(lineList.get(hologramData.getLine()), location, grave, plugin);
                                entity.setCustomName(MiniMessage.parseString(newHologramLine));
                            } else {
                                entity.setCustomName(StringUtil.parseString(lineList.get(hologramData.getLine()), location, grave, plugin));
                            }
                        } else {
                            entityDataRemoveList.add(hologramData);
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // ignored
        }
    }

    /**
     * Processes the block data within the given chunk.
     *
     * @param chunkData          the data of the chunk being processed.
     * @param blockDataRemoveList the list to which block data to be removed will be added.
     */
    private void processBlockData(ChunkData chunkData, List<BlockData> blockDataRemoveList) {
        try {
            for (BlockData blockData : new ArrayList<>(chunkData.getBlockDataMap().values())) {
                if (blockData.getLocation().getWorld() != null) {
                    if (plugin.getCacheManager().getGraveMap().containsKey(blockData.getGraveUUID())) {
                        graveParticle(blockData.getLocation(), plugin.getCacheManager().getGraveMap().get(blockData.getGraveUUID()));
                    } else {
                        blockDataRemoveList.add(blockData);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // ignored
        }
    }

    /**
     * Unloads all open grave inventories for online players.
     */
    @SuppressWarnings("ConstantConditions")
    public void unload() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null) { // Mohist, might return null even when Bukkit shouldn't.
                Inventory topInventory = CompatibilityInventoryView.getTopInventory(player.getOpenInventory());
                InventoryHolder inventoryHolder = topInventory.getHolder();

                try {
                    if (inventoryHolder instanceof Grave || inventoryHolder instanceof GraveList
                            || inventoryHolder instanceof GraveMenu) {
                        player.closeInventory();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Toggles the protection state of a grave.
     *
     * @param grave the grave to toggle protection for.
     */
    public void toggleGraveProtection(Grave grave) {
        boolean currentProtection = grave.getProtection();
        long protectionRemaining = grave.getTimeProtectionRemaining();
        grave.setProtection(!currentProtection);
        plugin.getDataManager().updateGrave(grave, "protection", grave.getProtection() ? 1 : 0);

        if (protectionRemaining == -1) {
            plugin.debugMessage("Grave " + grave.getUUID() + " has infinite protection, skipping protection remaining handling.", 2);
            return;
        }

        if (currentProtection) {
            // Trigger GraveProtectionExpiredEvent when protection expires
            GraveProtectionExpiredEvent event = new GraveProtectionExpiredEvent(grave);
            plugin.getServer().getPluginManager().callEvent(event);

            // If the event is cancelled, revert the protection state
            if (event.isCancelled() && !event.isAddon()) {
                grave.setProtection(true);
                plugin.debugMessage("GraveProtectionExpiredEvent called for grave: " + grave.getUUID(), 2);
                plugin.getDataManager().updateGrave(grave, "protection", 1);
                grave.setTimeProtection(-1);
            } else if (!event.isCancelled() && !event.isAddon()) {
                // Log the grave details
                plugin.debugMessage("Grave protection expired for grave: " + grave.getUUID(), 1);
                plugin.getDataManager().updateGrave(grave, "protection", grave.getProtection() ? 1 : 0);
            }
        }
    }

    /**
     * Abandons a grave.
     *
     * @param grave    the grave to abandon.
     */
    public void abandonGrave(Grave grave) {
        grave.setAbandoned(true);
        grave.setExperience(0);
        grave.setTimeProtection(0);
        grave.setTimeAlive(-1);
        grave.setTimeAliveRemaining(-1);
        grave.setOwnerName("Abandoned");
        grave.setOwnerDisplayName("Abandoned");
        grave.setOwnerTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTYxZjFmY2Q0MmY0OGNhNTFmOWRhN2M1NWI3MmYzNWE4MjZlNzViNmEwMjA0OGExZGVhNWQ3MTE5YmM5Y2Q2OSJ9fX0=");
        plugin.getDataManager().updateGrave(grave, "owner_name", grave.getOwnerName());
        plugin.getDataManager().updateGrave(grave, "experience", grave.getExperience());
        plugin.getDataManager().updateGrave(grave, "owner_name_display", grave.getOwnerDisplayName());
        plugin.getDataManager().updateGrave(grave, "is_abandoned", grave.isAbandoned() ? 1 : 0);
        // plugin.getDataManager().loadGraveMap();
    }

    /**
     * Spawns particle effects around a grave.
     *
     * @param location the location of the grave.
     * @param grave    the grave to spawn particles for.
     */
    public void graveParticle(Location location, Grave grave) {
        if (plugin.getVersionManager().hasParticle()
                && location.getWorld() != null
                && plugin.getConfig("particle.enabled", grave).getBoolean("particle.enabled")) {
            Particle particle = Particle.valueOf(plugin.getVersionManager().getParticleForVersion("REDSTONE").toString());
            String particleType = plugin.getConfig("particle.type", grave).getString("particle.type");

            if (particleType != null && !particleType.equals("")) {
                try {
                    particle = Particle.valueOf(plugin.getConfig("particle.type", grave)
                            .getString("particle.type"));
                } catch (IllegalArgumentException ignored) {
                    plugin.debugMessage(particleType + " is not a Particle ENUM", 1);
                }
            }

            int count = plugin.getConfig("particle.count", grave).getInt("particle.count");
            double offsetX = plugin.getConfig("particle.offset.x", grave).getDouble("particle.offset.x");
            double offsetY = plugin.getConfig("particle.offset.y", grave).getDouble("particle.offset.y");
            double offsetZ = plugin.getConfig("particle.offset.z", grave).getDouble("particle.offset.z");
            location = location.clone().add(offsetX + 0.5, offsetY + 0.5, offsetZ + 0.5);

            if (location.getWorld() != null) {
                switch (particle.name()) {
                    case "DUST":
                    case "REDSTONE":
                        int sizeInt = plugin.getConfig("particle.dust-size", grave).getInt("particle.dust-size");
                        float size = (float) sizeInt; // Convert to float
                        Color color = ColorUtil.getColor(plugin.getConfig("particle.dust-color", grave)
                                .getString("particle.dust-color", "RED"));

                        if (color == null) {
                            color = Color.RED;
                        }
                        try {
                            location.getWorld().spawnParticle(particle, location, count,
                                    new Particle.DustOptions(color, size));
                        } catch (IllegalArgumentException e) {
                            location.getWorld().spawnParticle(particle, location, count, 1);
                        }
                        break;
                    case "SHRIEK":
                        location.getWorld().spawnParticle(particle, location, count, 1);
                        break;
                    default:
                        try {
                            location.getWorld().spawnParticle(particle, location, count);
                        } catch (IllegalArgumentException e) {
                            // May not work for all forks and versions, but will try again
                            location.getWorld().spawnParticle(particle, location, count, 0, 0, 0, 0);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Removes the oldest grave.
     */

    public void removeOldestGrave(LivingEntity livingEntity) {
        Grave toDel = plugin.getCacheManager().getOldestGrave(livingEntity.getUniqueId());
        if (toDel != null) {
            removeGrave(toDel);
        }
    }

    /**
     * Removes a grave and its associated data.
     *
     * @param grave the grave to remove.
     */
    public void removeGrave(Grave grave) {
        plugin.debugMessage("Starting removal of grave: " + grave.getUUID(), 1);
        closeGrave(grave);
        plugin.getBlockManager().removeBlock(grave);
        plugin.getHologramManager().removeHologram(grave);
        plugin.getEntityManager().removeEntity(grave);
        plugin.getDataManager().removeGrave(grave);

        if (plugin.getIntegrationManager().hasWorldEdit()) {
            plugin.getIntegrationManager().getWorldEdit().clearSchematic(grave);
        }

        if (plugin.getIntegrationManager().hasMultiPaper()) {
            plugin.getIntegrationManager().getMultiPaper().notifyGraveRemoval(grave);
        }

        if (plugin.getIntegrationManager().hasFurnitureLib()) {
            plugin.getIntegrationManager().getFurnitureLib().removeFurniture(grave);
        }

        if (plugin.getIntegrationManager().hasFurnitureEngine()) {
            plugin.getIntegrationManager().getFurnitureEngine().removeFurniture(grave);
        }

        if (plugin.getIntegrationManager().hasItemsAdder()) {
            plugin.getIntegrationManager().getItemsAdder().removeFurniture(grave);
        }

        if (plugin.getIntegrationManager().hasOraxen()) {
            plugin.getIntegrationManager().getOraxen().removeFurniture(grave);
        }

        if (plugin.getIntegrationManager().hasPlayerNPC()) {
            plugin.getIntegrationManager().getPlayerNPC().removeCorpse(grave);
        }
        if (plugin.getIntegrationManager().hasFancyNpcs()) {
            plugin.getIntegrationManager().getFancyNpcs().removeCorpse(grave);
        }

        if (plugin.getIntegrationManager().hasCitizensNPC()) {
            plugin.getIntegrationManager().getCitizensNPC().removeCorpse(grave);
        }

        // Remove the grave from the cache
        plugin.getCacheManager().getGraveMap().remove(grave.getUUID());

        plugin.debugMessage("Grave " + grave.getUUID() + " removed from cache", 1);
    }

    /**
     * Removes entity data associated with a grave.
     *
     * @param entityData the entity data to remove.
     */
    public void removeEntityData(EntityData entityData) {
        if (entityData.getType() == null) {
            plugin.debugMessage("Attempted to remove null entity data. This is not a bug", 3);
            return;
        }
        switch (entityData.getType()) {
            case HOLOGRAM: {
                plugin.getHologramManager().removeHologram(entityData);
                break;
            }
            case FURNITURELIB: {
                plugin.getIntegrationManager().getFurnitureLib().removeEntityData(entityData);
                break;
            }
            case FURNITUREENGINE: {
                plugin.getIntegrationManager().getFurnitureEngine().removeEntityData(entityData);
                break;
            }
            case ITEMSADDER: {
                plugin.getIntegrationManager().getItemsAdder().removeEntityData(entityData);
                break;
            }
            case ORAXEN: {
                plugin.getIntegrationManager().getOraxen().removeEntityData(entityData);
                break;
            }
            case PLAYERNPC: {
                plugin.getIntegrationManager().getPlayerNPC().removeEntityData(entityData);
                break;
            }
            case CITIZENSNPC: {
                plugin.getIntegrationManager().getCitizensNPC().removeEntityData(entityData);
                break;
            }
        }
    }

    /**
     * Closes any open inventories associated with a grave.
     *
     * @param grave the grave to close inventories for.
     */
    @SuppressWarnings("ConstantConditions")
    public void closeGrave(Grave grave) {
        List<HumanEntity> inventoryViewers = grave.getInventory().getViewers();

        for (HumanEntity humanEntity : new ArrayList<>(inventoryViewers)) {
            grave.getInventory().getViewers().remove(humanEntity);
            humanEntity.closeInventory();
            plugin.debugMessage("Closing grave " + grave.getUUID() + " for " + humanEntity.getName(), 1);
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null) { // Mohist, might return null even when Bukkit shouldn't.
                Inventory topInventory = CompatibilityInventoryView.getTopInventory(player.getOpenInventory());
                InventoryHolder inventoryHolder = topInventory.getHolder();

                if (inventoryHolder instanceof GraveMenu) {
                    GraveMenu graveMenu = (GraveMenu) inventoryHolder;

                    if (graveMenu.getGrave().getUUID().equals(grave.getUUID())) {
                        player.closeInventory();
                    }
                }
            }
        }
    }

    /**
     * Creates a new grave for the specified entity and list of item stacks.
     *
     * @param entity        the entity to create the grave for.
     * @param itemStackList the list of item stacks to be included in the grave.
     * @return the created grave.
     */
    public Grave createGrave(Entity entity, List<ItemStack> itemStackList) {
        return createGrave(entity, itemStackList, plugin.getPermissionList(entity));
    }

    /**
     * Creates a new grave for the specified entity, list of item stacks, and permissions.
     *
     * @param entity          the entity to create the grave for.
     * @param itemStackList   the list of item stacks to be included in the grave.
     * @param permissionList  the list of permissions associated with the grave.
     * @return the created grave.
     */
    public Grave createGrave(Entity entity, List<ItemStack> itemStackList, List<String> permissionList) {
        Grave grave = new Grave(UUID.randomUUID());
        String entityName = plugin.getEntityManager().getEntityName(entity);

        grave.setOwnerType(entity.getType());
        grave.setOwnerName(entityName);
        grave.setOwnerNameDisplay(entity instanceof Player ? ((Player) entity).getDisplayName()
                : entity.getCustomName());
        grave.setOwnerUUID(entity.getUniqueId());
        grave.setInventory(createGraveInventory(grave, entity.getLocation(), itemStackList,
                StringUtil.parseString(plugin.getConfig("gui.grave.title", entity, permissionList)
                        .getString("gui.grave.title"), entity, entity.getLocation(), grave, plugin),
                getStorageMode(plugin.getConfig("storage.mode", entity, permissionList).getString("storage.mode"))));
        plugin.debugMessage("Creating grave " + grave.getUUID() + " for entity " + entityName, 1);

        return grave;
    }

    /**
     * Retrieves the storage mode for a given string representation.
     *
     * @param string the string representation of the storage mode.
     * @return the corresponding storage mode.
     */
    public Grave.StorageMode getStorageMode(String string) {
        try {
            Grave.StorageMode storageMode = Grave.StorageMode.valueOf(string.toUpperCase());

            if (storageMode == Grave.StorageMode.CHESTSORT && !plugin.getIntegrationManager().hasChestSort()) {
                return Grave.StorageMode.COMPACT;
            }

            return storageMode;
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }

        return Grave.StorageMode.COMPACT;
    }

    /**
     * Places a grave at a specified location.
     *
     * @param location the location to place the grave.
     * @param grave    the grave to be placed.
     */
    public void placeGrave(Location location, Grave grave) {
        plugin.getBlockManager().createBlock(location, grave);
        plugin.getHologramManager().createHologram(location, grave);
        plugin.getEntityManager().createArmorStand(location, grave);
        plugin.getEntityManager().createItemFrame(location, grave);

        if (plugin.getIntegrationManager().hasWorldEdit()) {
            plugin.getIntegrationManager().getWorldEdit().createSchematic(location, grave);
        }

        if (plugin.getIntegrationManager().hasFurnitureLib()) {
            plugin.getIntegrationManager().getFurnitureLib().createFurniture(location, grave);
        }

        if (plugin.getIntegrationManager().hasFurnitureEngine()) {
            plugin.getIntegrationManager().getFurnitureEngine().createFurniture(location, grave);
        }

        if (plugin.getIntegrationManager().hasItemsAdder()) {
            plugin.getIntegrationManager().getItemsAdder().createFurniture(location, grave);
        }

        if (plugin.getIntegrationManager().hasOraxen()) {
            plugin.getIntegrationManager().getOraxen().createFurniture(location, grave);
        }

        if (plugin.getIntegrationManager().hasPlayerNPC()) {
            plugin.getIntegrationManager().getPlayerNPC().createCorpse(location, grave);
        }

        if (plugin.getIntegrationManager().hasCitizensNPC()) {
            plugin.getIntegrationManager().getCitizensNPC().createCorpse(location, grave);
        }
        if (plugin.getIntegrationManager().hasFancyNpcs() && !plugin.getIntegrationManager().hasFloodgate()) {
            plugin.getIntegrationManager().getFancyNpcs().createCorpse(grave.getUUID(),grave.getLocationDeath(),grave);
        }
        if (plugin.getIntegrationManager().hasFancyNpcs() && plugin.getIntegrationManager().hasFloodgate()) {
            if (FloodgateApi.getInstance().isFloodgatePlayer(grave.getOwnerUUID())) {
                plugin.getIntegrationManager().getFancyNpcs().createBedrockcompatCorpse(grave.getUUID(),grave.getLocationDeath(),grave);
            } else {
                plugin.getIntegrationManager().getFancyNpcs().createCorpse(grave.getUUID(),grave.getLocationDeath(),grave);
            }
        }
    }

    /**
     * Retrieves the grave inventory for a specified grave and living entity.
     *
     * @param grave                  the grave.
     * @param livingEntity           the living entity.
     * @param graveItemStackList     the list of item stacks to be included in the grave.
     * @param removedItemStackList   the list of item stacks to be removed.
     * @param permissionList         the list of permissions associated with the grave.
     * @return the created grave inventory.
     */
    public Inventory getGraveInventory(Grave grave, LivingEntity livingEntity,
                                       List<ItemStack> graveItemStackList, List<ItemStack> removedItemStackList,
                                       List<String> permissionList) {
        List<ItemStack> filterGraveItemStackList = filterGraveItemStackList(graveItemStackList, removedItemStackList,
                livingEntity, permissionList);
        String title;
        if (plugin.getIntegrationManager().hasMiniMessage()) {
            String newTitle = StringUtil.parseString(plugin.getConfig("gui.grave.title", grave)
                    .getString("gui.grave.title"), livingEntity, grave.getLocationDeath(), grave, plugin);
            title = MiniMessage.parseString(newTitle);
        } else {
            title = StringUtil.parseString(plugin.getConfig("gui.grave.title", grave)
                    .getString("gui.grave.title"), livingEntity, grave.getLocationDeath(), grave, plugin);
        }

        Grave.StorageMode storageMode = getStorageMode(plugin.getConfig("storage.mode", grave)
                .getString("storage.mode"));

        return plugin.getGraveManager().createGraveInventory(grave, grave.getLocationDeath(), filterGraveItemStackList,
                title, storageMode);
    }

    /**
     * Creates a grave inventory with the specified parameters.
     *
     * @param inventoryHolder  the holder of the inventory.
     * @param location         the location of the grave.
     * @param itemStackList    the list of item stacks to be included in the inventory.
     * @param title            the title of the inventory.
     * @param storageMode      the storage mode for the inventory.
     * @return the created inventory.
     */
    public Inventory createGraveInventory(InventoryHolder inventoryHolder, Location location,
                                          List<ItemStack> itemStackList, String title, Grave.StorageMode storageMode) {
        if (storageMode == Grave.StorageMode.COMPACT || storageMode == Grave.StorageMode.CHESTSORT) {
            Inventory tempInventory = plugin.getServer().createInventory(null, 54);
            int counter = 0;

            for (ItemStack itemStack : itemStackList) {
                if (getItemStacksSize(tempInventory.getContents()) < tempInventory.getSize()) {
                    if (itemStack != null && !MaterialUtil.isAir(itemStack.getType())) {
                        tempInventory.addItem(itemStack);
                        counter++;
                    }
                } else if (itemStack != null && location != null && location.getWorld() != null) {
                    location.getWorld().dropItem(location, itemStack);
                }
            }

            counter = 0;

            for (ItemStack itemStack : tempInventory.getContents()) {
                if (itemStack != null) {
                    counter++;
                }
            }

            Inventory inventory = plugin.getServer().createInventory(inventoryHolder,
                    InventoryUtil.getInventorySize(counter), title);

            for (ItemStack itemStack : tempInventory.getContents()) {
                if (itemStack != null && location != null && location.getWorld() != null) {
                    inventory.addItem(itemStack).forEach((key, value) -> location.getWorld().dropItem(location, value));
                }
            }

            if (storageMode == Grave.StorageMode.CHESTSORT && plugin.getIntegrationManager().hasChestSort()) {
                plugin.getIntegrationManager().getChestSort().sortInventory(inventory);
            }

            return inventory;
        } else if (storageMode == Grave.StorageMode.EXACT) {
            if (plugin.getVersionManager().hasEnchantmentCurse()) {
                itemStackList.removeIf(itemStack -> itemStack != null &&
                        itemStack.containsEnchantment(Enchantment.VANISHING_CURSE));
            }

            ItemStack itemStackAir = new ItemStack(Material.AIR);
            Inventory inventory = plugin.getServer().createInventory(inventoryHolder,
                    InventoryUtil.getInventorySize(itemStackList.size()), title);

            int counter = 0;
            for (ItemStack itemStack : itemStackList) {
                if (counter < inventory.getSize()) {
                    inventory.setItem(counter, itemStack != null ? itemStack : itemStackAir);
                } else if (itemStack != null && location != null && location.getWorld() != null) {
                    location.getWorld().dropItem(location, itemStack);
                }

                counter++;
            }

            return inventory;
        }

        return null;
    }

    /**
     * Gets the size of the item stacks array.
     *
     * @param itemStacks the array of item stacks.
     * @return the size of the item stacks array.
     */
    public int getItemStacksSize(ItemStack[] itemStacks) {
        int counter = 0;

        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null) {
                counter++;
            }
        }

        return counter;
    }

    /**
     * Filters the grave item stack list based on the living entity and permission list.
     *
     * @param itemStackList     the original list of item stacks.
     * @param livingEntity      the living entity.
     * @param permissionList    the list of permissions associated with the grave.
     * @return the filtered list of item stacks.
     */
    public List<ItemStack> filterGraveItemStackList(List<ItemStack> itemStackList, LivingEntity livingEntity, List<String> permissionList) {
        return filterGraveItemStackList(itemStackList, new ArrayList<>(), livingEntity, permissionList);
    }

    /**
     * Filters the grave item stack list based on the living entity, removed item stacks, and permission list.
     *
     * @param itemStackList         the original list of item stacks.
     * @param removedItemStackList  the list of item stacks to be removed.
     * @param livingEntity          the living entity.
     * @param permissionList        the list of permissions associated with the grave.
     * @return the filtered list of item stacks.
     */
    public List<ItemStack> filterGraveItemStackList(List<ItemStack> itemStackList, List<ItemStack> removedItemStackList,
                                                    LivingEntity livingEntity, List<String> permissionList) {
        itemStackList = new ArrayList<>(itemStackList);

        if (livingEntity instanceof Player && getStorageMode(plugin.getConfig("storage.mode",
                livingEntity, permissionList).getString("storage.mode")) == Grave.StorageMode.EXACT) {
            Player player = (Player) livingEntity;
            List<ItemStack> playerInventoryContentList = Arrays.asList(player.getInventory().getContents());

            List<ItemStack> itemStackListNew = new ArrayList<>(playerInventoryContentList);
            List<ItemStack> differenceList = new ArrayList<>(removedItemStackList);

            differenceList.removeIf(itemStackList::contains);
            itemStackListNew.removeAll(differenceList);
            itemStackList.removeAll(playerInventoryContentList);

            if (!itemStackList.isEmpty()) {
                int counter = 0;

                for (ItemStack itemStack : new ArrayList<>(itemStackListNew)) {
                    if (!itemStackList.isEmpty()) {
                        if (itemStack == null) {
                            itemStackListNew.set(counter, itemStackList.get(0));
                        } else {
                            itemStackListNew.add(itemStackList.get(0));
                        }

                        itemStackList.remove(0);
                    }

                    counter++;
                }
            }

            return itemStackListNew;
        }

        return itemStackList;
    }

    /**
     * Breaks a grave at its death location.
     *
     * @param grave the grave to be broken.
     */
    public void breakGrave(Grave grave) {
        breakGrave(grave.getLocationDeath(), grave);
    }

    /**
     * Breaks a grave at the specified location.
     *
     * @param location the location to break the grave.
     * @param grave    the grave to be broken.
     */
    public void breakGrave(Location location, Grave grave) {
        dropGraveItems(location, grave);
        dropGraveExperience(location, grave);
        removeGrave(grave);
        plugin.debugMessage("Grave " + grave.getUUID() + " broken", 1);
    }

    /**
     * Drops the items from a grave at the specified location.
     *
     * @param location the location to drop the items.
     * @param grave    the grave containing the items.
     */
    public void dropGraveItems(Location location, Grave grave) {
        if (grave != null && location.getWorld() != null) {
            for (ItemStack itemStack : grave.getInventory()) {
                if (itemStack != null) {
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }

            grave.getInventory().clear();
        }
    }

    /**
     * Gives the experience from a grave to a player.
     *
     * @param player the player to receive the experience.
     * @param grave  the grave containing the experience.
     */
    public void giveGraveExperience(Player player, Grave grave) {
        if (grave.getExperience() > 0) {
            player.giveExp(grave.getExperience());
            grave.setExperience(0);
            plugin.getEntityManager().playWorldSound("ENTITY_EXPERIENCE_ORB_PICKUP", player);
        }
    }

    /**
     * Drops the experience from a grave at the specified location.
     *
     * @param location the location to drop the experience.
     * @param grave    the grave containing the experience.
     */
    public void dropGraveExperience(Location location, Grave grave) {
        if (grave.getExperience() > 0 && location.getWorld() != null) {
            ExperienceOrb experienceOrb = (ExperienceOrb) location.getWorld()
                    .spawnEntity(location, EntityType.EXPERIENCE_ORB);

            experienceOrb.setExperience(grave.getExperience());
            grave.setExperience(0);
        }
    }

    /**
     * Retrieves a list of graves associated with a player.
     *
     * @param player the player to retrieve the graves for.
     * @return the list of graves.
     */
    public List<Grave> getGraveList(Player player) {
        return getGraveList(player.getUniqueId());
    }

    /**
     * Retrieves a list of graves associated with an offline player.
     *
     * @param player the offline player to retrieve the graves for.
     * @return the list of graves.
     */
    public List<Grave> getGraveList(OfflinePlayer player) {
        return getGraveList(player.getUniqueId());
    }

    /**
     * Retrieves a list of graves associated with an entity.
     *
     * @param entity the entity to retrieve the graves for.
     * @return the list of graves.
     */
    public List<Grave> getGraveList(Entity entity) {
        return getGraveList(entity.getUniqueId());
    }

    /**
     * Retrieves a list of graves associated with a UUID.
     *
     * @param uuid the UUID to retrieve the graves for.
     * @return the list of graves.
     */
    public List<Grave> getGraveList(UUID uuid) {
        List<Grave> graveList = new ArrayList<>();

        plugin.getCacheManager().getGraveMap().forEach((key, value) -> {
            if (!value.isAbandoned() && value.getOwnerUUID() != null && value.getOwnerUUID().equals(uuid)) {
                graveList.add(value);
            }
        });

        return graveList;
    }

    /**
     * Retrieves the number of graves associated with an entity.
     *
     * @param entity the entity to retrieve the grave count for.
     * @return the number of graves.
     */
    public int getGraveCount(Entity entity) {
        return getGraveList(entity).size();
    }

    /**
     * Opens a grave for a player.
     *
     * @param entity   the entity attempting to open the grave.
     * @param location the location of the grave.
     * @param grave    the grave to be opened.
     * @return true if the grave was opened successfully, false otherwise.
     */
    public boolean openGrave(Entity entity, Location location, Grave grave) {
        openGrave(entity, location, grave, false);
        return false;
    }

    /**
     * Opens a grave for a player.
     *
     * @param entity   the entity attempting to open the grave.
     * @param location the location of the grave.
     * @param grave    the grave to be opened.
     * @return true if the grave was opened successfully, false otherwise.
     */
    public boolean openGrave(Entity entity, Location location, Grave grave, boolean preview) {
        if (entity instanceof Player) {
            Player player = (Player) entity;

            plugin.getEntityManager().swingMainHand(player);

            if (plugin.getEntityManager().canOpenGrave(player, grave)) {
                cleanupCompasses(player, grave);

                if (player.isSneaking() && plugin.hasGrantedPermission("graves.autoloot", player.getPlayer())) {
                    GraveAutoLootEvent graveAutoLootEvent = new GraveAutoLootEvent(player, location, grave);

                    plugin.getServer().getPluginManager().callEvent(graveAutoLootEvent);
                    if (!graveAutoLootEvent.isCancelled())
                        autoLootGrave(player, location, grave);
                } else if (plugin.hasGrantedPermission("graves.open", player.getPlayer())) {
                    if (plugin.getConfig("grave.preview", grave).getBoolean("grave.preview", false)) {
                        grave.setGravePreview(preview);
                    } else {
                        grave.setGravePreview(false);
                    }
                    player.openInventory(grave.getInventory());
                    plugin.getEntityManager().runCommands("event.command.open", player, location, grave);
                    plugin.getEntityManager().playWorldSound("sound.open", location, grave);
                }

                return true;
            } else {
                if (plugin.getConfig("protection.preview", grave).getBoolean("protection.preview", false)) {
                    if (plugin.getConfig("grave.preview", grave).getBoolean("grave.preview", false)) {
                        grave.setGravePreview(preview);
                    } else {
                        grave.setGravePreview(false);
                    }
                    player.openInventory(grave.getInventory());
                    plugin.getEntityManager().runCommands("event.command.open", player, location, grave);
                    plugin.getEntityManager().playWorldSound("sound.open", location, grave);
                } else {
                    plugin.getEntityManager().sendMessage("message.protection", player, location, grave);
                    plugin.getEntityManager().playWorldSound("sound.protection", location, grave);
                }
            }
        }

        return false;
    }

    /**
     * Cleans up compasses from a player's inventory that are associated with a grave.
     *
     * @param player the player to clean up the compasses for.
     * @param grave  the grave associated with the compasses.
     */
    public void cleanupCompasses(Player player, Grave grave) {
        for (Map.Entry<ItemStack, UUID> entry : plugin.getEntityManager()
                .getCompassesFromInventory(player).entrySet()) {
            if (grave.getUUID().equals(entry.getValue())) {
                player.getInventory().remove(entry.getKey());
            }
        }
    }

    /**
     * Retrieves a list of locations associated with a grave.
     *
     * @param baseLocation the base location.
     * @param grave        the grave to retrieve the locations for.
     * @return the list of locations.
     */
    public List<Location> getGraveLocationList(Location baseLocation, Grave grave) {
        List<Location> locationList = new ArrayList<>(plugin.getBlockManager().getBlockList(grave));
        Map<Double, Location> locationMap = new HashMap<>();
        List<Location> otherWorldLocationList = new ArrayList<>();

        if (baseLocation.getWorld() != null) {
            if (!locationList.contains(grave.getLocationDeath())) {
                locationList.add(grave.getLocationDeath());
            }

            for (Location location : locationList) {
                if (location != null) {
                    if (location.getWorld() != null && baseLocation.getWorld().equals(location.getWorld())) {
                        locationMap.put(location.distanceSquared(baseLocation), location);
                    } else {
                        otherWorldLocationList.add(location);
                    }
                }
            }

            locationList = new ArrayList<>(new TreeMap<>(locationMap).values());

            locationList.addAll(otherWorldLocationList);
        }

        return locationList;
    }

    /**
     * Retrieves the nearest grave location to a specified location.
     *
     * @param location the base location.
     * @param grave    the grave to retrieve the location for.
     * @return the nearest grave location.
     */
    public Location getGraveLocation(Location location, Grave grave) {
        if (location == null) return null;
        List<Location> locationList = plugin.getGraveManager().getGraveLocationList(location, grave);

        return !locationList.isEmpty() ? locationList.get(0) : null;
    }

    /**
     * Automatically loots a grave for a player.
     *
     * @param entity   the entity looting the grave.
     * @param location the location of the grave.
     * @param grave    the grave to be looted.
     */
    public void autoLootGrave(Entity entity, Location location, Grave grave) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            Grave.StorageMode storageMode = getStorageMode(plugin.getConfig("storage.mode", grave)
                    .getString("storage.mode"));

            if (storageMode == Grave.StorageMode.EXACT) {
                List<ItemStack> itemStackListLeftOver = new ArrayList<>();
                int counter = 0;
                int inventorySize = player.getInventory().getSize();

                for (ItemStack itemStack : grave.getInventory().getContents()) {
                    if (itemStack != null) {
                        if (player.getInventory().getItem(counter) == null) {
                            if (counter < inventorySize) {
                                player.getInventory().setItem(counter, itemStack);
                                grave.getInventory().remove(itemStack);
                                if (counter == 17 && plugin.getVersionManager().hasSecondHand()) {
                                    player.getInventory().setItem(17, itemStack);
                                    grave.getInventory().remove(itemStack);
                                }
                                if ((counter == 39 && InventoryUtil.isHelmet(itemStack))
                                        || (counter == 38 && InventoryUtil.isChestplate(itemStack))
                                        || (counter == 37 && InventoryUtil.isLeggings(itemStack))
                                        || (counter == 36 && InventoryUtil.isBoots(itemStack))) {
                                    InventoryUtil.playArmorEquipSound(player, itemStack);
                                }
                            } else {
                                itemStackListLeftOver.add(itemStack);
                            }
                        } else {
                            itemStackListLeftOver.add(itemStack);
                        }
                    }

                    counter++;
                }
                grave.getInventory().clear();
                for (ItemStack itemStack : itemStackListLeftOver) {
                    for (Map.Entry<Integer, ItemStack> itemStackEntry : player.getInventory().addItem(itemStack).entrySet()) {
                        grave.getInventory().addItem(itemStackEntry.getValue())
                                .forEach((key, value) -> player.getWorld().dropItem(player.getLocation(), value));
                    }
                }
            } else {
                InventoryUtil.equipArmor(grave.getInventory(), player);
                InventoryUtil.equipItems(grave.getInventory(), player);
            }

            player.updateInventory();
            plugin.getDataManager().updateGrave(grave, "inventory",
                    InventoryUtil.inventoryToString(grave.getInventory()));
            plugin.getEntityManager().runCommands("event.command.open", player, location, grave);

            if (grave.getItemAmount() <= 0) {
                plugin.getEntityManager().runCommands("event.command.loot", player, location, grave);
                plugin.getEntityManager().sendMessage("message.looted", player, location, grave);
                plugin.getEntityManager().playWorldSound("sound.close", location, grave);
                plugin.getEntityManager().spawnZombie(location, player, player, grave);
                giveGraveExperience(player, grave);
                playEffect("effect.loot", location, grave);
                removeGrave(grave);
                closeGrave(grave);
                plugin.debugMessage("Grave " + grave.getUUID() + " autolooted by " + player.getName(), 1);
            } else {
                plugin.getEntityManager().playWorldSound("sound.open", location, grave);
            }
        }
    }

    /**
     * Retrieves the damage reason for a specified damage cause and grave.
     *
     * @param damageCause the cause of the damage.
     * @param grave       the grave associated with the damage.
     * @return the damage reason.
     */
    public String getDamageReason(EntityDamageEvent.DamageCause damageCause, Grave grave) {
        List<String> reasons = plugin.getConfig("message.death-reason", grave).getStringList("message.death-reason");
        String causeName = damageCause.name();

        for (String reason : reasons) {
            if (reason.startsWith(causeName + ":")) {
                return reason.split(":", 2)[1].trim();
            }
        }

        // Fallback if no specific reason is found
        return StringUtil.format(causeName);
    }

    /**
     * Plays an effect at a specified location.
     *
     * @param string   the effect string.
     * @param location the location to play the effect.
     */
    public void playEffect(String string, Location location) {
        playEffect(string, location, null);
    }

    /**
     * Plays an effect at a specified location for a grave.
     *
     * @param string   the effect string.
     * @param location the location to play the effect.
     * @param grave    the grave associated with the effect.
     */
    public void playEffect(String string, Location location, Grave grave) {
        playEffect(string, location, 0, grave);
    }

    /**
     * Plays an effect at a specified location with additional data for a grave.
     *
     * @param string   the effect string.
     * @param location the location to play the effect.
     * @param data     additional data for the effect.
     * @param grave    the grave associated with the effect.
     */
    public void playEffect(String string, Location location, int data, Grave grave) {
        if (location.getWorld() != null) {
            if (grave != null) {
                string = plugin.getConfig(string, grave).getString(string);
            }

            if (string != null && !string.equals("")) {
                try {
                    location.getWorld().playEffect(location, Effect.valueOf(string.toUpperCase()), data);
                } catch (IllegalArgumentException exception) {
                    plugin.debugMessage(string.toUpperCase() + " is not an Effect ENUM", 1);
                }
            }
        }
    }

    /**
     * Checks if an item stack should be ignored based on the entity and grave.
     *
     * @param itemStack the item stack to check.
     * @param entity    the entity.
     * @param grave     the grave.
     * @return true if the item stack should be ignored, false otherwise.
     */
    public boolean shouldIgnoreItemStack(ItemStack itemStack, Entity entity, Grave grave) {
        return shouldIgnoreItemStack(itemStack, entity, grave.getPermissionList());
    }

    /**
     * Checks if an item stack should be ignored based on the entity and permissions.
     *
     * @param itemStack      the item stack to check.
     * @param entity         the entity.
     * @param permissionList the list of permissions.
     * @return true if the item stack should be ignored, false otherwise.
     */
    public boolean shouldIgnoreItemStack(ItemStack itemStack, Entity entity, List<String> permissionList) {
        if (plugin.getConfig("ignore.item.material", entity, permissionList)
                .getStringList("ignore.item.material").contains(itemStack.getType().name())) {
            return true;
        }

        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    for (String string : plugin.getConfig("ignore.item.name", entity, permissionList)
                            .getStringList("ignore.item.name")) {
                        if (!string.equals("")
                                && itemMeta.getDisplayName().equals(StringUtil.parseString(string, plugin))) {
                            return true;
                        }
                    }

                    for (String string : plugin.getConfig("ignore.item.name-contains", entity, permissionList)
                            .getStringList("ignore.item.name-contains")) {
                        if (!string.equals("")
                                && itemMeta.getDisplayName().contains(StringUtil.parseString(string, plugin))) {
                            return true;
                        }
                    }
                }

                if (itemMeta.hasLore() && itemMeta.getLore() != null) {
                    for (String string : plugin.getConfig("ignore.item.lore", entity, permissionList)
                            .getStringList("ignore.item.lore")) {
                        if (!string.equals("")) {
                            for (String lore : itemMeta.getLore()) {
                                if (lore.equals(StringUtil.parseString(string, plugin))) {
                                    return true;
                                }
                            }
                        }
                    }

                    for (String string : plugin.getConfig("ignore.item.lore-contains", entity, permissionList)
                            .getStringList("ignore.item.lore-contains")) {
                        if (!string.equals("")) {
                            for (String lore : itemMeta.getLore()) {
                                if (lore.contains(StringUtil.parseString(string, plugin))) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if there is a grave at the specified location.
     *
     * @param location The location to check.
     * @return True if there is a grave at the location, false otherwise.
     */
    public boolean hasGraveAtLocation(Location location) {
        List<Grave> graves = getAllGraves();
        if (graves != null) {
            for (Grave grave : graves) {
                if (grave.getLocationDeath().equals(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a list of all graves.
     *
     * @return A list of all graves.
     */
    public List<Grave> getAllGraves() {
        // Implement logic to retrieve all graves from your storage system (e.g., database, in-memory cache)
        return null;
    }

    /**
     * @deprecated This method is deprecated.
     * Use {@link #shouldIgnoreBlock(Block, Entity, List<String>)} instead.
     */
    @Deprecated
    public boolean shouldIgnoreBlock(Block block, Entity entity, Grave grave) {
        return shouldIgnoreBlock(block, entity, grave.getPermissionList());
    }

    /**
     * Checks if a block should be ignored based on the entity and permissions.
     *
     * @param block           the block to check.
     * @param entity          the entity.
     * @param permissionList  the list of permissions.
     * @return true if the block should be ignored, false otherwise.
     */
    public boolean shouldIgnoreBlock(Block block, Entity entity, List<String> permissionList) {
        List<String> stringList = plugin.getConfig("ignore.block.material", entity, permissionList)
                .getStringList("ignore.block.material");

        for (String string : stringList) {
            if (!string.equals("") && string.equals(block.getType().name())) {
                return true;
            }
        }

        return false;
    }
}