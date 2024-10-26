package dev.cwhead.GravesX;

import com.mojang.authlib.GameProfile;
import com.ranull.graves.Graves;
import com.ranull.graves.data.*;
import com.ranull.graves.event.GraveBlockPlaceEvent;
import com.ranull.graves.event.GraveCreateEvent;
import com.ranull.graves.event.GraveProtectionCreateEvent;
import com.ranull.graves.manager.*;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * API for managing graves in the GravesX plugin. The GravesXAPI provides methods to create graves for entities
 * and manage grave creation events.
 * <p>
 * Graves are created with various configurations, including equipment, items, experience, protection, and more.
 * The API also handles event triggering when graves are created and ensures data is stored correctly.
 */
public class GravesXAPI {
    private final GravesXAPI instance;

    private final Graves plugin;

    /**
     * Constructor for initializing the GravesXAPI with the main plugin instance.
     *
     * @param plugin The main Graves plugin instance.
     */
    public GravesXAPI(Graves plugin) {
        this.plugin = plugin;
        instance = this;
    }

    /**
     * Creates a grave for an entity with the basic parameters.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, long timeAliveRemaining) {
        createGrave(victim, null, killerEntityType,  null, null, null, 0, timeAliveRemaining, null, false, 0);
    }

    /**
     * Creates a grave for an entity with the basic parameters.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, int experience, long timeAliveRemaining) {
        createGrave(victim, null, killerEntityType,  null, null, null, experience, timeAliveRemaining, null, false, 0);
    }

    /**
     * Creates a grave for an entity with the basic parameters.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining) {
        createGrave(victim, null, killerEntityType,  null, equipmentMap, itemStackList, experience, timeAliveRemaining, null, false, 0);
    }

    /**
     * Creates a grave with additional protection settings.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     * @param graveProtection     Whether the grave is protected.
     * @param graveProtectionTime The time for which the grave remains protected.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining, boolean graveProtection, long graveProtectionTime) {
        createGrave(victim, null, killerEntityType,  null, equipmentMap, itemStackList, experience, timeAliveRemaining, null, graveProtection, graveProtectionTime);
    }

    /**
     * Creates a grave for an entity with a specific storage type.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     * @param damageCause         Damage Caused (nullable).
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining, @Nullable EntityDamageEvent.DamageCause damageCause) {
        createGrave(victim, null, killerEntityType,  null, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, false, 0);
    }

    /**
     * Creates a grave for an entity with a specific storage type and additional protection settings.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     * @param damageCause         Damage Caused (nullable).
     * @param graveProtection     Whether the grave is protected.
     * @param graveProtectionTime The time for which the grave remains protected.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining, @Nullable EntityDamageEvent.DamageCause damageCause, boolean graveProtection, long graveProtectionTime) {
        createGrave(victim, null, killerEntityType,  null, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, graveProtection, graveProtectionTime);
    }

    /**
     * Creates a grave for an entity at a specific location where the victim died.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param locationDeath       The location where the victim died (nullable).
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Location locationDeath, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining, @Nullable EntityDamageEvent.DamageCause damageCause) {
        createGrave(victim, null, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, false, 0);
    }

    /**
     * Creates a grave for an entity at a specific location with protection settings.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param locationDeath       The location where the victim died (nullable).
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     * @param graveProtection     Whether the grave is protected.
     * @param graveProtectionTime The time for which the grave remains protected.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Location locationDeath, @NotNull Map<EquipmentSlot, ItemStack> equipmentMap, @NotNull List<ItemStack> itemStackList, int experience, long timeAliveRemaining, @Nullable EntityDamageEvent.DamageCause damageCause, boolean graveProtection, long graveProtectionTime) {
        createGrave(victim, null, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, damageCause, graveProtection, graveProtectionTime);
    }

    /**
     * Creates a grave for an entity at a specific location without a killer and no storage type.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param locationDeath       The location where the victim died (nullable).
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Location locationDeath, @NotNull Map<EquipmentSlot, ItemStack> equipmentMap, @NotNull List<ItemStack> itemStackList, int experience, long timeAliveRemaining) {
        createGrave(victim, null, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, null, false, 0);
    }

    /**
     * Creates a grave for an entity at a specific location with grave protection, no killer, and no storage type.
     *
     * @param victim              The entity that died.
     * @param killerEntityType    The entity type of the killer.
     * @param locationDeath       The location where the victim died (nullable).
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     * @param graveProtection     Whether the grave is protected.
     * @param graveProtectionTime The time for which the grave remains protected.
     */
    public void createGrave(@NotNull Entity victim, @Nullable EntityType killerEntityType, @Nullable Location locationDeath, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining, boolean graveProtection, long graveProtectionTime) {
        createGrave(victim, null, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, null, graveProtection, graveProtectionTime);
    }

