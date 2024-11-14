package com.ranull.graves.manager;

import ch.njol.skript.SkriptAddon;
import com.ranull.graves.Graves;
import com.ranull.graves.integration.*;
import com.ranull.graves.listener.integration.coreprotect.CoreProtectListener;
import com.ranull.graves.listener.integration.itemsadder.CustomBlockBreakListener;
import com.ranull.graves.listener.integration.itemsadder.FurnitureBreakListener;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * The {@code IntegrationManager} class is responsible for managing the integration of various external plugins with the Graves plugin.
 * This class handles loading, unloading, and checking the availability of these integrations, allowing the Graves plugin to interact with other plugins.
 */
public final class IntegrationManager {
    /**
     * The main plugin instance associated with Graves.
     * <p>
     * This {@link Graves} instance represents the core plugin that this Graves is part of. It provides access
     * to the plugin's functionality, configuration, and other services.
     * </p>
     */
    private final Graves plugin;

    /**
     * Integration with MultiPaper, a server software or library.
     * <p>
     * This {@link MultiPaper} instance represents the integration with the MultiPaper server software or library.
     * </p>
     */
    private MultiPaper multiPaper;

    /**
     * Integration with Vault, a permissions and economy API.
     * <p>
     * This {@link Vault} instance represents the integration with the Vault API, used for permissions and economy functionalities.
     * </p>
     */
    private Vault vault;

    /**
     * Integration with ProtocolLib, a library for handling protocol-related tasks.
     * <p>
     * This {@link ProtocolLib} instance represents the integration with the ProtocolLib library, which is used for manipulating network protocols.
     * </p>
     */
    private ProtocolLib protocolLib;

    /**
     * Integration with WorldEdit, a tool for editing worlds.
     * <p>
     * This {@link WorldEdit} instance represents the integration with the WorldEdit plugin, used for large-scale world editing.
     * </p>
     */
    private WorldEdit worldEdit;

    /**
     * Integration with WorldGuard, a plugin for managing regions and protection.
     * <p>
     * This {@link WorldGuard} instance represents the integration with the WorldGuard plugin, used for managing region protection.
     * </p>
     */
    private WorldGuard worldGuard;

    /**
     * Integration with Towny, a plugin for managing towns and nations.
     * <p>
     * This {@link Towny} instance represents the integration with the Towny plugin, used for managing town and nation systems.
     * </p>
     */
    private Towny towny;

    /**
     * Integration with GriefDefender, a plugin for land protection and grief prevention.
     * <p>
     * This {@link GriefDefender} instance represents the integration with the GriefDefender plugin, used for land protection and grief prevention.
     * </p>
     */
    private GriefDefender griefDefender;

    /**
     * Integration with FurnitureLib, a plugin for furniture management.
     * <p>
     * This {@link FurnitureLib} instance represents the integration with the FurnitureLib plugin, used for managing furniture.
     * </p>
     */
    private FurnitureLib furnitureLib;

    /**
     * Integration with FurnitureEngine, another plugin for furniture management.
     * <p>
     * This {@link FurnitureEngine} instance represents the integration with the FurnitureEngine plugin, used for managing furniture.
     * </p>
     */
    private FurnitureEngine furnitureEngine;

    /**
     * Integration with ProtectionLib, a library for protection management.
     * <p>
     * This {@link ProtectionLib} instance represents the integration with the ProtectionLib library, used for protection-related functionalities.
     * </p>
     */
    private ProtectionLib protectionLib;

    /**
     * Integration with ItemsAdder, a plugin for adding custom items.
     * <p>
     * This {@link ItemsAdder} instance represents the integration with the ItemsAdder plugin, used for adding custom items to the game.
     * </p>
     */
    private ItemsAdder itemsAdder;

    /**
     * Integration with Oraxen, a plugin for custom items and resource packs.
     * <p>
     * This {@link Oraxen} instance represents the integration with the Oraxen plugin, used for managing custom items and resource packs.
     * </p>
     */
    private Oraxen oraxen;

    /**
     * Integration with ChestSort, a plugin for sorting chests and inventories.
     * <p>
     * This {@link ChestSort} instance represents the integration with the ChestSort plugin, used for sorting chests and other inventories.
     * </p>
     */
    private ChestSort chestSort;

    /**
     * Integration with MiniMessage, a library for advanced message formatting.
     * <p>
     * This {@link MiniMessage} instance represents the integration with the MiniMessage library, used for advanced message formatting.
     * </p>
     */
    private MiniMessage miniMessage;

    /**
     * Integration with MineDown, a library for Markdown-like text formatting.
     * <p>
     * This {@link MineDown} instance represents the integration with the MineDown library, used for text formatting similar to Markdown.
     * </p>
     */
    private MineDown mineDown;

    /**
     * Integration with ItemBridge, a plugin or library for item management.
     * <p>
     * This {@link ItemBridge} instance represents the integration with the ItemBridge plugin or library, used for managing items.
     * </p>
     */
    private ItemBridge itemBridge;
    /**
     * Integration with floodgate
     * <p>
     * This boolean is used to handle bedrock players
     * </p>
     */
    private boolean floodgate;
    /**
     * Integration with FancyNPCs, a plugin for managing player-like NPCs.
     * <p>
     * This {@link FancyNPCs} instance represents the integration with the FancyNPCs plugin, used for creating and managing NPCs that mimic players.
     * </p>
     */
    private FancyNPCs fancyNpcs;

    /**
     * Integration with PlayerNPC, a plugin for managing player-like NPCs.
     * <p>
     * This {@link PlayerNPC} instance represents the integration with the PlayerNPC plugin, used for creating and managing NPCs that mimic players.
     * </p>
     */
    private PlayerNPC playerNPC;

    /**
     * Integration with CitizensNPC, a plugin for creating NPCs.
     * <p>
     * This {@link CitizensNPC} instance represents the integration with the Citizens plugin, used for creating and managing NPCs in the game.
     * </p>
     */
    private CitizensNPC citizensNPC;

    /**
     * Integration with PlaceholderAPI, a plugin for managing placeholders.
     * <p>
     * This {@link PlaceholderAPI} instance represents the integration with the PlaceholderAPI plugin, used for managing and resolving placeholders.
     * </p>
     */
    private PlaceholderAPI placeholderAPI;

