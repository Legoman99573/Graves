package com.ranull.graves.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides integration with WorldGuard for managing regions and flags related to graves.
 */
public final class WorldGuard {
    private final JavaPlugin plugin;
    private final com.sk89q.worldguard.WorldGuard worldGuard;
    private final StateFlag createFlag;
    private final StateFlag teleportFlag;

    /**
     * Constructs a new WorldGuard integration instance with the specified plugin.
     *
     * @param plugin The JavaPlugin instance.
     */
    public WorldGuard(JavaPlugin plugin) {
        this.plugin = plugin;
        this.worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
        this.createFlag = getFlag("graves-create", true);
        this.teleportFlag = getFlag("graves-teleport", true);
    }

    /**
     * Retrieves or registers a StateFlag with the specified name and default value.
     *
     * @param string The name of the flag.
     * @param defaultValue The default value of the flag (ALLOW by default).
     * @return The StateFlag if found or created, otherwise {@code null}.
     */
    private StateFlag getFlag(String string, boolean defaultValue) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            Flag<?> flag = worldGuard.getFlagRegistry().get(string);

            if (flag instanceof StateFlag) {
                return (StateFlag) flag;
            }
        } else {
            try {
                StateFlag flag = new StateFlag(string, defaultValue);
                worldGuard.getFlagRegistry().register(flag);
                return flag;
            } catch (FlagConflictException exception) {
                Flag<?> flag = worldGuard.getFlagRegistry().get(string);
                if (flag instanceof StateFlag) {
                    return (StateFlag) flag;
                }
            }
        }

        return null;
    }

    public void setRegionFlag(String worldName, String regionName, String flagName, String value) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }

        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager != null) {
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                FlagRegistry registry = worldGuard.getFlagRegistry();
                StateFlag stateFlag = (StateFlag) registry.get(flagName);
                if (stateFlag != null) {
                    StateFlag.State state = StateFlag.State.valueOf(value.toUpperCase());
                    region.setFlag(stateFlag, state);
                    try {
                        regionManager.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Checks if the location allows creating graves based on WorldGuard regions and flags.
     *
     * @param location The location to check.
     * @return {@code true} if grave creation is allowed, otherwise {@code false}.
     */
    public boolean hasCreateGrave(Location location) {
        if (location.getWorld() != null && createFlag != null) {
            RegionManager regionManager = worldGuard.getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(location.getWorld()));

            if (regionManager != null) {
                ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(BlockVector3
                        .at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

                for (ProtectedRegion protectedRegion : applicableRegions.getRegions()) {
                    StateFlag.State flagState = protectedRegion.getFlag(createFlag);
                    if (flagState == StateFlag.State.DENY) {
                        return false;
                    }
                }
            }
        }

        return true; // Allow by default
    }

    /**
     * Checks if the entity has permission to create graves at the specified location.
     *
     * @param entity   The entity to check.
     * @param location The location to check.
     * @return {@code true} if the entity can create graves, otherwise {@code false}.
     */
    public boolean canCreateGrave(Entity entity, Location location) {
        return entity instanceof Player && createFlag != null
                && worldGuard.getPlatform().getRegionContainer().createQuery().testState(BukkitAdapter.adapt(location),
                WorldGuardPlugin.inst().wrapPlayer((Player) entity), createFlag);
    }

    /**
     * Checks if graves can be created at the specified location based on WorldGuard flags.
     *
     * @param location The location to check.
     * @return {@code true} if graves can be created, otherwise {@code false}.
     */
    public boolean canCreateGrave(Location location) {
        return createFlag != null
                && worldGuard.getPlatform().getRegionContainer().createQuery().testState(BukkitAdapter.adapt(location),
                (RegionAssociable) null, createFlag);
    }

    /**
     * Checks if the entity has permission to teleport at the specified location.
     *
     * @param entity   The entity to check.
     * @param location The location to check.
     * @return {@code true} if the entity can teleport, otherwise {@code false}.
     */
    public boolean canTeleport(Entity entity, Location location) {
        return entity instanceof Player && teleportFlag != null
                && worldGuard.getPlatform().getRegionContainer().createQuery().testState(BukkitAdapter.adapt(location),
                WorldGuardPlugin.inst().wrapPlayer((Player) entity), teleportFlag);
    }

    /**
     * Checks if teleportation is allowed at the specified location based on WorldGuard flags.
     *
     * @param location The location to check.
     * @return {@code true} if teleportation is allowed, otherwise {@code false}.
     */
    public boolean canTeleport(Location location) {
        return teleportFlag != null
                && worldGuard.getPlatform().getRegionContainer().createQuery().testState(BukkitAdapter.adapt(location),
                (RegionAssociable) null, teleportFlag);
    }

    /**
     * Retrieves the world associated with a specified WorldGuard region.
     *
     * @param region The name of the region.
     * @return The associated World, or {@code null} if the region is not found.
     */
    public World getRegionWorld(String region) {
        for (RegionManager regionManager : worldGuard.getPlatform().getRegionContainer().getLoaded()) {
            if (regionManager.getRegions().containsKey(region)) {
                return plugin.getServer().getWorld(regionManager.getName());
            }
        }

        return null;
    }

    /**
     * Checks if a player is a member of a specified WorldGuard region.
     *
     * @param region The name of the region.
     * @param player The player to check.
     * @return {@code true} if the player is a member, otherwise {@code false}.
     */
    public boolean isMember(String region, Player player) {
        for (RegionManager regionManager : worldGuard.getPlatform().getRegionContainer().getLoaded()) {
            if (regionManager.getRegions().containsKey(region)) {
                ProtectedRegion protectedRegion = regionManager.getRegion(region);

                if (protectedRegion != null) {
                    return protectedRegion.isMember(WorldGuardPlugin.inst().wrapPlayer(player));
                }
            }
        }

        return false;
    }

    /**
     * Checks if a location is inside a specified WorldGuard region.
     *
     * @param location The location to check.
     * @param region   The name of the region.
     * @return {@code true} if the location is inside the region, otherwise {@code false}.
     */
    public boolean isInsideRegion(Location location, String region) {
        if (location.getWorld() != null) {
            RegionManager regionManager = worldGuard.getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(location.getWorld()));

            if (regionManager != null) {
                ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(BlockVector3
                        .at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

                for (ProtectedRegion protectedRegion : applicableRegions.getRegions()) {
                    if (protectedRegion.getId().equals(region)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Retrieves a list of region keys for a specified location.
     *
     * @param location The location to retrieve region keys for.
     * @return A list of region keys.
     */
    public List<String> getRegionKeyList(Location location) {
        List<String> regionNameList = new ArrayList<>();

        if (location.getWorld() != null) {
            RegionManager regionManager = worldGuard.getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(location.getWorld()));

            if (regionManager != null) {
                ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(BlockVector3
                        .at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

                for (ProtectedRegion protectedRegion : applicableRegions.getRegions()) {
                    regionNameList.add("worldguard|" + location.getWorld().getName() + "|" + protectedRegion.getId());
                }
            }
        }

        return regionNameList;
    }

    /**
     * Retrieves a list of region names.
     *
     * @return A list of region names.
     */
    public List<String> getRegions() {
        List<String> regionNames = new ArrayList<>();
        for (RegionManager regionManager : worldGuard.getPlatform().getRegionContainer().getLoaded()) {
            regionNames.addAll(regionManager.getRegions().keySet());
        }
        return regionNames;
    }
}