    /**
     * Creates a grave for an entity killed by another entity.
     *
     * @param victim              The entity that died.
     * @param killer              The entity that killed the victim (nullable).
     * @param killerEntityType    The entity type of the killer.
     * @param locationDeath       The location where the victim died (nullable).
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     */
    public void createGrave(@NotNull Entity victim, @Nullable Entity killer, @Nullable EntityType killerEntityType, @Nullable Location locationDeath, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining) {
        createGrave(victim, killer, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, null, false, 0);
    }

    /**
     * Creates a grave for an entity killed by another entity with protection settings.
     *
     * @param victim              The entity that died.
     * @param killer              The entity that killed the victim (nullable).
     * @param killerEntityType    The entity type of the killer.
     * @param locationDeath       The location where the victim died (nullable).
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     * @param graveProtection     Whether the grave is protected.
     * @param graveProtectionTime The time for which the grave remains protected.
     */
    public void createGrave(@NotNull Entity victim, @Nullable Entity killer, @Nullable EntityType killerEntityType, @Nullable Location locationDeath, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining, boolean graveProtection, long graveProtectionTime) {
        createGrave(victim, killer, killerEntityType, locationDeath, equipmentMap, itemStackList, experience, timeAliveRemaining, null, graveProtection, graveProtectionTime);
    }

    /**
     * Main method to create a grave with all available parameters.
     *
     * @param victim              The entity that died.
     * @param killer              The entity that killed the victim (nullable).
     * @param killerEntityType    The entity type of the killer.
     * @param locationDeath       The location where the victim died (nullable).
     * @param equipmentMap        The equipment the victim had at the time of death.
     * @param itemStackList       The list of items the victim had at the time of death.
     * @param experience          The experience the victim had.
     * @param timeAliveRemaining  The remaining time the grave will stay alive.
     * @param damageCause         Damage Caused (nullable).
     * @param graveProtection     Whether the grave is protected.
     * @param graveProtectionTime The time for which the grave remains protected.
     */
    public void createGrave(@NotNull Entity victim, @Nullable Entity killer, @Nullable EntityType killerEntityType, @Nullable Location locationDeath, @Nullable Map<EquipmentSlot, ItemStack> equipmentMap, @Nullable List<ItemStack> itemStackList, int experience, long timeAliveRemaining, @Nullable EntityDamageEvent.DamageCause damageCause, boolean graveProtection, long graveProtectionTime) {
        GraveManager graveManager = plugin.getGraveManager();
        DataManager dataManager = plugin.getDataManager();
        IntegrationManager integrationManager = plugin.getIntegrationManager();
        VersionManager versionManager = plugin.getVersionManager();
        LocationManager locationManager = plugin.getLocationManager();
        EntityManager entityManager = plugin.getEntityManager();
        CacheManager cacheManager = plugin.getCacheManager();

        Map<Location, BlockData.BlockType> locationMap = new HashMap<>();
        Grave grave = graveManager.createGrave(victim, itemStackList);

        grave.setOwnerType(victim.getType());
        grave.setOwnerName(victim.getName());
        grave.setOwnerNameDisplay(victim instanceof Player ? ((Player) victim).getDisplayName() : grave.getOwnerName());
        grave.setOwnerUUID(victim.getUniqueId());
        grave.setOwnerTexture(SkinTextureUtil.getTexture(victim));
        grave.setOwnerTextureSignature(SkinSignatureUtil.getSignature(victim));
        grave.setPermissionList(null);
        grave.setYaw(victim.getLocation().getYaw());
        grave.setPitch(victim.getLocation().getPitch());
        grave.setExperience(experience);
        grave.setTimeAliveRemaining(timeAliveRemaining);
        grave.setTimeCreation(System.currentTimeMillis());
        grave.setTimeAlive(timeAliveRemaining);
        Location finalLocationDeath = locationDeath != null ? locationDeath : locationManager.getSafeGraveLocation((LivingEntity) victim, victim.getLocation(), grave);
        if (killer != null) {
            grave.setKillerType(killerEntityType != null ? killerEntityType : EntityType.PLAYER);
            grave.setKillerName(killer.getName());
            grave.setKillerNameDisplay(killer.getCustomName());
            grave.setKillerUUID(killer.getUniqueId());
        } else {
            grave.setKillerUUID(victim.getUniqueId());
            grave.setKillerType(EntityType.PLAYER);
            EntityDamageEvent.DamageCause finalDamageCause = EntityDamageEvent.DamageCause.valueOf("KILL");
            if (damageCause != null) {
                finalDamageCause = damageCause;
            }
            grave.setKillerName(graveManager.getDamageReason(victim.getLastDamageCause() != null ? victim.getLastDamageCause().getCause() : EntityDamageEvent.DamageCause.valueOf(String.valueOf(finalDamageCause)), grave));
            grave.setKillerNameDisplay(grave.getKillerName());
        }

        if (graveProtection && plugin.getConfig("protection.enabled", grave).getBoolean("protection.enabled")) {
            GraveProtectionCreateEvent graveProtectionCreateEvent = new GraveProtectionCreateEvent(victim, grave);
            plugin.getServer().getPluginManager().callEvent(graveProtectionCreateEvent);
            grave.setProtection(true);
            grave.setTimeProtection(graveProtectionTime != 0 ? graveProtectionTime : plugin.getConfig("protection.time", grave).getInt("protection.time") * 1000L);
        }

        try {
            GraveCreateEvent createGrave = new GraveCreateEvent(victim, grave);
            Bukkit.getPluginManager().callEvent(createGrave);
            locationMap.put(finalLocationDeath, BlockData.BlockType.DEATH);

            cacheManager.getGraveMap().put(grave.getUUID(), grave);
            grave.setLocationDeath(finalLocationDeath);
            grave.setInventory(graveManager.getGraveInventory(grave, (LivingEntity) victim, itemStackList, getRemovedItemStacks((LivingEntity) victim), null));
            grave.setEquipmentMap(equipmentMap != null ? equipmentMap : !versionManager.is_v1_7() ? entityManager.getEquipmentMap((LivingEntity) victim, grave) : new HashMap<>());
            dataManager.addGrave(grave);
            if (integrationManager.hasMultiPaper()) {
                integrationManager.getMultiPaper().notifyGraveCreation(grave);
            }
            placeGraveBlocks(grave, locationMap, (LivingEntity) victim);

            plugin.debugMessage("Creating grave " + grave.getUUID() + " for entity " + victim + " through the GravesX API", 1);

        } catch (Exception e) {
            plugin.getLogger().severe("An error occurred while creating grave " + grave.getUUID() + " for entity " + victim + " through the GravesX API. Cause: " + e.getCause());
            plugin.getLogger().severe("Exception Message: " + e.getMessage());
            plugin.logStackTrace(e);
        }
    }