    /**
     * Integration with Skript, a plugin for scripting.
     * <p>
     * This {@link SkriptImpl} instance represents the integration with the Skript plugin, used for scripting and creating custom scripts.
     * </p>
     */
    private SkriptImpl skriptImpl;

    /**
     * Indicates whether Vault permissions are available.
     * <p>
     * This {@code boolean} flag indicates if Vault permissions are present and can be used within the plugin.
     * </p>
     */
    private boolean hasVaultPermissions;

    /**
     * Handles integration with LuckPerms, a permissions management plugin.
     * <p>
     * This {@link LuckPermsHandler} instance represents the handler for integrating with the LuckPerms plugin, which manages permissions.
     * </p>
     */
    private LuckPermsHandler luckPermsHandler;

    /**
     * Manages integration with CoreProtect, a plugin for block logging and protection.
     * <p>
     * This {@link CoreProtectIntegration} instance represents the integration with the CoreProtect plugin, used for logging and block protection.
     * </p>
     */
    private CoreProtectIntegration coreProtectIntegration;

    /**
     * Indicates whether Vault Economy are available.
     * <p>
     * This {@code boolean} flag indicates if Vault permissions are present and can be used within the plugin.
     * </p>
     */
    private boolean hasVaultEconomy;

    /**
     * Handles integration with NoteBlockAPI, a permissions management plugin.
     * <p>
     * This {@link NoteBlockAPI} instance represents the handler for integrating with the NoteBlockAPI plugin, which manages playing nbs files.
     * </p>
     */
    private NoteBlockAPI noteBlockAPI;

    /**
     * Indicates whether NoteBlockAPI are available.
     * <p>
     * This {@code boolean} flag indicates if NoteBlockAPI are present and can be used within the plugin.
     * </p>
     */
    private boolean hasNoteBlockAPI;

