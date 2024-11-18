package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.event.*;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Listener for handling entity death events and creating graves.
 */
public class EntityDeathListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs an EntityDeathListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public EntityDeathListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the EntityDeathEvent to create a grave based on various conditions.
     *
     * @param event The EntityDeathEvent to handle.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) throws InvocationTargetException {
        LivingEntity livingEntity = event.getEntity();
        String entityName = plugin.getEntityManager().getEntityName(livingEntity);
        Location location = LocationUtil.roundLocation(livingEntity.getLocation());
        List<String> permissionList = livingEntity instanceof Player ? plugin.getPermissionList(livingEntity) : null;
        List<String> worldList = plugin.getConfig("world", livingEntity, permissionList).getStringList("world");
        List<ItemStack> removedItemStackList = getRemovedItemStacks(livingEntity);

        if (isInvalidMohistDeath(event) || isInvalidGraveZombie(event, livingEntity, entityName)) return;

        if (livingEntity instanceof Player) {
            if (handlePlayerDeath((Player) livingEntity, entityName)) return;
        }

        if (livingEntity instanceof Zombie) {
            Zombie zombie = (Zombie) livingEntity;

            if (isConfiguredZombieType(zombie) && hasGravesXMetadata(zombie)) {
                removePlayerSkullFromDrops(zombie, event);
            }
        }

        if (!isEnabledGrave(livingEntity, permissionList, entityName)) return;

        if (isKeepInventory((PlayerDeathEvent) event, entityName)) return;

        if (event.getDrops().isEmpty()) {
            plugin.debugMessage("Grave not created for " + entityName + " because they had an empty inventory", 2);
            return;
        }

        if (isInvalidCreatureSpawn(livingEntity, permissionList, entityName)) return;

        if (!isValidWorld(worldList, livingEntity, entityName)) return;

        if (plugin.getGraveManager().shouldIgnoreBlock(location.getBlock(), livingEntity, permissionList)) {
            plugin.getEntityManager().sendMessage("message.ignore", livingEntity, StringUtil.format(location.getBlock().getType().name()), location, permissionList);
            return;
        }

        if (!isValidDamageCause(livingEntity, permissionList, entityName)) return;

        Player player = ((PlayerDeathEvent) event).getEntity().getPlayer();

        // Retrieve the server-configured maximum graves
        int serverMaxGraves = plugin.getConfig("grave.max", livingEntity, permissionList).getInt("grave.max");

        // Retrieve the permission-based maximum graves
        int maxGravesPermission = getMaxGravesPermission(player);

        // Determine the applicable limit
        // Use the permission-based limit if it's available; otherwise, fall back to the server-configured limit
        int applicableMaxGraves = (maxGravesPermission > 0) ? maxGravesPermission : serverMaxGraves;

        // Check if the player has reached the applicable grave limit
        if (plugin.getGraveManager().getGraveList(livingEntity).size() >= applicableMaxGraves) {
            if (plugin.hasGrantedPermission("graves.max.replace", player) && plugin
                    .getConfig("grave.replace-oldest", livingEntity, permissionList).getBoolean("grave.replace-oldest")) {
                plugin.getGraveManager().removeOldestGrave(livingEntity);
                plugin.getEntityManager().sendMessage("message.grave-oldest-replaced", livingEntity, livingEntity.getLocation(),
                        permissionList);
                plugin.debugMessage("Grave replaced oldest for " + entityName + " because they reached maximum graves", 2);
            } else if (plugin.hasGrantedPermission("graves.max.bypass", player)) {
                plugin.debugMessage("Grave created for " + entityName + " even though they reached the maximum graves cap", 2);
            } else {
                plugin.getEntityManager().sendMessage("message.max", livingEntity, livingEntity.getLocation(),
                        permissionList);
                plugin.debugMessage("Grave not created for " + entityName + " because they reached maximum graves", 2);
                return;
            }
        }

        if (!hasValidToken(livingEntity, permissionList, entityName, event.getDrops())) return;

        List<ItemStack> graveItemStackList = getGraveItemStackList(event, livingEntity, permissionList);

        if (!graveItemStackList.isEmpty()) {
            createGrave(event, livingEntity, entityName, permissionList, removedItemStackList, graveItemStackList, location);
        } else {
            plugin.debugMessage("Grave not created for " + entityName + " because they had no drops", 2);
        }
    }

    /**
     * Retrieves the maximum number of graves a player is allowed to have based on their permissions.
     * <p>
     * The method checks for permissions related to grave limits and returns the highest limit found. If the player
     * has the "grave.max.limit.unlimited" permission, the method will return {@code Integer.MAX_VALUE} indicating
     * that the player has no limit on the number of graves. If no specific permissions are found, the method returns
     * {@code 0} by default, which should be interpreted as no specific limit set by permissions.
     * </p>
     *
     * @param player The player whose grave limit is being checked.
     * @return The maximum number of graves the player is allowed to have. Returns {@code Integer.MAX_VALUE} for
     *         unlimited graves, or {@code 0} if no specific limit is set by permissions.
     */
    private int getMaxGravesPermission(Player player) {
        int maxGraves = 0; // Default to 0, indicating no specific limit set

        // Retrieve all permissions related to grave limits
        for (int i = 0; i <= 10; i++) { // Adjust the range as needed
            String permission = "grave.max.limit." + i;
            if (plugin.hasGrantedPermission("grave.max.limit.unlimited", player)) {
                return Integer.MAX_VALUE; // Player has unlimited graves
            } else if (plugin.hasGrantedPermission(permission, player)) {
                maxGraves = i;
            }
        }

        return maxGraves;
    }

    /**
     * Checks if the zombie is of the type configured in config.yml.
     *
     * @param zombie the zombie entity to check
     * @return true if the zombie is of the configured type, false otherwise
     */
    private boolean isConfiguredZombieType(Zombie zombie) {
        String configuredZombieType = plugin.getConfig().getString("zombie.type", "ZOMBIE").toUpperCase();
        EntityType configuredEntityType = EntityType.valueOf(configuredZombieType);
        return zombie.getType() == configuredEntityType;
    }

    /**
     * Removes player skull from the drops of the entity if it is wearing one.
     *
     * @param entity the entity whose drops are to be modified
     * @param event the EntityDeathEvent containing the drops
     */
    private void removePlayerSkullFromDrops(LivingEntity entity, EntityDeathEvent event) {
        ItemStack helmet = entity.getEquipment().getHelmet();
        if (helmet != null && helmet.getType() == Material.PLAYER_HEAD) {
            event.getDrops().removeIf(item -> item.getType() == Material.PLAYER_HEAD);
        }
    }

    /**
     * Checks if the given entity has the GravesX metadata.
     *
     * @param zombie the zombie entity to check
     * @return true if the entity has the GravesX metadata, false otherwise
     */
    private boolean hasGravesXMetadata(Zombie zombie) {
        for (MetadataValue value : zombie.getMetadata("GravesX")) {
            if (value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the list of removed item stacks for the specified entity.
     *
     * @param livingEntity The entity whose removed item stacks are to be retrieved.
     * @return The list of removed item stacks.
     */
    private List<ItemStack> getRemovedItemStacks(LivingEntity livingEntity) {
        List<ItemStack> removedItemStackList = new ArrayList<>();
        if (plugin.getCacheManager().getRemovedItemStackMap().containsKey(livingEntity.getUniqueId())) {
            removedItemStackList.addAll(plugin.getCacheManager().getRemovedItemStackMap().get(livingEntity.getUniqueId()));
            plugin.getCacheManager().getRemovedItemStackMap().remove(livingEntity.getUniqueId());
        }
        return removedItemStackList;
    }

    /**
     * Checks if the entity death event is an invalid Mohist death.
     *
     * @param event The entity death event to check.
     * @return True if the event is an invalid Mohist death, false otherwise.
     */
    private boolean isInvalidMohistDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && !(event instanceof PlayerDeathEvent)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            return true;
        }
        return false;
    }

    /**
     * Checks if the entity is an invalid grave zombie.
     *
     * @param event       The entity death event.
     * @param livingEntity The entity to check.
     * @param entityName   The name of the entity.
     * @return True if the entity is an invalid grave zombie, false otherwise.
     */
    private boolean isInvalidGraveZombie(EntityDeathEvent event, LivingEntity livingEntity, String entityName) {
        if (plugin.getEntityManager().hasDataByte(livingEntity, "graveZombie")) {
            EntityType zombieGraveEntityType = plugin.getEntityManager().hasDataString(livingEntity, "graveEntityType") ? EntityType.valueOf(plugin.getEntityManager().getDataString(livingEntity, "graveEntityType")) : EntityType.PLAYER;
            List<String> zombieGravePermissionList = plugin.getEntityManager().hasDataString(livingEntity, "gravePermissionList") ? Arrays.asList(plugin.getEntityManager().getDataString(livingEntity, "gravePermissionList").split("\\|")) : null;
            if (!plugin.getConfig("zombie.drop", zombieGraveEntityType, zombieGravePermissionList).getBoolean("zombie.drop")) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
            return true;
        }
        return false;
    }

    /**
     * Handles player death and checks if a grave should be created.
     *
     * @param player      The player who died.
     * @param entityName  The name of the player.
     * @return True if a grave should not be created, false otherwise.
     */
    private boolean handlePlayerDeath(Player player, String entityName) throws InvocationTargetException {
        if (!plugin.hasGrantedPermission("graves.place", player.getPlayer())) {
            plugin.debugMessage("Grave not created for " + entityName + " because they don't have permission to place graves", 2);
            return true;
        } else if (plugin.hasGrantedPermission("essentials.keepinv", player.getPlayer())) {
            plugin.debugMessage(entityName + " has essentials.keepinv", 2);
        }
        return false;
    }

    /**
     * Checks if graves are enabled for the specified entity.
     *
     * @param livingEntity The entity to check.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @return True if graves are enabled, false otherwise.
     */
    private boolean isEnabledGrave(LivingEntity livingEntity, List<String> permissionList, String entityName) {
        if (!plugin.getConfig("grave.enabled", livingEntity, permissionList).getBoolean("grave.enabled")) {
            if (livingEntity instanceof Player) {
                plugin.debugMessage("Grave not created for " + entityName + " because they have graves disabled", 2);
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if the player has keep inventory enabled.
     *
     * @param event      The player death event.
     * @param entityName The name of the player.
     * @return True if the player has keep inventory enabled, false otherwise.
     */
    private boolean isKeepInventory(PlayerDeathEvent event, String entityName) {
        try {
            if (event.getKeepInventory()) {
                plugin.debugMessage("Grave not created for " + entityName + " because they had keep inventory", 2);
                return true;
            }
        } catch (NoSuchMethodError ignored) {
        }
        return false;
    }

    /**
     * Checks if the creature spawn reason is valid.
     *
     * @param livingEntity  The creature entity.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @return True if the spawn reason is invalid, false otherwise.
     */
    private boolean isInvalidCreatureSpawn(LivingEntity livingEntity, List<String> permissionList, String entityName) {
        if (livingEntity instanceof Creature) {
            List<String> spawnReasonList = plugin.getConfig("spawn.reason", livingEntity, permissionList).getStringList("spawn.reason");
            if (plugin.getEntityManager().hasDataString(livingEntity, "spawnReason") && (!spawnReasonList.contains("ALL") && !spawnReasonList.contains(plugin.getEntityManager().getDataString(livingEntity, "spawnReason")))) {
                plugin.debugMessage("Grave not created for " + entityName + " because they had an invalid spawn reason", 2);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the entity is in a valid world.
     *
     * @param worldList    The list of valid worlds.
     * @param livingEntity The entity to check.
     * @param entityName   The name of the entity.
     * @return True if the entity is in a valid world, false otherwise.
     */
    private boolean isValidWorld(List<String> worldList, LivingEntity livingEntity, String entityName) {
        if (!worldList.contains("ALL") && !worldList.contains(livingEntity.getWorld().getName())) {
            plugin.debugMessage("Grave not created for " + entityName + " because they are not in a valid world", 2);
            return false;
        }
        return true;
    }


    /**
     * Checks if the damage cause is valid for creating a grave.
     *
     * @param livingEntity  The entity that was damaged.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @return True if the damage cause is valid, false otherwise.
     */private boolean isValidDamageCause(LivingEntity livingEntity, List<String> permissionList, String entityName) {
        if (livingEntity.getLastDamageCause() != null) {
            EntityDamageEvent.DamageCause damageCause = livingEntity.getLastDamageCause().getCause();
            List<String> damageCauseList = plugin.getConfig("death.reason", livingEntity, permissionList).getStringList("death.reason");
            if (!damageCauseList.contains("ALL") && !damageCauseList.contains(damageCause.name()) && (damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && ((livingEntity.getKiller() != null && !plugin.getConfig("death.player", livingEntity, permissionList).getBoolean("death.player")) || (livingEntity.getKiller() == null && !plugin.getConfig("death.entity", livingEntity, permissionList).getBoolean("death.entity"))) || (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && !plugin.getConfig("death.environmental", livingEntity, permissionList).getBoolean("death.environmental")))) {
                plugin.debugMessage("Grave not created for " + entityName + " because they died to an invalid damage cause", 2);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the entity has a valid grave token.
     *
     * @param livingEntity  The entity to check.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @param drops         The list of item drops.
     * @return True if the entity has a valid grave token, false otherwise.
     */
    private boolean hasValidToken(LivingEntity livingEntity, List<String> permissionList, String entityName, List<ItemStack> drops) {
        if (plugin.getVersionManager().hasPersistentData() && plugin.getConfig("token.enabled", livingEntity, permissionList).getBoolean("token.enabled")) {
            String name = plugin.getConfig("token.name", livingEntity).getString("token.name", "basic");
            if (plugin.getConfig().isConfigurationSection("settings.token." + name)) {
                ItemStack itemStack = plugin.getRecipeManager().getGraveTokenFromPlayer(name, drops);
                if (itemStack != null) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    plugin.getEntityManager().sendMessage("message.no-token", livingEntity, livingEntity.getLocation(), permissionList);
                    plugin.debugMessage("Grave not created for " + entityName + " because they did not have a grave token", 2);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retrieves the list of item stacks for the grave.
     *
     * @param event         The entity death event.
     * @param livingEntity  The entity that died.
     * @param permissionList The list of permissions.
     * @return The list of item stacks for the grave.
     */
    private List<ItemStack> getGraveItemStackList(EntityDeathEvent event, LivingEntity livingEntity, List<String> permissionList) {
        List<ItemStack> graveItemStackList = new ArrayList<>();
        List<ItemStack> eventItemStackList = new ArrayList<>(event.getDrops());
        List<ItemStack> dropItemStackList = new ArrayList<>(eventItemStackList);
        Iterator<ItemStack> dropItemStackListIterator = dropItemStackList.iterator();

        try {
            while (dropItemStackListIterator.hasNext()) {
                ItemStack itemStack = dropItemStackListIterator.next();
                if (itemStack != null) {
                    if (plugin.getEntityManager().getGraveUUIDFromItemStack(itemStack) != null) {
                        if (plugin.getConfig("compass.destroy", livingEntity, permissionList).getBoolean("compass.destroy")) {
                            dropItemStackListIterator.remove();
                            event.getDrops().remove(itemStack);
                            continue;
                        } else if (plugin.getConfig("compass.ignore", livingEntity, permissionList).getBoolean("compass.ignore")) {
                            continue;
                        }
                    }
                    if (!plugin.getGraveManager().shouldIgnoreItemStack(itemStack, livingEntity, permissionList)) {
                        graveItemStackList.add(itemStack);
                        dropItemStackListIterator.remove();
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // End the loop if the exception occurs
        }

        return graveItemStackList;
    }

    /**
     * Creates a grave for the specified entity.
     *
     * @param event              The entity death event.
     * @param livingEntity       The entity that died.
     * @param entityName         The name of the entity.
     * @param permissionList     The list of permissions.
     * @param removedItemStackList The list of removed item stacks.
     * @param graveItemStackList The list of item stacks for the grave.
     * @param location           The location of the grave.
     */
    private void createGrave(EntityDeathEvent event, LivingEntity livingEntity, String entityName, List<String> permissionList, List<ItemStack> removedItemStackList, List<ItemStack> graveItemStackList, Location location) {
        Grave grave = new Grave(UUID.randomUUID());
        setupGrave(grave, livingEntity, entityName, permissionList);
        setGraveExperience(grave, event, livingEntity);
        setupGraveKiller(grave, livingEntity);
        setupGraveProtection(livingEntity, grave);
        GraveCreateEvent graveCreateEvent = new GraveCreateEvent(livingEntity, grave);
        plugin.getServer().getPluginManager().callEvent(graveCreateEvent);
        if (!graveCreateEvent.isCancelled() && !graveCreateEvent.isAddon()) {
            placeGrave(event, grave, graveCreateEvent, graveItemStackList, removedItemStackList, location, livingEntity, permissionList);
        } else if (graveCreateEvent.isCancelled() && !graveCreateEvent.isAddon()) {
            Player player = (Player) event.getEntity();
            if (player.getPlayer() != null) {
                player.getPlayer().getInventory().clear();
            }
        }
    }

    /**
     * Sets up the basic properties of the grave.
     *
     * @param grave         The grave to set up.
     * @param livingEntity  The entity that died.
     * @param entityName    The name of the entity.
     * @param permissionList The list of permissions.
     */
    private void setupGrave(Grave grave, LivingEntity livingEntity, String entityName, List<String> permissionList) {
        grave.setOwnerType(livingEntity.getType());
        grave.setOwnerName(entityName);
        grave.setOwnerNameDisplay(livingEntity instanceof Player ? ((Player) livingEntity).getDisplayName() : grave.getOwnerName());
        grave.setOwnerUUID(livingEntity.getUniqueId());
        grave.setPermissionList(permissionList);
        grave.setYaw(livingEntity.getLocation().getYaw());
        grave.setPitch(livingEntity.getLocation().getPitch());
        grave.setTimeAlive(plugin.getConfig("grave.time", grave).getInt("grave.time") * 1000L);
        if (!plugin.getVersionManager().is_v1_7()) {
            grave.setOwnerTexture(SkinTextureUtil.getTexture(livingEntity));
            grave.setOwnerTextureSignature(SkinSignatureUtil.getSignature(livingEntity));
        }
    }

    /**
     * Sets the experience for the grave.
     *
     * @param grave        The grave to set the experience for.
     * @param event        The entity death event.
     * @param livingEntity The entity that died.
     */
    private void setGraveExperience(Grave grave, EntityDeathEvent event, LivingEntity livingEntity) {
        float experiencePercent = (float) plugin.getConfig("experience.store", grave).getDouble("experience.store");
        if (experiencePercent >= 0) {
            if (livingEntity instanceof Player) {
                Player player = (Player) livingEntity;
                if (plugin.hasGrantedPermission("graves.experience", player.getPlayer())) {
                    grave.setExperience(ExperienceUtil.getDropPercent(ExperienceUtil.getPlayerExperience(player), experiencePercent));
                } else {
                    grave.setExperience(event.getDroppedExp());
                }
                if (event instanceof PlayerDeathEvent) {
                    ((PlayerDeathEvent) event).setKeepLevel(false);
                }
            } else {
                grave.setExperience(ExperienceUtil.getDropPercent(event.getDroppedExp(), experiencePercent));
            }
        } else {
            grave.setExperience(event.getDroppedExp());
        }
    }

    /**
     * Sets up the killer details for the grave.
     *
     * @param grave        The grave to set up.
     * @param livingEntity The entity that died.
     */
    private void setupGraveKiller(Grave grave, LivingEntity livingEntity) {
        if (livingEntity.getKiller() != null) {
            grave.setKillerType(EntityType.PLAYER);
            grave.setKillerName(livingEntity.getKiller().getName());
            grave.setKillerNameDisplay(livingEntity.getKiller().getDisplayName());
            grave.setKillerUUID(livingEntity.getKiller().getUniqueId());
        } else if (livingEntity.getLastDamageCause() != null) {
            EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
            if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && entityDamageEvent instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
                grave.setKillerUUID(entityDamageByEntityEvent.getDamager().getUniqueId());
                grave.setKillerType(entityDamageByEntityEvent.getDamager().getType());
                grave.setKillerName(plugin.getEntityManager().getEntityName(entityDamageByEntityEvent.getDamager()));
            } else {
                grave.setKillerUUID(null);
                grave.setKillerType(null);
                grave.setKillerName(plugin.getGraveManager().getDamageReason(entityDamageEvent.getCause(), grave));
            }
            grave.setKillerNameDisplay(grave.getKillerName());
        }
    }

    /**
     * Sets up the protection details for the grave.
     *
     * @param grave The grave to set up.
     */
    private void setupGraveProtection(LivingEntity livingEntity, Grave grave) {
        if (plugin.getConfig("protection.enabled", grave).getBoolean("protection.enabled")) {
            GraveProtectionCreateEvent graveProtectionCreateEvent = new GraveProtectionCreateEvent(livingEntity, grave);
            plugin.getServer().getPluginManager().callEvent(graveProtectionCreateEvent);
            if (!graveProtectionCreateEvent.isCancelled() && !graveProtectionCreateEvent.isAddon()) {
                grave.setProtection(true);
                grave.setTimeProtection(plugin.getConfig("protection.time", grave).getInt("protection.time") * 1000L);
            }
        }
    }

    /**
     * Places the grave at the specified location.
     *
     * @param event               The entity death event.
     * @param grave               The grave to place.
     * @param graveCreateEvent    The grave create event.
     * @param graveItemStackList  The list of item stacks for the grave.
     * @param removedItemStackList The list of removed item stacks.
     * @param location            The location to place the grave.
     * @param livingEntity        The entity that died.
     * @param permissionList      The list of permissions.
     */
    private void placeGrave(EntityDeathEvent event, Grave grave, GraveCreateEvent graveCreateEvent, List<ItemStack> graveItemStackList, List<ItemStack> removedItemStackList, Location location, LivingEntity livingEntity, List<String> permissionList) {
        Map<Location, BlockData.BlockType> locationMap = new HashMap<>();
        Location safeLocation = plugin.getLocationManager().getSafeGraveLocation(livingEntity, location, grave);
        event.getDrops().clear();
        event.getDrops().addAll(event.getDrops());
        event.setDroppedExp(0);

        grave.setLocationDeath(safeLocation != null ? safeLocation : location);
        grave.getLocationDeath().setYaw(grave.getYaw());
        grave.getLocationDeath().setPitch(grave.getPitch());

        if (locationMap.isEmpty()) {
            locationMap.put(grave.getLocationDeath(), BlockData.BlockType.DEATH);
        }

        setupObituary(grave, graveItemStackList, livingEntity, location);
        setupSkull(grave, graveItemStackList, livingEntity, location);
        grave.setInventory(plugin.getGraveManager().getGraveInventory(grave, livingEntity, graveItemStackList, removedItemStackList, permissionList));
        grave.setEquipmentMap(!plugin.getVersionManager().is_v1_7() ? plugin.getEntityManager().getEquipmentMap(livingEntity, grave) : new HashMap<>());

        if (!locationMap.isEmpty()) {
            notifyGraveCreation(event, grave, locationMap, livingEntity, permissionList);
        } else {
            handleFailedGravePlacement(event, grave, location, livingEntity);
        }
    }

    /**
     * Sets up the obituary item for the grave.
     *
     * @param grave               The grave to set up.
     * @param graveItemStackList  The list of item stacks for the grave.
     */
    private void setupObituary(Grave grave, List<ItemStack> graveItemStackList, LivingEntity livingEntity, Location location) {
        if (plugin.getConfig("obituary.enabled", grave).getBoolean("obituary.enabled")) {
            double percentage = plugin.getConfig("obituary.percent", grave).getDouble("obituary.percent");
            boolean shouldDrop = plugin.getConfig("obituary.drop", grave).getBoolean("obituary.drop");

            // Ensure the percentage is between 0 and 1
            if (percentage < 0) {
                percentage = 0;
            } else if (percentage > 1) {
                percentage = 1;
            }

            if (shouldAddObituaryItem(percentage)) {
                GraveObituaryAddEvent graveObituaryAddEvent = new GraveObituaryAddEvent(grave, location, livingEntity);
                plugin.getServer().getPluginManager().callEvent(graveObituaryAddEvent);

                if (!graveObituaryAddEvent.isCancelled() && !graveObituaryAddEvent.isAddon()) {
                    if (shouldDrop) {
                        ItemStack obituaryItem = plugin.getItemStackManager().getGraveObituary(grave);
                        if (location.getWorld() != null) {
                            location.getWorld().dropItemNaturally(location, obituaryItem);
                            plugin.debugMessage("Obituary dropped at location x: " + location.getBlockX() + " y: " + location.getBlockY() + " z: " + location.getBlockZ() + ".", 2);
                        } else {
                            Block graveLocation = location.getBlock();
                            plugin.debugMessage("World not found. Obituary added to " + grave.getOwnerName() + "'s Grave at location x: " + graveLocation.getX() + " y: " + graveLocation.getY() + " z: " + graveLocation.getZ() + ".", 2);
                            graveItemStackList.add(plugin.getItemStackManager().getGraveObituary(grave));
                        }
                    } else {
                        Block graveLocation = location.getBlock();
                        plugin.debugMessage("Obituary added to " + grave.getOwnerName() + "'s Grave at location x: " + graveLocation.getX() + " y: " + graveLocation.getY() + " z: " + graveLocation.getZ() + ".", 2);
                        graveItemStackList.add(plugin.getItemStackManager().getGraveObituary(grave));
                    }
                }
            }
        }
    }

    /**
     * Determines whether to add the obituary item based on the percentage.
     *
     * @param percentage The percentage value (between 0 and 1).
     * @return true if the obituary item should be added; false otherwise.
     */
    private boolean shouldAddObituaryItem(double percentage) {
        return Math.random() <= percentage;
    }

    /**
     * Sets up the skull item for the grave.
     *
     * @param grave               The grave to set up.
     * @param graveItemStackList  The list of item stacks for the grave.
     */
    private void setupSkull(Grave grave, List<ItemStack> graveItemStackList, LivingEntity livingEntity, Location location) {
        if (plugin.getConfig("head.enabled", grave).getBoolean("head.enabled")
                && Math.random() < plugin.getConfig("head.percent", grave).getDouble("head.percent")
                && grave.getOwnerTexture() != null
                && grave.getOwnerTextureSignature() != null) {

            boolean shouldDrop = plugin.getConfig("head.drop", grave).getBoolean("head.drop");

            GravePlayerHeadDropEvent gravePlayerHeadDropEvent = new GravePlayerHeadDropEvent(grave, location, livingEntity);
            plugin.getServer().getPluginManager().callEvent(gravePlayerHeadDropEvent);

            if (!gravePlayerHeadDropEvent.isCancelled() && !gravePlayerHeadDropEvent.isAddon()) {
                if (shouldDrop) {
                    if (location.getWorld() != null) {
                        ItemStack headItem = plugin.getItemStackManager().getGraveHead(grave);
                        location.getWorld().dropItemNaturally(location, headItem);
                        plugin.debugMessage("Player Head dropped at location x: " + location.getBlockX() + ", y: " + location.getBlockY() + ", z: " + location.getBlockZ() + ".", 2);
                    } else {
                        Location graveLocation = grave.getLocationDeath();
                        plugin.debugMessage("World not found. Player Head added to " + livingEntity.getName() + "'s grave at location x: " + graveLocation.getBlockX() + ", y: " + graveLocation.getBlockY() + ", z: " + graveLocation.getBlockZ() + ".", 2);
                        graveItemStackList.add(plugin.getItemStackManager().getGraveHead(grave));
                    }
                } else {
                    Location graveLocation = grave.getLocationDeath();
                    plugin.debugMessage("Player Head added to " + livingEntity.getName() + "'s grave at location x: " + graveLocation.getBlockX() + ", y: " + graveLocation.getBlockY() + ", z: " + graveLocation.getBlockZ() + ".", 2);
                    graveItemStackList.add(plugin.getItemStackManager().getGraveHead(grave));
                }
            }
        }
    }

    /**
     * Notifies the creation of the grave and places the grave blocks.
     *
     * @param event              The entity death event.
     * @param grave              The grave that was created.
     * @param locationMap        The map of locations for the grave.
     * @param livingEntity       The entity that died.
     * @param permissionList     The list of permissions.
     */
    private void notifyGraveCreation(EntityDeathEvent event, Grave grave, Map<Location, BlockData.BlockType> locationMap, LivingEntity livingEntity, List<String> permissionList) {
        plugin.getEntityManager().sendMessage("message.death", livingEntity, grave.getLocationDeath(), grave);
        plugin.getEntityManager().runCommands("event.command.create", livingEntity, grave.getLocationDeath(), grave);
        plugin.getDataManager().addGrave(grave);
        Player player = (Player) event.getEntity();
        if (plugin.getConfig("noteblockapi.enabled", grave).getBoolean("noteblockapi.enabled") && plugin.getIntegrationManager().hasNoteBlockAPI()) {
            if (plugin.getConfig("noteblockapi.play-locally", grave).getBoolean("noteblockapi.play-locally")) {
                plugin.getIntegrationManager().getNoteBlockAPI().playSongForPlayer(player, plugin.getConfig("noteblockapi.nbs-sound", grave).getString("noteblockapi.nbs-sound"));
            } else {
                plugin.getIntegrationManager().getNoteBlockAPI().playSongForAllPlayers(plugin.getConfig("noteblockapi.nbs-sound", grave).getString("noteblockapi.nbs-sound"));
            }
        } else {
            player.playSound(player.getLocation(), plugin.getVersionManager().getSoundFromVersion("BLOCK_BELL_USE"), 1.0f, 0.93f);
        }
        if (plugin.getIntegrationManager().hasMultiPaper()) {
            plugin.getIntegrationManager().getMultiPaper().notifyGraveCreation(grave);
        }
        placeGraveBlocks(event, grave, locationMap, livingEntity);
    }

    /**
     * Places the grave blocks at the specified locations.
     *
     * @param event              The entity death event.
     * @param grave              The grave to place.
     * @param locationMap        The map of locations for the grave.
     * @param livingEntity       The entity that died.
     */
    private void placeGraveBlocks(EntityDeathEvent event, Grave grave, Map<Location, BlockData.BlockType> locationMap, LivingEntity livingEntity) {
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
            }
            location.add(offsetX, offsetY, offsetZ);
            GraveBlockPlaceEvent graveBlockPlaceEvent = new GraveBlockPlaceEvent(grave, location, entry.getValue(), entry.getKey().getBlock(), livingEntity);
            plugin.getServer().getPluginManager().callEvent(graveBlockPlaceEvent);
            if (!graveBlockPlaceEvent.isCancelled() && !graveBlockPlaceEvent.isAddon()) {
                plugin.getGraveManager().placeGrave(graveBlockPlaceEvent.getLocation(), grave);
                plugin.getEntityManager().sendMessage("message.block", livingEntity, location, grave);
                plugin.getEntityManager().runCommands("event.command.block", livingEntity, graveBlockPlaceEvent.getLocation(), grave);
            }
        }
    }

    /**
     * Handles failed grave placement.
     *
     * @param event         The entity death event.
     * @param grave         The grave that failed to be placed.
     * @param location      The location where the grave was to be placed.
     * @param livingEntity  The entity that died.
     */
    private void handleFailedGravePlacement(EntityDeathEvent event, Grave grave, Location location, LivingEntity livingEntity) {
        if (event instanceof PlayerDeathEvent && plugin.getConfig("placement.failure-keep-inventory", grave).getBoolean("placement.failure-keep-inventory")) {
            PlayerDeathEvent playerDeathEvent = (PlayerDeathEvent) event;
            try {
                playerDeathEvent.setKeepLevel(true);
                playerDeathEvent.setKeepInventory(true);
                plugin.getEntityManager().sendMessage("message.failure-keep-inventory", livingEntity, location, grave);
            } catch (NoSuchMethodError ignored) {
            }
        } else {
            event.getDrops().addAll(event.getDrops());
            event.setDroppedExp(event.getDroppedExp());
            plugin.getEntityManager().sendMessage("message.failure", livingEntity, location, grave);
        }
    }

    private Location findGraveLocation(Grave grave, LivingEntity livingEntity, Location location) {
        return plugin.getLocationManager().getSafeGraveLocation(livingEntity, location, grave);
    }
}