    /**
     * Retrieves the list of removed item stacks for the specified entity.
     *
     * @param livingEntity The entity whose removed item stacks are to be retrieved.
     * @return The list of removed item stacks.
     */
    private List<ItemStack> getRemovedItemStacks(@NotNull LivingEntity livingEntity) {
        List<ItemStack> removedItemStackList = new ArrayList<>();
        if (plugin.getCacheManager().getRemovedItemStackMap().containsKey(livingEntity.getUniqueId())) {
            removedItemStackList.addAll(plugin.getCacheManager().getRemovedItemStackMap().get(livingEntity.getUniqueId()));
            plugin.getCacheManager().getRemovedItemStackMap().remove(livingEntity.getUniqueId());
        }
        return removedItemStackList;
    }

    private void placeGraveBlocks(@NotNull Grave grave, @NotNull Map<Location, BlockData.BlockType> locationMap, @NotNull LivingEntity livingEntity) {
        for (Map.Entry<Location, BlockData.BlockType> entry : locationMap.entrySet()) {
            Location location = entry.getKey().clone();
            int offsetX = 0;
            int offsetY = 0;
            int offsetZ = 0;
            switch (entry.getValue()) {
                case DEATH:
                    break;
                case NORMAL:
                    offsetX = plugin.getConfig("placement.offset.x", grave).getInt("placement.offset.x");
                    offsetY = plugin.getConfig("placement.offset.y", grave).getInt("placement.offset.y");
                    offsetZ = plugin.getConfig("placement.offset.z", grave).getInt("placement.offset.z");
                    break;
                case GRAVEYARD:
                    offsetX = plugin.getConfig().getInt("settings.graveyard.offset.x");
                    offsetY = plugin.getConfig().getInt("settings.graveyard.offset.y");
                    offsetZ = plugin.getConfig().getInt("settings.graveyard.offset.z");
                    break;
            }
            location.add(offsetX, offsetY, offsetZ);
            GraveBlockPlaceEvent graveBlockPlaceEvent = new GraveBlockPlaceEvent(grave, location, entry.getValue(), entry.getKey().getBlock(), livingEntity);
            plugin.getServer().getPluginManager().callEvent(graveBlockPlaceEvent);
            if (!graveBlockPlaceEvent.isCancelled()) {
                plugin.getGraveManager().placeGrave(graveBlockPlaceEvent.getLocation(), grave);
                plugin.getEntityManager().sendMessage("message.block", livingEntity, location, grave);
                plugin.getEntityManager().runCommands("event.command.block", livingEntity, graveBlockPlaceEvent.getLocation(), grave);
            }
        }
    }