    /**
     * Initializes a new instance of the {@code IntegrationManager} class.
     *
     * @param plugin The plugin instance of Graves.
     */
    public IntegrationManager(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads all integrations by first unloading them and then loading them again.
     */
    public void reload() {
        unload();
        load();
    }

    /**
     * Loads all integrations for the Graves plugin.
     */
    public void load() {
        loadMultiPaper();
        loadVault();
        loadProtocolLib();
        loadWorldEdit();
        loadWorldGuard();
        loadTowny();
        loadGriefDefender(); // TODO: Integration enabled, test for possible issues.
        loadFurnitureLib();
        loadFurnitureEngine();
        loadProtectionLib();
        loadItemsAdder();
        loadOraxen();
        loadMiniMessage();
        loadMineDown();
        loadChestSort();
        loadPlayerNPC();
        loadCitizensNPC();
        loadItemBridge();
        loadPlaceholderAPI();
        loadCompatibilityWarnings();
        loadLuckPerms();
        loadCoreProtect();
        loadNBTAPI();
        loadBedrockSupport();
        loadFancyNpcs();
        loadNoteblockAPI();
    }

    /**
     * Loads Skript integration without reloading other integrations.
     */
    public void loadNoReload() {
        loadSkript();
    }

    /**
     * Unloads Skript integration without unloading other integrations.
     */
    public void unloadNoReload() {
        if (skriptImpl != null) {
            skriptImpl = null;
        }
    }

    /**
     * Unloads all integrations associated with the Graves plugin.
     */
    public void unload() {
        if (furnitureLib != null) {
            furnitureLib.unregisterListeners();
        }

        if (furnitureEngine != null) {
            furnitureEngine.unregisterListeners();
        }

        if (oraxen != null) {
            oraxen.unregisterListeners();
        }

        if (placeholderAPI != null) {
            placeholderAPI.unregister();
        }

        if (playerNPC != null) {
            playerNPC.unregisterListeners();
        }

        if (citizensNPC != null) {
            citizensNPC.unregisterListeners();
        }

        if (towny != null) {
            towny.unregisterListeners();
        }
    }

    /**
     * Returns the instance of the MultiPaper integration, if it is loaded.
     *
     * @return The {@code MultiPaper} integration instance, or null if not loaded.
     */
    public MultiPaper getMultiPaper() {
        return multiPaper;
    }

    /**
     * Returns the instance of the Vault integration, if it is loaded.
     *
     * @return The {@code Vault} integration instance, or null if not loaded.
     */
    public Vault getVault() {
        return vault;
    }

    /**
     * Returns the instance of the ProtocolLib integration, if it is loaded.
     *
     * @return The {@code ProtocolLib} integration instance, or null if not loaded.
     */
    public ProtocolLib getProtocolLib() {
        return protocolLib;
    }

    /**
     * Returns the instance of the WorldEdit integration, if it is loaded.
     *
     * @return The {@code WorldEdit} integration instance, or null if not loaded.
     */
    public WorldEdit getWorldEdit() {
        return worldEdit;
    }

    /**
     * Returns the instance of the WorldGuard integration, if it is loaded.
     *
     * @return The {@code WorldGuard} integration instance, or null if not loaded.
     */
    public WorldGuard getWorldGuard() {
        return worldGuard;
    }

    /**
     * Returns the instance of the Towny integration, if it is loaded.
     *
     * @return The {@code Towny} integration instance, or null if not loaded.
     */
    public Towny getTowny() {
        return towny;
    }

    /**
     * Returns the instance of the GriefDefender integration, if it is loaded.
     *
     * @return The {@code GriefDefender} integration instance, or null if not loaded.
     */
    public GriefDefender getGriefDefender() {
        return griefDefender;
    }

    /**
     * Returns the instance of the CoreProtect integration, if it is loaded.
     *
     * @return The {@code CoreProtect} integration instance, or null if not loaded.
     */
    public CoreProtectIntegration getCoreProtect(){
        return coreProtectIntegration;
    }

    /**
     * Returns the instance of the FurnitureLib integration, if it is loaded.
     *
     * @return The {@code FurnitureLib} integration instance, or null if not loaded.
     */
    public FurnitureLib getFurnitureLib() {
        return furnitureLib;
    }

    /**
     * Returns the instance of the FurnitureEngine integration, if it is loaded.
     *
     * @return The {@code FurnitureEngine} integration instance, or null if not loaded.
     */
    public FurnitureEngine getFurnitureEngine() {
        return furnitureEngine;
    }

    /**
     * Returns the instance of the ProtectionLib integration, if it is loaded.
     *
     * @return The {@code ProtectionLib} integration instance, or null if not loaded.
     */
    public ProtectionLib getProtectionLib() {
        return protectionLib;
    }

    /**
     * Returns the instance of the ItemsAdder integration, if it is loaded.
     *
     * @return The {@code ItemsAdder} integration instance, or null if not loaded.
     */
    public ItemsAdder getItemsAdder() {
        return itemsAdder;
    }

    /**
     * Returns the instance of the Oraxen integration, if it is loaded.
     *
     * @return The {@code Oraxen} integration instance, or null if not loaded.
     */
    public Oraxen getOraxen() {
        return oraxen;
    }

    /**
     * Returns the instance of the MiniMessage integration, if it is loaded.
     *
     * @return The {@code MiniMessage} integration instance, or null if not loaded.
     */
    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    /**
     * Returns the instance of the MineDown integration, if it is loaded.
     *
     * @return The {@code MineDown} integration instance, or null if not loaded.
     */
    public MineDown getMineDown() {
        return mineDown;
    }

    /**
     * Returns the instance of the ChestSort integration, if it is loaded.
     *
     * @return The {@code ChestSort} integration instance, or null if not loaded.
     */
    public ChestSort getChestSort() {
        return chestSort;
    }

    /**
     * Returns the instance of the PlayerNPC integration, if it is loaded.
     *
     * @return The {@code PlayerNPC} integration instance, or null if not loaded.
     */
    public PlayerNPC getPlayerNPC() {
        return playerNPC;
    }

    /**
     * Returns the instance of the NoteBlockAPI integration, if it is loaded.
     *
     * @return The {@code NoteBlockAPI} integration instance, or null if not loaded.
     */
    public NoteBlockAPI getNoteBlockAPI() {
        return noteBlockAPI;
    }

    /**
     * Returns the SkriptAddon instance if Skript integration is loaded.
     *
     * @return The {@code SkriptAddon} instance, or null if Skript is not loaded.
     */
    public SkriptAddon getSkript() {
        return skriptImpl != null ? skriptImpl.getSkriptAddon() : null;
    }

    /**
     * Returns whether you are using floodgate.
     *
     * @return The boolean value of floodgate var.
     */
    public boolean hasFloodgate() {
        return floodgate;
    }

    /**
     * Returns the instance of the FancyNPCs integration, if it is loaded.
     *
     * @return The {@code FancyNPCs} integration instance, or null if not loaded.
     */
    public FancyNPCs getFancyNpcs() {
        return fancyNpcs;
    }

    /**
     * Returns the instance of the CitizensNPC integration, if it is loaded.
     *
     * @return The {@code CitizensNPC} integration instance, or null if not loaded.
     */
    public CitizensNPC getCitizensNPC() {
        return citizensNPC;
    }

    /**
     * Returns the instance of the LuckPermsHandler, if it is loaded.
     *
     * @return The {@code LuckPermsHandler} instance, or null if not loaded.
     */
    public LuckPermsHandler getLuckPermsHandler() {
        return luckPermsHandler;
    }

    /**
     * Checks if MultiPaper integration is loaded.
     *
     * @return {@code true} if MultiPaper integration is loaded, {@code false} otherwise.
     */
    public boolean hasMultiPaper() {
        return multiPaper != null;
    }

    /**
     * Checks if Vault integration is loaded.
     *
     * @return {@code true} if Vault integration is loaded, {@code false} otherwise.
     */
    public boolean hasVault() {
        return vault != null;
    }

    /**
     * Checks if Vault permissions provider is available.
     *
     * @return {@code true} if Vault permissions provider is available, {@code false} otherwise.
     */
    public boolean hasVaultPermProvider() {
        return hasVaultPermissions;
    }

    /**
     * Checks if ProtocolLib integration is loaded.
     *
     * @return {@code true} if ProtocolLib integration is loaded, {@code false} otherwise.
     */
    public boolean hasProtocolLib() {
        return protocolLib != null;
    }

    /**
     * Checks if CoreProtect integration is loaded.
     *
     * @return {@code true} if CoreProtect integration is loaded, {@code false} otherwise.
     */
    public boolean hasCoreProtect() {
        return coreProtectIntegration != null;
    }

    /**
     * Checks if WorldEdit integration is loaded.
     *
     * @return {@code true} if WorldEdit integration is loaded, {@code false} otherwise.
     */
    public boolean hasWorldEdit() {
        return worldEdit != null;
    }

    /**
     * Checks if WorldGuard integration is loaded.
     *
     * @return {@code true} if WorldGuard integration is loaded, {@code false} otherwise.
     */
    public boolean hasWorldGuard() {
        return worldGuard != null;
    }

    /**
     * Checks if Towny integration is loaded.
     *
     * @return {@code true} if Towny integration is loaded, {@code false} otherwise.
     */
    public boolean hasTowny() {
        return towny != null;
    }

    /**
     * Checks if GriefDefender integration is loaded.
     *
     * @return {@code true} if GriefDefender integration is loaded, {@code false} otherwise.
     */
    public boolean hasGriefDefender() {
        return griefDefender != null;
    }

    /**
     * Checks if FurnitureLib integration is loaded.
     *
     * @return {@code true} if FurnitureLib integration is loaded, {@code false} otherwise.
     */
    public boolean hasFurnitureLib() {
        return furnitureLib != null;
    }

    /**
     * Checks if FurnitureEngine integration is loaded.
     *
     * @return {@code true} if FurnitureEngine integration is loaded, {@code false} otherwise.
     */
    public boolean hasFurnitureEngine() {
        return furnitureEngine != null;
    }

    /**
     * Checks if ProtectionLib integration is loaded.
     *
     * @return {@code true} if ProtectionLib integration is loaded, {@code false} otherwise.
     */
    public boolean hasProtectionLib() {
        return protectionLib != null;
    }

    /**
     * Checks if ItemsAdder integration is loaded.
     *
     * @return {@code true} if ItemsAdder integration is loaded, {@code false} otherwise.
     */
    public boolean hasItemsAdder() {
        return itemsAdder != null;
    }

    /**
     * Checks if Oraxen integration is loaded.
     *
     * @return {@code true} if Oraxen integration is loaded, {@code false} otherwise.
     */
    public boolean hasOraxen() {
        return oraxen != null;
    }

    /**
     * Checks if MiniMessage integration is loaded.
     *
     * @return {@code true} if MiniMessage integration is loaded, {@code false} otherwise.
     */
    public boolean hasMiniMessage() {
        return miniMessage != null;
    }

    /**
     * Checks if MineDown integration is loaded.
     *
     * @return {@code true} if MineDown integration is loaded, {@code false} otherwise.
     */
    public boolean hasMineDown() {
        return mineDown != null;
    }

    /**
     * Checks if ChestSort integration is loaded.
     *
     * @return {@code true} if ChestSort integration is loaded, {@code false} otherwise.
     */
    public boolean hasChestSort() {
        return chestSort != null;
    }

    /**
     * Checks if PlayerNPC integration is loaded.
     *
     * @return {@code true} if PlayerNPC integration is loaded, {@code false} otherwise.
     */
    public boolean hasPlayerNPC() {
        return playerNPC != null;
    }

    public boolean hasVaultEconomy() {
        return hasVaultEconomy;
    }

    /**
     * Checks if CitizensNPC integration is loaded.
     *
     * @return {@code true} if CitizensNPC integration is loaded, {@code false} otherwise.
     */
    public boolean hasCitizensNPC() {
        return citizensNPC != null;
    }

    /**
     * Checks if PlaceholderAPI integration is loaded.
     *
     * @return {@code true} if PlaceholderAPI integration is loaded, {@code false} otherwise.
     */
    public boolean hasPlaceholderAPI() {
        return placeholderAPI != null;
    }

    /**
     * Checks if Skript integration is loaded.
     *
     * @return {@code true} if Skript integration is loaded, {@code false} otherwise.
     */
    public boolean hasSkript() {
        return skriptImpl != null;
    }

    /**
     * Checks if LuckPermsHandler is loaded.
     *
     * @return {@code true} if LuckPermsHandler is loaded, {@code false} otherwise.
     */
    public boolean hasLuckPermsHandler() {
        return luckPermsHandler != null;
    }

    /**
     * Checks if FancyNpcs is loaded.
     *
     * @return {@code true} if FancyNpcs is loaded, {@code false} otherwise.
     */
    public boolean hasFancyNpcs() {
        return fancyNpcs != null;
    }

    /**
     * Checks if NoteBlockAPI is loaded.
     *
     * @return {@code true} if NoteBlockAPI is loaded, {@code false} otherwise.
     */
    public boolean hasNoteBlockAPI() {
        return hasNoteBlockAPI;
    }

    /**
     * Loads the MultiPaper integration if enabled in the configuration.
     */
    private void loadMultiPaper() {
        if (plugin.getConfig().getBoolean("settings.integration.multipaper.enabled", true)) {
            try {
                Class.forName("puregero.multipaper.MultiPaper", false, getClass().getClassLoader());

                multiPaper = new MultiPaper(plugin);

                plugin.infoMessage("MultiPaper detected, enabling MultiLib.");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            multiPaper = null;
        }
    }

    /**
     * Loads the Vault integration if enabled in the configuration.
     */
    private void loadVault() {
        if (plugin.getConfig().getBoolean("settings.integration.vault.enabled", true)) {
            Plugin vaultPlugin = plugin.getServer().getPluginManager().getPlugin("Vault");

            if (vaultPlugin != null && vaultPlugin.isEnabled()) {
                handleVaultIntegration(vaultPlugin);
            } else {
                resetVaultIntegration();
            }
        } else {
            resetVaultIntegration();
        }
    }

    /**
     * Handles the integration of the Vault plugin.
     *
     * @param vaultPlugin The Vault plugin instance.
     */
    private void handleVaultIntegration(Plugin vaultPlugin) {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (economyProvider != null && permissionProvider != null) {
            enableFullVaultIntegration(vaultPlugin, economyProvider, permissionProvider);
        } else if (economyProvider != null) {
            enableEconomyOnlyVaultIntegration(vaultPlugin, economyProvider);
        } else if (permissionProvider != null) {
            enablePermissionsOnlyVaultIntegration(vaultPlugin, permissionProvider);
        } else {
            disableVaultIntegration(vaultPlugin);
        }
    }

    /**
     * Enables full Vault integration with both economy and permissions.
     *
     * @param vaultPlugin      The Vault plugin instance.
     * @param economyProvider  The economy service provider.
     * @param permissionProvider The permissions service provider.
     */
    private void enableFullVaultIntegration(Plugin vaultPlugin, RegisteredServiceProvider<Economy> economyProvider, RegisteredServiceProvider<Permission> permissionProvider) {
        Economy economy = economyProvider.getProvider();
        Permission permission = permissionProvider.getProvider();
        vault = new Vault(economy, permission);
        hasVaultPermissions = true;
        hasVaultEconomy = true;

        plugin.integrationMessage("Hooked into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + ". Economy is enabled.");
        plugin.integrationMessage("Hooked into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + "'s permissions provider.");
    }

    /**
     * Enables Vault integration with only economy support.
     *
     * @param vaultPlugin     The Vault plugin instance.
     * @param economyProvider The economy service provider.
     */
    private void enableEconomyOnlyVaultIntegration(Plugin vaultPlugin, RegisteredServiceProvider<Economy> economyProvider) {
        Economy economy = economyProvider.getProvider();
        vault = new Vault(economy);
        hasVaultPermissions = false;
        hasVaultEconomy = true;

        plugin.integrationMessage("Hooked into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + ". Economy is enabled.");
        plugin.getLogger().severe("Failed to hook into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + "'s permissions provider. Vault will not be used as a Permissions Provider.");
    }

    /**
     * Enables Vault integration with only permissions support.
     *
     * @param vaultPlugin       The Vault plugin instance.
     * @param permissionProvider The permissions service provider.
     */
    private void enablePermissionsOnlyVaultIntegration(Plugin vaultPlugin, RegisteredServiceProvider<Permission> permissionProvider) {
        Permission permission = permissionProvider.getProvider();
        vault = new Vault(permission);
        hasVaultPermissions = true;
        hasVaultEconomy = false;

        plugin.integrationMessage("Failed to hook into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + "'s economy. This is likely because you are missing an economy plugin. Economy will be disabled.", "severe");
        plugin.integrationMessage("Hooked into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + "'s permissions provider.");
    }

    /**
     * Disables the Vault integration if both economy and permissions are unavailable.
     *
     * @param vaultPlugin The Vault plugin instance.
     */
    private void disableVaultIntegration(Plugin vaultPlugin) {
        vault = null;
        hasVaultPermissions = false;
        hasVaultEconomy = false;

        plugin.integrationMessage("Failed to hook into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + "'s economy. This is likely because you are missing an economy plugin. Economy will be disabled.", "severe");
        plugin.integrationMessage("Failed to hook into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + "'s permissions provider. Vault will not be used as a Permissions Provider.", "severe");
    }

    /**
     * Resets the Vault integration by setting the vault and permissions to null.
     */
    private void resetVaultIntegration() {
        vault = null;
        hasVaultPermissions = false;
    }

    /**
     * Loads the ProtocolLib integration if enabled in the configuration.
     */
    private void loadProtocolLib() {
        if (plugin.getConfig().getBoolean("settings.integration.protocollib.enabled", true)) {
            Plugin protocolLibPlugin = plugin.getServer().getPluginManager().getPlugin("ProtocolLib");

            if (protocolLibPlugin != null && protocolLibPlugin.isEnabled()) {
                protocolLib = new ProtocolLib(plugin);

                plugin.integrationMessage("Hooked into " + protocolLibPlugin.getName() + " " + protocolLibPlugin.getDescription().getVersion() + ".");
            }
        } else {
            protocolLib = null;
        }
    }

    /**
     * Loads the WorldGuard integration if enabled in the configuration.
     */
    public void loadWorldGuard() {
        if (plugin.getConfig().getBoolean("settings.integration.worldguard.enabled", true)) {
            Plugin worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

            if (worldGuardPlugin != null) {
                try {
                    Class.forName("com.sk89q.worldguard.WorldGuard", false, getClass().getClassLoader());
                    Class.forName("com.sk89q.worldguard.protection.flags.registry.FlagConflictException", false, getClass().getClassLoader());

                    worldGuard = new WorldGuard(plugin);

                    plugin.integrationMessage("Hooked into " + worldGuardPlugin.getName() + " " + worldGuardPlugin.getDescription().getVersion() + ".");
                } catch (ClassNotFoundException ignored) {
                    plugin.integrationMessage(worldGuardPlugin.getName() + " " + worldGuardPlugin.getDescription().getVersion() + " detected, Only WorldGuard 6.2+ is supported. Disabling WorldGuard support.");
                }
            }
        } else {
            worldGuard = null;
        }
    }

    /**
     * Loads the Towny integration if enabled in the configuration.
     */
    public void loadTowny() {
        if (plugin.getConfig().getBoolean("settings.integration.towny.enabled", true)) {
            Plugin townyPlugin = plugin.getServer().getPluginManager().getPlugin("Towny");

            if (townyPlugin != null) {
                towny = new Towny(plugin, townyPlugin);

                plugin.integrationMessage("Hooked into " + townyPlugin.getName() + " " + townyPlugin.getDescription().getVersion() + ".");
            }
        } else {
            towny = null;
        }
    }

    /**
     * Loads the WorldEdit integration if enabled in the configuration.
     */
    private void loadWorldEdit() {
        if (plugin.getConfig().getBoolean("settings.integration.worldedit.enabled", true)) {
            Plugin worldEditPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");

            if (worldEditPlugin != null && worldEditPlugin.isEnabled()) {
                try {
                    Class.forName("com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats", false, getClass().getClassLoader());

                    worldEdit = new WorldEdit(plugin, worldEditPlugin);

                    plugin.integrationMessage("Hooked into " + worldEditPlugin.getName() + " " + worldEditPlugin.getDescription().getVersion() + ".");
                } catch (ClassNotFoundException ignored) {
                    plugin.integrationMessage(worldEditPlugin.getName() + " " + worldEditPlugin.getDescription().getVersion() + " detected, Only WorldEdit 7+ is supported. Disabling WorldEdit support.", "severe");
                }
            }
        } else {
            worldEdit = null;
        }
    }

    /**
     * Loads CoreProtect integration if enabled in the configuration and CoreProtect is installed.
     */
    private void loadCoreProtect() {
        if (plugin.getConfig().getBoolean("settings.integration.coreprotect.enabled", true)) {
            Plugin coreProtectPlugin = plugin.getServer().getPluginManager().getPlugin("CoreProtect");

            if (coreProtectPlugin != null && coreProtectPlugin.isEnabled()) {
                try {
                    coreProtectIntegration = new CoreProtectIntegration(plugin);
                    plugin.getServer().getPluginManager().registerEvents(new CoreProtectListener(plugin), plugin);
                    plugin.integrationMessage("Hooked into " + coreProtectPlugin.getName() + " " + coreProtectPlugin.getDescription().getVersion() + ".");
                } catch (Exception e) {
                    coreProtectIntegration = null;
                    plugin.integrationMessage("Failed to hook into " + coreProtectPlugin.getName() + " " + coreProtectPlugin.getDescription().getVersion() + ". Is CoreProtect installed and enabled?", "severe");
                    plugin.logStackTrace(e);
                }
            }
        } else {
            coreProtectIntegration = null;
        }
    }

    /**
     * Loads the GriefDefender integration if enabled in the configuration.
     */
    private void loadGriefDefender() {
        if (plugin.getConfig().getBoolean("settings.integration.griefdefender.enabled", true)) {
            Plugin griefDefenderPlugin = plugin.getServer().getPluginManager().getPlugin("GriefDefender");

            if (griefDefenderPlugin != null && griefDefenderPlugin.isEnabled()) {
                griefDefender = new GriefDefender();

                plugin.integrationMessage("Hooked into " + griefDefenderPlugin.getName() + " " + griefDefenderPlugin.getDescription().getVersion() + ".");
            }
        } else {
            griefDefender = null;
        }
    }

    /**
     * Loads the FurnitureLib integration if enabled in the configuration.
     */
    private void loadFurnitureLib() {
        if (plugin.getConfig().getBoolean("settings.integration.furniturelib.enabled", true)) {
            Plugin furnitureLibPlugin = plugin.getServer().getPluginManager().getPlugin("FurnitureLib");

            if (furnitureLibPlugin != null && furnitureLibPlugin.isEnabled()) {
                furnitureLib = new FurnitureLib(plugin);

                plugin.integrationMessage("Hooked into " + furnitureLibPlugin.getName() + " " + furnitureLibPlugin.getDescription().getVersion() + ".");
            }
        } else {
            furnitureLib = null;
        }
    }

    /**
     * Loads the FurnitureEngine integration if enabled in the configuration.
     */
    private void loadFurnitureEngine() {
        if (plugin.getConfig().getBoolean("settings.integration.furnitureengine.enabled", true)) {
            Plugin furnitureEnginePlugin = plugin.getServer().getPluginManager().getPlugin("FurnitureEngine");

            if (furnitureEnginePlugin != null && furnitureEnginePlugin.isEnabled()) {
                try {
                    Class.forName("com.mira.furnitureengine.furniture.FurnitureManager", false, getClass().getClassLoader());

                    furnitureEngine = new FurnitureEngine(plugin);

                    plugin.integrationMessage("Hooked into " + furnitureEnginePlugin.getName() + " " + furnitureEnginePlugin.getDescription().getVersion() + ".");
                } catch (ClassNotFoundException ignored) {
                    plugin.integrationMessage(furnitureEnginePlugin.getName() + " " + furnitureEnginePlugin.getDescription().getVersion() + " detected, but FurnitureManager class not found, disabling integration.", "severe");
                }
            }
        } else {
            furnitureEngine = null;
        }
    }

    /**
     * Loads the ProtectionLib integration if enabled in the configuration.
     */
    private void loadProtectionLib() {
        if (plugin.getConfig().getBoolean("settings.integration.protectionlib.enabled", true)) {
            Plugin protectionLibPlugin = plugin.getServer().getPluginManager().getPlugin("ProtectionLib");

            if (protectionLibPlugin != null && protectionLibPlugin.isEnabled()) {
                protectionLib = new ProtectionLib(plugin, protectionLibPlugin);

                plugin.integrationMessage("Hooked into " + protectionLibPlugin.getName() + " " + protectionLibPlugin.getDescription().getVersion() + ".");
            }
        } else {
            protectionLib = null;
        }
    }

    /**
     * Loads the ItemsAdder integration if enabled in the configuration.
     */
    private void loadItemsAdder() {
        if (plugin.getConfig().getBoolean("settings.integration.itemsadder.enabled", true)) {
            Plugin itemsAdderPlugin = plugin.getServer().getPluginManager().getPlugin("ItemsAdder");

            if (itemsAdderPlugin != null && itemsAdderPlugin.isEnabled()) {
                itemsAdder = new ItemsAdder(plugin, itemsAdderPlugin);
                plugin.getServer().getPluginManager().registerEvents(new FurnitureBreakListener(plugin), plugin);
                plugin.getServer().getPluginManager().registerEvents(new CustomBlockBreakListener(plugin), plugin);

                plugin.integrationMessage("Hooked into " + itemsAdderPlugin.getName() + " " + itemsAdderPlugin.getDescription().getVersion() + ".");
            }
        } else {
            itemsAdder = null;
        }
    }

    /**
     * Loads the Oraxen integration if enabled in the configuration.
     */
    private void loadOraxen() {
        if (plugin.getConfig().getBoolean("settings.integration.oraxen.enabled", true)) {
            Plugin oraxenPlugin = plugin.getServer().getPluginManager().getPlugin("Oraxen");

            if (oraxenPlugin != null && oraxenPlugin.isEnabled()) {
                oraxen = new Oraxen(plugin, oraxenPlugin);

                plugin.integrationMessage("Hooked into " + oraxenPlugin.getName() + " " + oraxenPlugin.getDescription().getVersion() + ".");
            }
        } else {
            oraxen = null;
        }
    }

    /**
     * Loads the MiniMessage integration if enabled in the configuration.
     */
    private void loadMiniMessage() {
        if (plugin.getConfig().getBoolean("settings.integration.minimessage.enabled", true)) {
            try {
                Class.forName("com.ranull.graves.libraries.kyori.adventure.text.minimessage.MiniMessage", false, getClass().getClassLoader());
                Class.forName("com.ranull.graves.libraries.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer", false, getClass().getClassLoader());

                miniMessage = new MiniMessage();

                plugin.integrationMessage("Hooked into MiniMessage.");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            miniMessage = null;
        }
    }

    /**
     * Loads the MineDown integration if enabled in the configuration.
     */
    private void loadMineDown() {
        if (miniMessage == null) return;
        if (plugin.getConfig().getBoolean("settings.integration.minedown.enabled", true)) {
            try {
                Class.forName("com.ranull.graves.libraries.minedown.adventure.MineDown", false, getClass().getClassLoader());
                Class.forName("com.ranull.graves.libraries.minedown.adventure.MineDownParser", false, getClass().getClassLoader());
                mineDown = new MineDown();

                plugin.integrationMessage("Hooked into Minedown Adventure.");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            mineDown = null;
        }
    }

    /**
     * Loads the ChestSort integration if enabled in the configuration.
     */
    private void loadChestSort() {
        if (plugin.getConfig().getBoolean("settings.integration.chestsort.enabled", true)) {
            Plugin chestSortPlugin = plugin.getServer().getPluginManager().getPlugin("ChestSort");

            if (chestSortPlugin != null && chestSortPlugin.isEnabled()) {
                chestSort = new ChestSort();

                plugin.integrationMessage("Hooked into " + chestSortPlugin.getName() + " " + chestSortPlugin.getDescription().getVersion() + ".");
            }
        } else {
            chestSort = null;
        }
    }

    /**
     * Loads the PlayerNPC integration if enabled in the configuration.
     */
    private void loadPlayerNPC() {
        if (plugin.getConfig().getBoolean("settings.integration.playernpc.enabled", true)) {
            Plugin playerNPCPlugin = plugin.getServer().getPluginManager().getPlugin("PlayerNPC");

            if (playerNPCPlugin != null && playerNPCPlugin.isEnabled()) {
                playerNPC = new PlayerNPC(plugin);

                plugin.integrationMessage("Hooked into " + playerNPCPlugin.getName() + " " + playerNPCPlugin.getDescription().getVersion() + ".");
            }
        } else {
            playerNPC = null;
        }
    }

    /**
     * Loads the FancyNpcs integration if enabled in the configuration.
     */
    private void loadFancyNpcs() {
        if (plugin.getConfig().getBoolean("settings.integration.fancynpcs.enabled")) {
            Plugin FancyNPCPlugin = plugin.getServer().getPluginManager().getPlugin("FancyNpcs");

            if (FancyNPCPlugin != null && FancyNPCPlugin.isEnabled()) {
                fancyNpcs = new FancyNPCs(plugin);

                plugin.integrationMessage("Hooked into " + FancyNPCPlugin.getName() + " "
                        + FancyNPCPlugin.getDescription().getVersion() + ".");
            }
        } else {
            fancyNpcs = null;
        }
    }

    /**
     * Loads the CitizensNPC integration if enabled in the configuration.
     */
    private void loadCitizensNPC() {
        if (plugin.getConfig().getBoolean("settings.integration.citizens.enabled", true)) {
            Plugin citizensPlugin = plugin.getServer().getPluginManager().getPlugin("Citizens");

            if (citizensPlugin != null && citizensPlugin.isEnabled()) {
                citizensNPC = new CitizensNPC(plugin);

                plugin.integrationMessage("Hooked into " + citizensPlugin.getName() + " " + citizensPlugin.getDescription().getVersion() + ".");
            }
        } else {
            citizensNPC = null;
        }
    }

    /**
     * Loads the ItemBridge integration if enabled in the configuration.
     */
    private void loadItemBridge() {
        if (plugin.getConfig().getBoolean("settings.integration.itembridge.enabled", true)) {
            Plugin itemBridgePlugin = plugin.getServer().getPluginManager().getPlugin("ItemBridge");

            if (itemBridgePlugin != null && itemBridgePlugin.isEnabled()) {
                if (itemBridge == null) {
                    itemBridge = new ItemBridge(plugin);
                }

                plugin.integrationMessage("Hooked into " + itemBridgePlugin.getName() + " " + itemBridgePlugin.getDescription().getVersion() + ".");
            }
        } else {
            itemBridge = null;
        }
    }

    /**
     * Loads the PlaceholderAPI integration if enabled in the configuration.
     */
    private void loadPlaceholderAPI() {
        if (placeholderAPI != null) {
            placeholderAPI.unregister();
        }

        if (plugin.getConfig().getBoolean("settings.integration.placeholderapi.enabled", true)) {
            Plugin placeholderAPIPlugin = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");

            if (placeholderAPIPlugin != null && placeholderAPIPlugin.isEnabled()) {
                placeholderAPI = new PlaceholderAPI(plugin);

                placeholderAPI.register();

                plugin.integrationMessage("Hooked into " + placeholderAPIPlugin.getName() + " " + placeholderAPIPlugin.getDescription().getVersion() + ".");
            }
        } else {
            placeholderAPI = null;
        }
    }

    /**
     * Loads the Skript integration if enabled in the configuration.
     */
    private void loadSkript() {
        if (plugin.getConfig().getBoolean("settings.integration.skript.enabled", true)) {
            Plugin skriptPlugin = plugin.getServer().getPluginManager().getPlugin("Skript");
            if (skriptPlugin != null && skriptPlugin.isEnabled()) {
                skriptImpl = new SkriptImpl(plugin);
                plugin.integrationMessage("Hooked into " + skriptPlugin.getName() + " " + skriptPlugin.getDescription().getVersion() + ".");
            }
        } else {
            skriptImpl = null;
        }
    }

    /**
     * Loads the LuckPerms integration if enabled in the configuration.
     */
    private void loadLuckPerms() {
        if (plugin.getConfig().getBoolean("settings.integration.luckperms.enabled", true)) {
            Plugin luckPermsPlugin = plugin.getServer().getPluginManager().getPlugin("LuckPerms");
            try {
                if (luckPermsPlugin != null && luckPermsPlugin.isEnabled()) {
                    luckPermsHandler = new LuckPermsHandler();
                    plugin.integrationMessage("Hooked into " + luckPermsPlugin.getName() + " " + luckPermsPlugin.getDescription().getVersion() + ".");
                }
            } catch (IllegalArgumentException exception) {
                plugin.integrationMessage("Failed to Hook into " + luckPermsPlugin.getName() + " " + luckPermsPlugin.getDescription().getVersion() + ". LuckPerms will not be used as a Permissions Provider.", "severe");
                luckPermsHandler = null;
            }
        } else {
            luckPermsHandler = null;
        }
    }

    /**
     * Loads the LuckPerms integration if enabled in the configuration.
     */
    private void loadBedrockSupport() {
        Plugin floodgatePlugin = plugin.getServer().getPluginManager().getPlugin("floodgate");
        Plugin geysermcPlugin = plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot");

        if (geysermcPlugin != null && geysermcPlugin.isEnabled()) {
            if (floodgatePlugin != null && floodgatePlugin.isEnabled()) {
                plugin.integrationMessage("Hooked into " + geysermcPlugin.getName() + " " + geysermcPlugin.getDescription().getVersion() + ".");
                plugin.integrationMessage("Hooked into " + floodgatePlugin.getName() + " " + floodgatePlugin.getDescription().getVersion() + ".");
                floodgate = true;
            } else if (floodgatePlugin != null && !floodgatePlugin.isEnabled()) {
                plugin.integrationMessage("Hooked into " + geysermcPlugin.getName() + " " + geysermcPlugin.getDescription().getVersion() + ".");
                plugin.integrationMessage("Failed to Hook into " + floodgatePlugin.getName() + " " + floodgatePlugin.getDescription().getVersion() + ".", "severe");
                floodgate = false;
            } else {
                floodgate = false;
            }
        } else {
            if (floodgatePlugin != null && floodgatePlugin.isEnabled()) {
                plugin.integrationMessage("Failed to hook into Geyser-Spigot. Assuming the server runs behind a proxy.", "warning");
                plugin.integrationMessage("Hooked into " + floodgatePlugin.getName() + " " + floodgatePlugin.getDescription().getVersion() + ".");
                floodgate = true;
            } else {
                floodgate = false;
            }
        }
    }

    /**
     * Prints in console if NBTAPI is loaded to let the user know that NBT API will handle inventory storage.
     */
    private void loadNBTAPI() {
        Plugin nbtAPI = plugin.getServer().getPluginManager().getPlugin("NBTAPI");

        if (nbtAPI != null && nbtAPI.isEnabled()) {
            noteBlockAPI = new NoteBlockAPI(plugin);
            plugin.integrationMessage("Hooked into " + nbtAPI.getName() + " " + nbtAPI.getDescription().getVersion() + ". Using " + nbtAPI.getName() + " "  + nbtAPI.getDescription().getVersion() +  " for handling Inventory NBT Data.");
        }
    }

    private void loadNoteblockAPI() {
        if (plugin.getConfig().getBoolean("settings.integration.noteblockapi.enabled", false)) {
            Plugin nbAPI = plugin.getServer().getPluginManager().getPlugin("NoteBlockAPI");

            if (nbAPI != null && nbAPI.isEnabled()) {
                try {
                    hasNoteBlockAPI = true;
                    noteBlockAPI = new NoteBlockAPI(plugin);
                    plugin.integrationMessage("Hooked into " + nbAPI.getName() + " " + nbAPI.getDescription().getVersion() + ".");
                    // Create the nbs folder if it doesn't exist
                    File nbsFolder = new File(plugin.getDataFolder(), "nbs");
                    if (!nbsFolder.exists()) {
                        if (!nbsFolder.mkdirs()) {
                            plugin.getLogger().warning("Failed to create /plugins/GravesX/nbs directory. You will need to make this folder manually.");
                        }
                    }
                } catch (Exception e) {
                    hasNoteBlockAPI = false;
                }
            } else {
                hasNoteBlockAPI = false;
            }
        } else {
            hasNoteBlockAPI = false;
        }
    }

    /**
     * Loads and displays warnings for compatibility issues with other plugins.
     */
    @SuppressWarnings("deprecation")
    private void loadCompatibilityWarnings() {
        if (plugin.getConfig().getBoolean("settings.compatibility.warning")) {
            for (World world : plugin.getServer().getWorlds()) {
                if (world.getGameRuleValue("keepInventory").equals("true")) {
                    plugin.compatibilityMessage("World \"" + world.getName() + "\" has keepInventory set to true, Graves will not be created here.");
                }
            }

            Plugin essentialsPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials");

            if (essentialsPlugin != null && essentialsPlugin.isEnabled()) {
                plugin.compatibilityMessage(essentialsPlugin.getName() + " Detected, make sure you don't have the essentials.keepinv or essentials.keepxp permissions.");
            }

            Plugin deluxeCombatPlugin = plugin.getServer().getPluginManager().getPlugin("DeluxeCombat");

            if (deluxeCombatPlugin != null && deluxeCombatPlugin.isEnabled()) {
                plugin.compatibilityMessage(deluxeCombatPlugin.getName() + " Detected, in order to work with graves you need to set disable-drop-handling to true in " + deluxeCombatPlugin.getName() + "'s data.yml file.");
            }

            try {
                Class.forName("ru.xezard.items.remover.ItemsRemoverPlugin");
                plugin.compatibilityMessage("XItemsRemover Detected. Plugin is known to leave [pdd] lore on all items. It is best to not modify plugin.yml to remove the loadsbefore option.");
            } catch (ClassNotFoundException ignore) {
                // ignore
            }

            checkForPluginManagers(); // Plugin Manager Jumpscare

            similarPluginWarning("DeadChest");
            similarPluginWarning("DeathChest");
            similarPluginWarning("DeathChestPro");
            similarPluginWarning("SavageDeathChest");
            similarPluginWarning("AngelChest");
        }
    }

    /**
     * Checks for known plugin managers that could cause compatibility issues.
     */
    public void checkForPluginManagers() {
        List<String> knownPluginManagers = Arrays.asList(
                "PluginManager",
                "PlugMan",
                "PlugManX",
                "WorldPluginManager",
                "AnthoPlugManager",
                "GlobalPlugins",
                "ProManager",
                "RestartManager",
                "UltimatePluginManager"
        );

        StringJoiner detectedPlugins = new StringJoiner(", ");

        for (String pluginManagerName : knownPluginManagers) {
            Plugin plugins = plugin.getServer().getPluginManager().getPlugin(pluginManagerName);
            if (plugins != null && plugins.isEnabled()) {
                detectedPlugins.add(plugins.getName() + " v." + plugins.getDescription().getVersion());
            }
        }

        if (detectedPlugins.length() > 0) {
            // Let owner know they are running a plugin manager
            plugin.getLogger().warning("Detected server is running a Plugin Manager based plugin: " + detectedPlugins);
            plugin.getLogger().warning("No support will be given if you use one of these plugins.");
        }
    }

    /**
     * Displays a warning message if a plugin with similar functionality to Graves is detected.
     *
     * @param string The name of the plugin to check for.
     */
    private void similarPluginWarning(String string) {
        Plugin similarPlugin = plugin.getServer().getPluginManager().getPlugin(string);

        if (similarPlugin != null && similarPlugin.isEnabled()) {
            plugin.compatibilityMessage(string + " Detected, Graves listens to the death event after " + string + ", and " + string + " clears the drop list. This means Graves will never be created for players if " + string + " is enabled, only non-player entities will create Graves if configured to do so.");
        }
    }
}