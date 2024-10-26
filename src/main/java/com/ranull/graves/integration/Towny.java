package com.ranull.graves.integration;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.ranull.graves.Graves;
import com.ranull.graves.listener.integration.towny.TownBlockTypeRegisterListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides integration with Towny to manage town blocks.
 */
public final class Towny {
    private final Graves plugin;
    private final Plugin townyPlugin;
    private final TownyAPI townyAPI;
    private final TownBlockTypeRegisterListener townBlockTypeRegisterListener;

    /**
     * Constructs a new Towny integration instance with the specified Graves plugin and Towny plugin.
     *
     * @param plugin        The main Graves plugin instance.
     * @param townyPlugin   The Towny plugin instance.
     */
    public Towny(Graves plugin, Plugin townyPlugin) {
        this.plugin = plugin;
        this.townyPlugin = townyPlugin;
        this.townyAPI = TownyAPI.getInstance();
        this.townBlockTypeRegisterListener = new TownBlockTypeRegisterListener(this);

        registerListeners();
    }

    /**
     * Checks if the Towny plugin is enabled.
     *
     * @return {@code true} if the Towny plugin is enabled, otherwise {@code false}.
     */
    public boolean isEnabled() {
        return townyPlugin.isEnabled();
    }

    /**
     * Registers event listeners related to Towny integration.
     */
    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(townBlockTypeRegisterListener, plugin);
    }

    /**
     * Unregisters event listeners related to Towny integration.
     */
    public void unregisterListeners() {
        HandlerList.unregisterAll(townBlockTypeRegisterListener);
    }

    /**
     * Registers a new TownBlockType with Towny.
     *
     * @param townBlockType The TownBlockType to register.
     */
    private void registerType(TownBlockType townBlockType) {
        try {
            TownBlockTypeHandler.registerType(townBlockType);
        } catch (TownyException exception) {
            plugin.logStackTrace(exception);
        }
    }

    /**
     * Checks if a player is a resident of a specific town.
     *
     * @param player The player to check.
     * @param town   The town name to check against.
     * @return {@code true} if the player is a resident of the town, otherwise {@code false}.
     */
    public boolean isTownResident(Player player, String town) {
        Resident resident = townyAPI.getResident(player);

        if (resident != null && resident.hasTown()) {
            try {
                return resident.getTown().getName().equals(town);
            } catch (NotRegisteredException ignored) {
            }
        }

        return false;
    }

    /**
     * Checks if a player has a town plot with the given name.
     *
     * @param player The player to check.
     * @param name   The name of the plot to check.
     * @return {@code true} if the player has a town plot with the given name, otherwise {@code false}.
     */
    public boolean hasTownPlot(Player player, String name) {
        return !getTownPlotsByName(player, name).isEmpty();
    }

    /**
     * Gets a list of town plots with a specific name that a player is associated with.
     *
     * @param player The player to check.
     * @param name   The name of the plots to retrieve.
     * @return A list of TownBlocks with the given name.
     */
    public List<TownBlock> getTownPlotsByName(Player player, String name) {
        Resident resident = townyAPI.getResident(player);

        return resident != null && resident.getTownOrNull() != null
                ? getTownPlotsByName(resident.getTownOrNull(), name) : new ArrayList<>();
    }

    /**
     * Gets a list of town plots with a specific name in the town at the given location.
     *
     * @param location The location to check.
     * @param name     The name of the plots to retrieve.
     * @return A list of TownBlocks with the given name.
     */
    public List<TownBlock> getTownPlotsByName(Location location, String name) {
        Town town = townyAPI.getTown(location);

        return town != null ? getTownPlotsByName(town, name) : new ArrayList<>();
    }

    /**
     * Gets a list of town plots with a specific name in the provided town.
     *
     * @param town The town to check.
     * @param name The name of the plots to retrieve.
     * @return A list of TownBlocks with the given name.
     */
    public List<TownBlock> getTownPlotsByName(Town town, String name) {
        List<TownBlock> townBlockList = new ArrayList<>();

        for (TownBlock townBlock : town.getTownBlocks()) {
            if (townBlock.getName().equals(name)) {
                townBlockList.add(townBlock);
            }
        }

        return townBlockList;
    }

    /**
     * Checks if a player is a resident of a specific region (town).
     *
     * @param region The region (town) to check.
     * @param player The player to check.
     * @return {@code true} if the player is a resident, otherwise {@code false}.
     */
    public boolean isResident(String region, Player player) {
        Resident resident = townyAPI.getResident(player);

        if (resident != null && resident.hasTown()) {
            try {
                Town town = resident.getTown();
                return town.getName().equalsIgnoreCase(region);
            } catch (NotRegisteredException ignored) {
            }
        }

        return false;
    }

    /**
     * Checks if a location is inside a plot with a specific name.
     *
     * @param location The location to check.
     * @param name     The name of the plot.
     * @return {@code true} if the location is inside the plot, otherwise {@code false}.
     */
    public boolean isInsidePlot(Location location, String name) {
        TownBlock townBlock = townyAPI.getTownBlock(location);

        return townBlock != null && townBlock.getName().equals(name);
    }

    /**
     * Retrieves a list of all town names from Towny.
     *
     * @return A List of all town names.
     */
    public List<String> getTownNames() {
        List<String> townNames = new ArrayList<>();
        for (Town town : townyAPI.getTowns()) {
            townNames.add(town.getName());
        }
        return townNames;
    }

}