    /**
     * Removes the specified grave from the grave manager.
     *
     * @param grave the grave to be removed
     */
    public void removeGrave(@NotNull Grave grave) {
        GraveManager graveManager = plugin.getGraveManager();
        graveManager.removeGrave(grave);
    }

    /**
     * Breaks the specified grave, triggering its removal and handling any related events.
     *
     * @param grave the grave to be broken
     */
    public void breakGrave(@NotNull Grave grave) {
        GraveManager graveManager = plugin.getGraveManager();
        graveManager.breakGrave(grave);
    }

    /**
     * Breaks the specified grave at a given location.
     *
     * @param location the location where the grave is located
     * @param grave the grave to be broken
     */
    public void breakGrave(@NotNull Location location, @NotNull Grave grave) {
        GraveManager graveManager = plugin.getGraveManager();
        graveManager.breakGrave(location, grave);
    }

    /**
     * Automatically loots the specified grave for the given entity at the given location.
     *
     * @param entity the entity that will loot the grave
     * @param location the location of the grave
     * @param grave the grave to be looted
     */
    public void autoLootGrave(@NotNull Entity entity, @NotNull Location location, @NotNull Grave grave) {
        GraveManager graveManager = plugin.getGraveManager();
        graveManager.autoLootGrave(entity, location, grave);
    }

    /**
     * Marks the specified grave as abandoned, preventing further interaction.
     *
     * @param grave the grave to be abandoned
     */
    public void abandonGrave(@NotNull Grave grave) {
        GraveManager graveManager = plugin.getGraveManager();
        graveManager.abandonGrave(grave);
    }

    /**
     * Drops the items stored in the specified grave at the given location.
     *
     * @param location the location where the items will be dropped
     * @param grave the grave whose items are to be dropped
     */
    public void dropGraveItems(@NotNull Location location, @NotNull Grave grave) {
        GraveManager graveManager = plugin.getGraveManager();
        graveManager.dropGraveItems(location, grave);
    }

    /**
     * Removes the oldest grave associated with the specified living entity.
     *
     * @param livingEntity the entity whose oldest grave will be removed
     */
    public void removeOldestGrave(@NotNull LivingEntity livingEntity) {
        GraveManager graveManager = plugin.getGraveManager();
        graveManager.removeOldestGrave(livingEntity);
    }

    /**
     * Retrieves the BlockData associated with a grave at a given location.
     *
     * @param location       The location of the grave.
     * @param graveUUID      The unique identifier of the grave.
     * @param replaceMaterial The material to replace in the BlockData.
     * @param replaceData    Additional data to apply to the BlockData.
     * @return A BlockData instance representing the grave at the specified location.
     */
    public BlockData getBlockData(@NotNull Location location, @NotNull UUID graveUUID, @NotNull String replaceMaterial, @NotNull String replaceData) {
        return new BlockData(location, graveUUID, replaceMaterial, replaceData);
    }

    /**
     * Retrieves the ChunkData for the chunk containing the specified location.
     *
     * @param location The location for which to retrieve the chunk data.
     * @return A ChunkData instance representing the chunk at the specified location.
     */
    public ChunkData getChunkData(@NotNull Location location) {
        return new ChunkData(location);
    }

    /**
     * Retrieves the EntityData for an entity associated with a grave.
     *
     * @param location     The location of the entity.
     * @param uuidEntity   The unique identifier of the entity.
     * @param uuidGrave    The unique identifier of the grave.
     * @param type         The type of the entity.
     * @return An EntityData instance representing the entity associated with the grave.
     */
    public EntityData getEntityData(@NotNull Location location, @NotNull UUID uuidEntity, @NotNull UUID uuidGrave, @NotNull EntityData.Type type) {
        return new EntityData(location, uuidEntity, uuidGrave, type);
    }

    /**
     * Retrieves the HologramData for a hologram associated with a grave.
     *
     * @param location    The location of the hologram.
     * @param uuidEntity  The unique identifier of the entity associated with the hologram.
     * @param uuidGrave   The unique identifier of the grave.
     * @param line        The line number of the hologram to retrieve.
     * @return A HologramData instance representing the hologram associated with the grave.
     */
    public HologramData getHologramData(@NotNull Location location, @NotNull UUID uuidEntity, @NotNull UUID uuidGrave, int line) {
        return new HologramData(location, uuidEntity, uuidGrave, line);
    }

    /**
     * Retrieves the LocationData for a given location.
     *
     * @param location The location for which to retrieve data.
     * @return A LocationData instance representing the specified location.
     */
    public LocationData getLocationData(@NotNull Location location) {
        return new LocationData(location);
    }

    /**
     * Simplifies a given BlockFace to one of the four cardinal directions (NORTH, EAST, SOUTH, WEST).
     *
     * @param face The BlockFace to simplify.
     * @return The simplified BlockFace.
     */
    public BlockFace simplifyBlockFace(@NotNull BlockFace face) {
        return BlockFaceUtil.getSimpleBlockFace(face);
    }

    /**
     * Retrieves the Rotation corresponding to a given BlockFace.
     *
     * @param face The BlockFace for which to retrieve the rotation.
     * @return The corresponding Rotation for the specified BlockFace.
     */
    public Rotation getRotationFromBlockFace(@NotNull BlockFace face) {
        return BlockFaceUtil.getBlockFaceRotation(face);
    }

    /**
     * Encodes an object to a Base64 string using Base64Util.
     *
     * @param object The object to encode.
     * @return The Base64 encoded string, or null if encoding fails.
     */
    public String encodeObjectToBase64(@NotNull Object object) {
        return Base64Util.objectToBase64(object);
    }

    /**
     * Decodes a Base64 string to an object using Base64Util.
     *
     * @param base64String The Base64 string to decode.
     * @return The decoded object, or null if decoding fails.
     */
    public Object decodeBase64ToObject(@NotNull String base64String) {
        return Base64Util.base64ToObject(base64String);
    }

    /**
     * Loads a class with the specified name using ClassUtil.
     *
     * @param className The fully qualified name of the class to be loaded.
     */
    public void loadClass(@NotNull String className) {
        ClassUtil.loadClass(className);
    }

    /**
     * Gets the Color corresponding to the given color name using ColorUtil.
     *
     * @param colorName The name of the color as a string.
     * @return The Color corresponding to the given name, or null if no match is found.
     */
    public Color getColor(@NotNull String colorName) {
        return ColorUtil.getColor(colorName);
    }

    /**
     * Parses a hex color code to a Color using ColorUtil.
     *
     * @param hex The hex color code as a string (e.g., "#FF5733").
     * @return The Color corresponding to the hex color code, or null if the code is invalid.
     */
    public Color getColorFromHex(@NotNull String hex) {
        return ColorUtil.getColorFromHex(hex);
    }

    /**
     * Creates a Particle.DustOptions object using a hex color code.
     *
     * @param hexColor The hex color code as a string (e.g., "#FF5733").
     * @param size The size of the dust particle.
     * @return A Particle.DustOptions object with the specified color and size, or null if the color code is invalid.
     */
    public Particle.DustOptions createDustOptionsFromHex(@NotNull String hexColor, float size) {
        return ColorUtil.createDustOptionsFromHex(hexColor, size);
    }

    /**
     * Checks if the specified entity has the given permission using EntityUtil.
     *
     * @param entity     The entity to check.
     * @param permission The permission to check for.
     * @return {@code true} if the entity has the specified permission,
     *         {@code true} if the method is not found,
     *         or {@code false} if an exception occurs.
     */
    public boolean hasPermission(@NotNull Entity entity, @NotNull String permission) {
        return EntityUtil.hasPermission(entity, permission);
    }

    /**
     * Gets the total experience of the specified player using ExperienceUtil.
     *
     * @param player The player to get the experience from.
     * @return The total experience of the player.
     */
    public int getPlayerExperience(@NotNull Player player) {
        return ExperienceUtil.getPlayerExperience(player);
    }

    /**
     * Gets the experience required to reach a specific level using ExperienceUtil.
     *
     * @param level The level to get the experience for.
     * @return The experience required to reach the specified level.
     */
    public int getExperienceAtLevel(int level) {
        return ExperienceUtil.getExperienceAtLevel(level);
    }

    /**
     * Calculates the level from a given amount of experience using ExperienceUtil.
     *
     * @param experience The experience to calculate the level from.
     * @return The level corresponding to the given experience.
     */
    public long getLevelFromExperience(long experience) {
        return ExperienceUtil.getLevelFromExperience(experience);
    }

    /**
     * Calculates the drop percentage of experience using ExperienceUtil.
     *
     * @param experience The total experience.
     * @param percent    The percentage to drop.
     * @return The experience drop amount.
     */
    public int getDropPercent(int experience, float percent) {
        return ExperienceUtil.getDropPercent(experience, percent);
    }

    /**
     * @deprecated
     * <p>
     * This method is deprecated and will be removed in a future version.
     * Use {@link #getLevelFromExperience(long)} instead.
     * </p>
     *
     * Gets the amount of experience a player will drop upon death based on a percentage.
     *
     * @param player          The player to get the drop experience from.
     * @param expStorePercent The percentage of experience to drop.
     * @return The amount of experience to drop.
     */
    @Deprecated
    public int getPlayerDropExperience(@NotNull Player player, float expStorePercent) {
        return ExperienceUtil.getPlayerDropExperience(player, expStorePercent);
    }

    /**
     * Moves a file to a new location with a new name using FileUtil.
     *
     * @param file The file to be moved.
     * @param name The new name for the file.
     */
    public void moveFile(@NotNull File file, @NotNull String name) {
        FileUtil.moveFile(file, name);
    }

    /**
     * @deprecated
     * <p>
     * This method is deprecated and will be removed in a future version.
     * Use {@link #moveFile(File, String)} instead.
     * </p>
     *
     * Copies a file to a new location with a new name using FileUtil.
     *
     * @param file The file to be copied.
     * @param name The new name for the copied file.
     */
    @Deprecated
    public void copyFile(@NotNull File file, @NotNull String name) {
        FileUtil.copyFile(file, name);
    }

    /**
     * Gets the appropriate inventory size based on the given size.
     *
     * @param size The size to be used for determining the inventory size.
     * @return The appropriate inventory size.
     */
    public int getInventorySize(int size) {
        return InventoryUtil.getInventorySize(size);
    }

    /**
     * Equips the player's armor from the given inventory.
     *
     * @param inventory The inventory containing the armor items.
     * @param player    The player to be equipped with armor.
     */
    public void equipArmor(@NotNull Inventory inventory, @NotNull Player player) {
        InventoryUtil.equipArmor(inventory, player);
    }

    /**
     * Equips the player's inventory items from the given inventory.
     *
     * @param inventory The inventory containing the items.
     * @param player    The player to be equipped with items.
     */
    public void equipItems(@NotNull Inventory inventory, @NotNull Player player) {
        InventoryUtil.equipItems(inventory, player);
    }

    /**
     * Converts the given inventory to a string representation.
     *
     * @param inventory The inventory to be converted.
     * @return The string representation of the inventory.
     */
    public String inventoryToString(@NotNull Inventory inventory) {
        return InventoryUtil.inventoryToString(inventory);
    }

    /**
     * Converts a string representation of an inventory to an Inventory object.
     *
     * @param inventoryHolder The inventory holder.
     * @param string          The string representation of the inventory.
     * @param title           The title of the inventory.
     * @return The Inventory object.
     */
    public Inventory stringToInventory(@NotNull InventoryHolder inventoryHolder, @NotNull String string, @NotNull String title) {
        return InventoryUtil.stringToInventory(inventoryHolder, string, title, plugin);
    }

    public LibraryLoaderUtil getLibraryLoaderUtil() {
        return new LibraryLoaderUtil(plugin);
    }

    /**
     * Rounds the given location's coordinates to the nearest whole numbers.
     *
     * @param location The location to be rounded.
     * @return A new location with rounded coordinates.
     */
    public Location roundLocation(@NotNull Location location) {
        return LocationUtil.roundLocation(location);
    }

    /**
     * Converts a Location object to a string representation.
     *
     * @param location The location to be converted.
     * @return A string representation of the location in the format "world|x|y|z".
     */
    public String locationToString(@NotNull Location location) {
        return LocationUtil.locationToString(location);
    }

    /**
     * Converts a chunk's location to a string representation.
     *
     * @param location The location within the chunk.
     * @return A string representation of the chunk in the format "world|chunkX|chunkZ".
     */
    public String chunkToString(@NotNull Location location) {
        return LocationUtil.chunkToString(location);
    }

    /**
     * Converts a chunk string representation back to a Location object.
     *
     * @param string The string representation of the chunk in the format "world|chunkX|chunkZ".
     * @return A Location object representing the chunk.
     */
    public Location chunkStringToLocation(@NotNull String string) {
        return LocationUtil.chunkStringToLocation(string);
    }

    /**
     * Converts a string representation of a location back to a Location object.
     *
     * @param string The string representation of the location in the format "world|x|y|z".
     * @return A Location object.
     */
    public Location stringToLocation(@NotNull String string) {
        return LocationUtil.stringToLocation(string);
    }

    /**
     * Finds the closest location to a given base location from a list of locations.
     *
     * @param locationBase The base location to compare against.
     * @param locationList The list of locations to search through.
     * @return The closest location to the base location, or null if the list is empty.
     */
    public Location getClosestLocation(@NotNull Location locationBase, @NotNull List<Location> locationList) {
        return LocationUtil.getClosestLocation(locationBase, locationList);
    }

    /**
     * Checks if the given material is an air block.
     *
     * @param material The material to check.
     * @return True if the material is air, false otherwise.
     */
    public boolean isAir(@NotNull Material material) {
        return MaterialUtil.isAir(material);
    }

    /**
     * Checks if the given material is lava.
     *
     * @param material The material to check.
     * @return True if the material is lava, false otherwise.
     */
    public boolean isLava(@NotNull Material material) {
        return MaterialUtil.isLava(material);
    }

    /**
     * Checks if the given material is not solid and is safe (i.e., not lava).
     *
     * @param material The material to check.
     * @return True if the material is not solid and safe, false otherwise.
     */
    public boolean isSafeNotSolid(@NotNull Material material) {
        return MaterialUtil.isSafeNotSolid(material);
    }

    /**
     * Checks if the given material is solid and safe (i.e., not lava).
     *
     * @param material The material to check.
     * @return True if the material is solid and safe, false otherwise.
     */
    public boolean isSafeSolid(@NotNull Material material) {
        return MaterialUtil.isSafeSolid(material);
    }

    /**
     * Checks if the given material is water.
     *
     * @param material The material to check.
     * @return True if the material is water, false otherwise.
     */
    public boolean isWater(@NotNull Material material) {
        return MaterialUtil.isWater(material);
    }

    /**
     * Checks if the given material is a player head.
     *
     * @param material The material to check.
     * @return True if the material is a player head, false otherwise.
     */
    public boolean isPlayerHead(@NotNull Material material) {
        return MaterialUtil.isPlayerHead(material);
    }

    /**
     * Checks if the given material is a player head.
     *
     * @param material The material to check via string.
     * @return True if the material is a player head, false otherwise.
     */
    public boolean isPlayerHead(@NotNull String material) {
        return MaterialUtil.isPlayerHead(material);
    }

    /**
     * Posts the given log content to mclo.gs and returns the URL of the posted log.
     *
     * @param content The log content to be posted.
     * @return The URL of the posted log, or null if the post was unsuccessful.
     */
    public String postLog(@NotNull String content) {
        return MclogsUtil.postLogToMclogs(content);
    }

    /**
     * Gets the highest integer value associated with a specific permission prefix for the player.
     *
     * @param player     The player whose permissions are being checked.
     * @param permission The permission prefix to search for.
     * @return The highest integer value found for the specified permission prefix. Returns 0 if no such permission is found.
     */
    public int getHighestInt(@NotNull Player player, @Nullable String permission) {
        return PermissionUtil.getHighestInt(player, permission);
    }

    /**
     * Gets the highest double value associated with a specific permission prefix for the player.
     *
     * @param player     The player whose permissions are being checked.
     * @param permission The permission prefix to search for.
     * @return The highest double value found for the specified permission prefix. Returns 0 if no such permission is found.
     */
    public double getHighestDouble(@NotNull Player player, String permission) {
        return PermissionUtil.getHighestDouble(player, permission);
    }

    /**
     * Triggers the main hand swing animation for the specified player.
     *
     * @param player The player whose main hand swing animation is to be triggered.
     */
    public void swingMainHand(@NotNull Player player) {
        ReflectionUtil.swingMainHand(player);
    }

    /**
     * Copies resources from the plugin's JAR file to the specified output path.
     *
     * @param inputPath  The path inside the JAR file to copy from.
     * @param outputPath The path on the file system to copy to.
     */
    public void copyResources(@NotNull String inputPath, @NotNull String outputPath) {
        ResourceUtil.copyResources(inputPath, outputPath, plugin);
    }

    /**
     * Copies resources from the plugin's JAR file to the specified output path, with an option to overwrite existing files.
     *
     * @param inputPath  The path inside the JAR file to copy from.
     * @param outputPath The path on the file system to copy to.
     * @param overwrite  Whether to overwrite existing files.
     */
    public void copyResources(@NotNull String inputPath, @NotNull String outputPath, boolean overwrite) {
        ResourceUtil.copyResources(inputPath, outputPath, overwrite, plugin);
    }

    /**
     * Gets the skin signature of the specified entity if it is a player.
     *
     * @param entity The entity whose skin signature is to be retrieved.
     * @return The skin signature of the player, or null if the entity is not a player or the signature could not be retrieved.
     */
    public String getSkinSignature(@NotNull Entity entity) {
        return SkinSignatureUtil.getSignature(entity);
    }

    /**
     * Sets the texture of a Skull block.
     *
     * @param skull  The Skull block.
     * @param name   The name associated with the texture.
     * @param base64 The Base64 encoded texture.
     */
    public void setSkullTexture(@NotNull Skull skull, @NotNull String name, @NotNull String base64) {
        SkinTextureUtil.setSkullBlockTexture(skull, name, base64);
    }

    /**
     * Sets the texture of a Skull item stack.
     *
     * @param skullMeta The SkullMeta item meta.
     * @param name      The name associated with the texture.
     * @param base64    The Base64 encoded texture.
     */
    public void setSkullTexture(@NotNull SkullMeta skullMeta, @NotNull String name, @NotNull String base64) {
        SkinTextureUtil.setSkullBlockTexture(skullMeta, name, base64);
    }

    /**
     * Retrieves the texture of the specified entity.
     *
     * @param entity The entity from which to get the texture.
     * @return The Base64 encoded texture string, or null if not found.
     */
    public String getTexture(@NotNull Entity entity) {
        return SkinTextureUtil.getTexture(entity);
    }

    /**
     * Retrieves the GameProfile of the specified player.
     *
     * @param player The player from which to get the GameProfile.
     * @return The GameProfile of the player, or null if not found.
     */
    public GameProfile getPlayerGameProfile(@NotNull Player player) {
        return SkinTextureUtil.getPlayerGameProfile(player);
    }

    /**
     * Converts a string to a UUID.
     *
     * @param string The string to convert to a UUID.
     * @return The UUID if the string is a valid UUID format, otherwise null.
     */
    public UUID getUUID(@NotNull String string) {
        return UUIDUtil.getUUID(string);
    }

    /**
     * Gets the latest version of a resource from SpigotMC.
     *
     * @param resourceId The ID of the resource on SpigotMC.
     * @return The latest version of the resource as a String, or null if an error occurs.
     */
    public String getLatestVersion(int resourceId) {
        return UpdateUtil.getLatestVersion(resourceId);
    }

    /**
     * Checks if a given file is a valid YAML file.
     *
     * @param file The file to check.
     * @return True if the file is a valid YAML file, otherwise false.
     */
    public boolean isValidYAML(@NotNull File file) {
        return YAMLUtil.isValidYAML(file);
    }

    /**
     * @deprecated Use {@link #isGrave(Grave, Location)} instead for precise location checking.
     * This code is added for debugging purposes.
     *
     * Checks if the specified location is a grave's location.
     *
     * @param grave the grave to check. This always returns true for the provided grave's death location.
     *              For more precise checking, use {@link #isGrave(Grave, Location)} with a specific location.
     * @return true if the location matches the grave's death location, false otherwise.
     */
    @Deprecated
    public boolean isGrave(@NotNull Grave grave) {
        return isGrave(grave, grave.getLocationDeath());
    }

    /**
     * Checks if a given location matches the death location of a specific grave.
     *
     * @param grave the grave to check
     * @param location the location to compare with the grave's death location
     * @return true if the location matches the grave's death location, false otherwise.
     */
    public boolean isGrave(@NotNull Grave grave, @NotNull Location location) {
        return location.equals(grave.getLocationDeath());
    }

    /**
     * Returns the total number of graves for all players.
     * <p>
     * This method calls {@link #getGraveAmount(Player)} with a {@code null} argument
     * to count graves without filtering by any specific player.
     *
     * @return the total count of graves for all players.
     */
    public long getGraveAmount() {
        return getGraveAmount(null);
    }

    /**
     * Returns the number of graves associated with a specified player.
     * <p>
     * If {@code targetPlayer} is provided, only graves owned by this player will be counted.
     * If {@code targetPlayer} is {@code null}, all graves are counted.
     *
     * @param targetPlayer the player whose graves should be counted; if {@code null},
     *                     counts graves for all players.
     * @return the number of graves associated with {@code targetPlayer}, or the total
     *         count of all graves if {@code targetPlayer} is {@code null}.
     */
    public long getGraveAmount(@Nullable Player targetPlayer) {
        List<Grave> graveList = new ArrayList<>(plugin.getCacheManager().getGraveMap().values());
        UUID playerUUID = null;
        if (targetPlayer != null) {
            playerUUID = targetPlayer.getUniqueId();
        }
        long graveCount = 0;

        for (Grave grave : graveList) {
            if (playerUUID != null) {
                if (grave.getOwnerUUID().equals(playerUUID)) {
                    graveCount++; // Filter to the current player
                }
            } else {
                graveCount++; // Count all graves
            }
        }
        return graveCount;
    }

    /**
     * Gets the instance of the GravesXAPI.
     *
     * @return The instance of the API.
     */
    public GravesXAPI getInstance() {
        return instance;
    }

    /**
     * Registers the API as an event listener in the plugin manager.
     */
    public void register() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents((Listener) this, plugin);
    }
}