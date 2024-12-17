package com.ranull.graves;

import com.ranull.graves.command.GravesCommand;
import com.ranull.graves.compatibility.Compatibility;
import com.ranull.graves.compatibility.CompatibilityBlockData;
import com.ranull.graves.compatibility.CompatibilityMaterialData;
import com.ranull.graves.listener.*;
import com.ranull.graves.manager.*;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.*;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Graves extends JavaPlugin {
    private VersionManager versionManager;
    private IntegrationManager integrationManager;
    private CacheManager cacheManager;
    private DataManager dataManager;
    private ImportManager importManager;
    private BlockManager blockManager;
    private ItemStackManager itemStackManager;
    private EntityDataManager entityDataManager;
    private HologramManager hologramManager;
    private GUIManager guiManager;
    private EntityManager entityManager;
    private RecipeManager recipeManager;
    private LocationManager locationManager;
    private GraveManager graveManager;
    private ParticleManager particleManager;
    private Compatibility compatibility;
    private FileConfiguration fileConfiguration;
    private boolean isDevelopmentBuild = false;
    private boolean isOutdatedBuild = false;
    private boolean isUnknownBuild = false;

    @Override
    public void onLoad() {
        File gravesDirectory = new File(getDataFolder().getParentFile(), "Graves");
        File newGravesDirectory = new File(getDataFolder().getParentFile(), "GravesX");

        if (gravesDirectory.exists() && gravesDirectory.isDirectory()) {
            getLogger().warning("Your server has legacy version of Graves. Migrating the folder to GravesX for you.");
            if (gravesDirectory.renameTo(newGravesDirectory)) {
                getLogger().info("Successfully renamed legacy folder Graves to GravesX.");
            } else {
                getLogger().severe("Failed to rename legacy folder Graves to GravesX. Ensure the folder doesn't already exist.");
            }
        }

        saveDefaultConfig();

        integrationManager = new IntegrationManager(this);
    }

    @Override
    public void onEnable() {
        loadLibraries();

        integrationManager.load();
        integrationManager.loadNoReload();

        versionManager = new VersionManager();
        cacheManager = new CacheManager();
        dataManager = new DataManager(this);
        importManager = new ImportManager(this);
        blockManager = new BlockManager(this);
        itemStackManager = new ItemStackManager(this);
        entityDataManager = new EntityDataManager(this);
        hologramManager = new HologramManager(this);
        guiManager = new GUIManager(this);
        entityManager = new EntityManager(this);
        locationManager = new LocationManager(this);
        graveManager = new GraveManager(this);
        particleManager = new ParticleManager(this);

        registerCommands();
        registerListeners();
        registerRecipes();
        saveTextFiles();

        getServer().getScheduler().runTask(this, () -> {
            compatibilityChecker();
            updateConfig();
            updateChecker();
            RegisterSoftCrashHandler();
        });

        if (getConfig().getBoolean("settings.metrics.enabled", true)) {
            getLogger().info("Metrics has been enabled. All metrics will be sent to https://bstats.org/plugin/bukkit/Graves/12849 and https://bstats.org/plugin/bukkit/GravesX/23069.");
            registerMetrics();
            registerMetricsLegacy();
        } else {
            getLogger().warning("Metrics has been disabled. Metrics will not be sent.");
        }
    }

    @Override
    public void onDisable() {
        runShutdownTasks();
    }

    private void RegisterSoftCrashHandler() {
        getLogger().info("Registering Crash Handler...");
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                getLogger().severe("GravesX detected server went into a crashed state...");
                runShutdownTasks();
                getServer().getPluginManager().disablePlugin(this);
            }));
            getLogger().info("Registered Crash Handler. Server will handle crashes in a separate thread.");
        } catch (Exception e) {
            getLogger().severe("Failed to Register Crash Handler. Server will not listen to crashed states in a seperate thread.");
        }
    }

    private void runShutdownTasks() {
        getLogger().info("Shutting Down GravesX...");
        try {
            dataManager.closeConnection();
        } catch (Exception e) {
            getLogger().severe("Failed to close Database Connection. Cause:" + e.getCause());
        }

        getLogger().info("Unloading GraveManager...");
        try {
            graveManager.unload();
            getLogger().info("Unloaded GraveManager Successfully.");
        } catch (Exception e) {
            getLogger().severe("Failed to unload GraveManager. Cause: " + e.getCause());
        }

        getLogger().info("Unloading IntegrationManager...");
        try {
            integrationManager.unload();
            integrationManager.unloadNoReload();
            getLogger().info("Unloaded IntegrationManager Successfully.");
        } catch (Exception e) {
            getLogger().severe("Failed to unload IntegrationManager. Cause: " + e.getCause());
        }


        if (recipeManager != null) {
            getLogger().info("Unloading RecipeManager...");
            try {
                recipeManager.unload();
                getLogger().info("Unloaded RecipeManager Successfully.");
            } catch (Exception e) {
                getLogger().severe("Failed to unload RecipeManager. Cause: " + e.getCause());
            }
        }
        getLogger().info("Shutdown Completed :)");
    }

    private void loadLibraries() {
        getLogger().info("Loading Libraries for GravesX");

        LibraryLoaderUtil libraryLoaderUtil = new LibraryLoaderUtil(this);

        libraryLoaderUtil.loadLibrary("com{}zaxxer", "HikariCP", "6.0.0", "com{}zaxxer{}hikari", "com{}ranull{}graves{}libraries{}hikari", false);
        libraryLoaderUtil.loadLibrary("org{}xerial", "sqlite-jdbc", "3.47.0.0", false);

        try {
            Class.forName("org.json.JSONObject");
        } catch (ClassNotFoundException e) {
            libraryLoaderUtil.loadLibrary("org{}json", "json", "20240303");
        }

        try {
            Class.forName("com.google.gson.Gson");
        } catch (ClassNotFoundException e) {
            libraryLoaderUtil.loadLibrary("com{}google{}code{}gson", "gson", "2.10.1", false);
        }

        try {
            Class.forName("com.google.common.collect.ImmutableList");
        } catch (ClassNotFoundException e) {
            libraryLoaderUtil.loadLibrary("com{}google{}guava", "guava", "33.3.0-jre", false);
        }


        libraryLoaderUtil.loadLibrary("com{}github{}oshi", "oshi-core", "6.6.5", false);


        String storageType = Objects.requireNonNull(getConfig().getString("settings.storage.type")).toUpperCase();

        if ("POSTGRESQL".equals(storageType)) {
            libraryLoaderUtil.loadLibrary("org{}postgresql", "postgresql", "42.7.4", "org{}postgresql", "com{}ranull{}graves{}libraries{}postgresql", false);
        } else if ("MARIADB".equals(storageType)) {
            libraryLoaderUtil.loadLibrary("com{}mysql", "mysql-connector-j", "9.1.0", "com{}mysql", "com{}ranull{}graves{}libraries{}mysql", false);
            libraryLoaderUtil.loadLibrary("org{}mariadb{}jdbc", "mariadb-java-client", "3.5.1", "org{}mariadb", "com{}ranull{}graves{}libraries{}mariadb", false);
        } else if ("MYSQL".equals(storageType)) {
            libraryLoaderUtil.loadLibrary("com{}mysql", "mysql-connector-j", "9.1.0", "com{}mysql", "com{}ranull{}graves{}libraries{}mysql", false);
        } else if ("H2".equals(storageType)) {
            libraryLoaderUtil.loadLibrary("com{}h2database", "h2", "2.3.232", "org{}h2", "com{}ranull{}graves{}libraries{}h2", false);
        } else if ("MSSQL".equals(storageType)) {
            String jdbcVersion;

            try {
                Class.forName("java.nio.file.Files");
                jdbcVersion = "12.8.1.jre11";
            } catch (ClassNotFoundException e) {
                jdbcVersion = "12.8.1.jre8";
            }

            libraryLoaderUtil.loadLibrary("com{}microsoft{}sqlserver", "mssql-jdbc", jdbcVersion, "com{}microsoft", "com{}ranull{}graves{}libraries{}microsoft", false);
        }
        libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-api", "4.17.0", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
        libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-text-minimessage", "4.17.0", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
        libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-text-serializer-gson", "4.17.0", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
        libraryLoaderUtil.loadLibrary("net{}kyori", "adventure-platform-bukkit", "4.3.3", "net{}kyori", "com{}ranull{}graves{}libraries{}kyori", false);
        libraryLoaderUtil.loadLibrary("de{}themoep", "minedown-adventure", "1.7.3-graves", "de{}themoep{}minedown", "com{}ranull{}graves{}libraries{}minedown", false, "https://repo.cwhead.dev/repository/maven-public/");
        libraryLoaderUtil.loadLibrary("me{}imdanix", "MiniTranslator", "2.1-graves", "me{}imdanix{}text", "com{}ranull{}graves{}libraries{}imdanix{}text", false, "https://repo.cwhead.dev/repository/maven-public/", false);
        libraryLoaderUtil.loadLibrary("com{}github{}puregero", "multilib", "1.2.4", "com{}github{}puregero{}multilib", "com{}ranull{}graves{}libraries{}multilib", false, "https://repo.clojars.org/");
        libraryLoaderUtil.loadLibrary("org{}apache{}commons", "commons-text", "1.12.0", "org{}apache{}commons{}text", "com{}ranull{}graves{}libraries{}commonstext", false);

        getLogger().info("Finished Loading Libraries for GravesX.");
    }

    @Override
    public void saveDefaultConfig() {
        ResourceUtil.copyResources("config", getConfigFolder().getPath(), false, this);
    }

    @Override
    public void reloadConfig() {
        File singleConfigFile = new File(getDataFolder(), "config.yml");

        if (!singleConfigFile.exists()) {
            fileConfiguration = getConfigFiles(getConfigFolder());
        } else {
            fileConfiguration = getConfigFile(singleConfigFile);
            loadResourceDefaults(fileConfiguration, singleConfigFile.getName());
        }
    }

    @Override
    @NotNull
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }

        return fileConfiguration;
    }

    public void reload() {
        saveDefaultConfig();
        saveTextFiles();
        reloadConfig();
        updateConfig();
        unregisterListeners();
        registerListeners();
        // dataManager.reload();
        integrationManager.reload();
        try {
            registerRecipes();
        } catch (Exception e) {
            recipeManager.reload();
        }

        infoMessage(getName() + " reloaded.");
    }

    public void saveTextFiles() {
        ResourceUtil.copyResources("data/text/readme.txt", getDataFolder().getPath()
                + "/readme.txt", this);
        ResourceUtil.copyResources("data/text/placeholders.txt", getDataFolder().getPath()
                + "/placeholders.txt", this);

        if (integrationManager != null) {
            if (integrationManager.hasPlaceholderAPI()) {
                ResourceUtil.copyResources("data/text/placeholderapi.txt", getDataFolder().getPath()
                        + "/placeholderapi.txt", this);
            }

            if (integrationManager.hasFurnitureLib()) {
                ResourceUtil.copyResources("data/text/furniturelib.txt", getDataFolder().getPath()
                        + "/furniturelib.txt", this);
            }
        }
    }

    private void registerMetrics() {
        Metrics metrics = new Metrics((Plugin) this, getMetricsID());

        metrics.addCustomChart(new SingleLineChart("graves", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return cacheManager.getGraveMap().size();
            }
        }));

        metrics.addCustomChart(new SimplePie("permission_handler", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (getIntegrationManager().hasLuckPermsHandler()) {
                    return "LuckPerms";
                } else if (getIntegrationManager().hasVaultPermProvider()) {
                    return "Vault";
                } else {
                    return "Bukkit";
                }
            }
        }));

        metrics.addCustomChart(new SimplePie("database", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getDataManager().getType();
            }
        }));

        metrics.addCustomChart(new SimplePie("plugin_release", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (isDevelopmentBuild) {
                    return "Development Build";
                } else if (isOutdatedBuild) {
                    return "Outdated Build";
                } else if (isUnknownBuild) {
                    return "Unknown Build";
                } else {
                    return "Production Build";
                }
            }
        }));

        metrics.addCustomChart(new DrilldownPie("database_versions", new Callable<Map<String, Map<String, Integer>>>() {
            @Override
            public Map<String, Map<String, Integer>> call() throws Exception {
                return getDataManager().getDatabaseVersions();
            }
        }));
    }

    private void registerMetricsLegacy() {
        Metrics metricsLegacy = new Metrics((Plugin) this, getMetricsIDLegacy());

        metricsLegacy.addCustomChart(new SingleLineChart("graves", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return cacheManager.getGraveMap().size();
            }
        }));

        metricsLegacy.addCustomChart(new SimplePie("permission_handler", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (getIntegrationManager().hasLuckPermsHandler()) {
                    return "LuckPerms";
                } else if (getIntegrationManager().hasVaultPermProvider()) {
                    return "Vault";
                } else {
                    return "Bukkit";
                }
            }
        }));

        metricsLegacy.addCustomChart(new SimplePie("database", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getDataManager().getType();
            }
        }));

        metricsLegacy.addCustomChart(new SimplePie("plugin_release", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (isDevelopmentBuild) {
                    return "Development Build";
                } else {
                    return "Production Build";
                }
            }
        }));

        metricsLegacy.addCustomChart(new DrilldownPie("database_versions", new Callable<Map<String, Map<String, Integer>>>() {
            @Override
            public Map<String, Map<String, Integer>> call() throws Exception {
                return getDataManager().getDatabaseVersions();
            }
        }));
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBucketListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityExplodeListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBurnAndIgniteListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockFromToListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPistonExtendListener(this), this);
        getServer().getPluginManager().registerEvents(new HangingBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryDragListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionPrimeListener(this), this);

        if (!versionManager.is_v1_7()) {
            getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(this), this);
        }

        if (!versionManager.is_v1_7() && !versionManager.is_v1_8()) {
            getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);
        }

        //getServer().getPluginManager().registerEvents(new GraveTestListener(this), this); // Test Listener
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    private void registerRecipes() {
        if (versionManager.hasPersistentData() && !versionManager.isMohist()) {
            recipeManager = new RecipeManager(this);
        }
    }

    private void registerCommands() {
        PluginCommand gravesPluginCommand = getCommand("graves");

        if (gravesPluginCommand != null) {
            GravesCommand gravesCommand = new GravesCommand(this);

            gravesPluginCommand.setExecutor(gravesCommand);
            gravesPluginCommand.setTabCompleter(gravesCommand);
        }
    }

    public void debugMessage(String string, int level) {
        if (getConfig().getInt("settings.debug.level", 0) >= level) {
            getLogger().warning("Debug: " + string);

            for (String admin : getConfig().getStringList("settings.debug.admin")) {
                Player player = getServer().getPlayer(admin);
                UUID uuid = UUIDUtil.getUUID(admin);

                if (uuid != null) {
                    Player uuidPlayer = getServer().getPlayer(uuid);

                    if (uuidPlayer != null) {
                        player = uuidPlayer;
                    }
                }

                if (player != null) {
                    String debug = !integrationManager.hasMultiPaper() ? "Debug:" : "Debug ("
                            + integrationManager.getMultiPaper().getLocalServerName() + "):";

                    player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + debug
                            + ChatColor.RESET + " " + string);
                }
            }
        }
    }

    public void warningMessage(String string) {
        getLogger().warning("Warning: " + string);
    }

    public void compatibilityMessage(String string) {
        getLogger().warning("Compatibility: " + string);
    }

    public void infoMessage(String string) {
        getLogger().info("Information: " + string);
    }

    public void testMessage(String string) {
        getLogger().info("Test: " + string);
    }

    public void updateMessage(String string) {
        getLogger().info("Update: " + string);
    }

    public void integrationMessage(String string) {
        integrationMessage(string, "info");
    }

    public void integrationMessage(String string, String messageType) {
        switch (messageType) {
            case "warn":
                getLogger().warning("Integration: " + string);
                break;
            case "severe":
            case "error":
                getLogger().severe("Integration: " + string);
                break;
            case "info":
            default:
                getLogger().info("Integration: " + string);
                break;
        }

    }

    private void updateConfig() {
        int currentConfigVersion = 18;
        File configFolder = new File(getDataFolder(), "config");

        // Load the main config file to check the version
        File mainConfigFile = new File(configFolder, "config.yml");
        FileConfiguration mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);
        int configVersion = mainConfig.getInt("config-version", 0);

        if (configVersion != currentConfigVersion) {
            // Create the outdated folder if it doesn't exist
            new File(getDataFolder(), "outdated").mkdirs();

            // Backup the outdated config files
            backupOutdatedConfigs(configVersion);

            // Log a warning message
            warningMessage("Outdated config detected (v" + configVersion + "), current version is (v"
                    + currentConfigVersion + "). Moving old configs to outdated folder and generating new config files.");

            // Update each config file directly
            updateConfigFile("config.yml", currentConfigVersion, true);
            updateConfigFile("entity.yml", currentConfigVersion, false);
            updateConfigFile("grave.yml", currentConfigVersion, false);

            // Reload the main config after all updates
            reloadConfig();
        }
    }

    private void backupOutdatedConfigs(double configVersion) {
        File configFolder = new File(getDataFolder(), "config");
        String[] configFiles = {"config.yml", "entity.yml", "grave.yml"};

        File outdatedFolder = new File(getDataFolder(), "outdated");
        outdatedFolder.mkdirs(); // Ensure the directory exists

        for (String configFileName : configFiles) {
            File configFile = new File(configFolder, configFileName);
            if (configFile.exists()) {
                File backupFile = new File(outdatedFolder, configFileName + "-" + configVersion);
                try {
                    Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    logStackTrace(e);
                }
            }
        }
    }

    private void updateConfigFile(String fileName, int currentConfigVersion, boolean shouldUpdateConfigVersion) {
        File configFile = new File(getDataFolder(), "config/" + fileName);
        if (configFile.exists()) {
            try {
                // Use ConfigUpdater to update the file
                String resourceName = "config/" + fileName;
                InputStream resourceStream = getResource(resourceName);

                if (resourceStream == null) {
                    getLogger().warning("Resource " + resourceName + " not found in the JAR.");
                    return;
                }

                // Update configuration
                ConfigUpdater.update(
                        this,             // Pass the plugin instance
                        resourceName,           // Resource name in JAR
                        configFile,             // File to update
                        Collections.emptyList() // Empty list if no sections to ignore
                );

                if (shouldUpdateConfigVersion) {
                    // Load the updated config and set the new version
                    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                    config.set("config-version", currentConfigVersion);
                    config.save(configFile);
                }

                getLogger().info("Config updated: " + configFile.getAbsolutePath());

            } catch (IOException e) {
                getLogger().severe("Failed to update " + fileName + ": " + e.getMessage());
                logStackTrace(e);
            }
        } else {
            getLogger().severe("File " + configFile.getAbsolutePath() + " does not exist.");
        }
    }

    private void updateChecker() {
        if (getConfig().getBoolean("settings.update.check")) {
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                String latestVersion = getLatestVersion();
                String installedVersion = getDescription().getVersion();

                // Debugging statements
                //getLogger().info("Installed Version: " + installedVersion);
                //getLogger().info("Latest Version: " + latestVersion);

                if (latestVersion != null && !installedVersion.equalsIgnoreCase(latestVersion)) {
                    try {
                        int comparisonResult = compareVersions(installedVersion, latestVersion);
                        // getLogger().info("Version Comparison Result: " + comparisonResult);

                        if (comparisonResult < 0) {
                            isOutdatedBuild = true;
                            getLogger().warning("You are using an outdated version of " + getDescription().getName() + ".");
                            getLogger().warning("Installed Version: " + installedVersion);
                            getLogger().warning("Latest Version:  " + latestVersion);
                            getLogger().warning("Grab the latest release from https://www.spigotmc.org/resources/" + getSpigotID() + "/");
                        } else if (comparisonResult > 0) {
                            isDevelopmentBuild = true;
                            getLogger().severe("You are running " + getDescription().getName() + " version " + installedVersion + ", which is a development build and is not production safe.");
                            getLogger().severe("THERE WILL NOT BE SUPPORT IF YOU LOSE GRAVE DATA FROM DEVELOPMENT OR COMPILED BUILDS. THIS BUILD IS FOR TESTING PURPOSES ONLY");
                            getLogger().severe("Keep note that you are using a development version when you report bugs.");
                            getLogger().severe("If the same issue occurs in "  + latestVersion + ", then let us know in https://discord.ranull.com/.");
                        } else {
                            getLogger().info("You are running the latest version of " + getDescription().getName() + ".");
                        }
                    } catch (NumberFormatException exception) {
                        isUnknownBuild = true;
                        getLogger().severe("NumberFormatException: " + exception.getMessage());
                        if (!installedVersion.equalsIgnoreCase(latestVersion)) {
                            getLogger().severe("You are either running an outdated version of " + getDescription().getName() + " or a development version.");
                            getLogger().severe("Installed Version: " + installedVersion);
                            getLogger().severe("Latest Version:  " + latestVersion);
                        }
                    }
                }
            });
        }
    }

    private int compareVersions(String version1, String version2) {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");

        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++) {
            int v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            int v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            if (v1 < v2) {
                return -1;
            }
            if (v1 > v2) {
                return 1;
            }
        }
        return 0;
    }

    private void compatibilityChecker() {
        compatibility = versionManager.hasBlockData() ? new CompatibilityBlockData() : new CompatibilityMaterialData();

        if (!versionManager.hasBlockData()) {
            infoMessage("Legacy version detected, Graves will run but may have problems with material names, " +
                    "the default config is setup for the latest version of the game, you can alter the config manually to fix " +
                    "any issues you encounter, you will need to find the names of materials and sounds for your version.");
        }

        if (versionManager.isBukkit()) {
            infoMessage("Bukkit detected, some functions won't work on Bukkit, like hex codes.");
        }

        if (versionManager.isMohist()) {
            infoMessage("Mohist detected, not injecting custom recipes. We also do not recommend Mohist nor will provide support if something goes wrong. Read here why: https://essentialsx.net/do-not-use-mohist.html");
        }
    }

    public void dumpServerInfo(CommandSender commandSender) {
        if (isEnabled()) {
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                String serverDumpInfo = ServerUtil.getServerDumpInfo(this);
                String message = serverDumpInfo;

                if (getConfig().getString("settings.dump.method", "MCLOGS").equalsIgnoreCase("MCLOGS") ||
                    getConfig().getString("settings.dump.method", "MCLOGS").equalsIgnoreCase("HASTEBIN")) {
                    String response = MclogsUtil.postLogToMclogs(serverDumpInfo);

                    if (response != null) {
                        message = response;
                        getLogger().info("Log uploaded successfully. URL: " + response);
                    } else {
                        getLogger().warning("Log upload failed. No response received.");
                    }
                }

                if (serverDumpInfo.equals(message)) {
                    try {
                        // Create the directory if it doesn't exist
                        File dumpDir = new File(getDataFolder(), "dump");
                        if (!dumpDir.exists()) {
                            dumpDir.mkdirs();
                        }

                        // Create the file within the dump directory
                        String name = "graves-dump-" + System.currentTimeMillis() + ".txt";
                        File dumpFile = new File(dumpDir, name);

                        PrintWriter printWriter = new PrintWriter(dumpFile, "UTF-8");
                        printWriter.write(serverDumpInfo);
                        printWriter.close();

                        message = dumpFile.getAbsolutePath();
                    } catch (FileNotFoundException | UnsupportedEncodingException exception) {
                        logStackTrace(exception);
                    }
                }

                commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET
                        + "Dumped: " + message);
            });
        }
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public GraveManager getGraveManager() {
        return graveManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public ItemStackManager getItemStackManager() {
        return itemStackManager;
    }

    public EntityDataManager getEntityDataManager() {
        return entityDataManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ImportManager getImportManager() {
        return importManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public Compatibility getCompatibility() {
        return compatibility;
    }

    public String getPluginReleaseType() {
        if (isDevelopmentBuild) {
            return "Development Build";
        } else if (isOutdatedBuild) {
            return "Outdated Build";
        } else if (isUnknownBuild) {
            return "Unknown Build";
        } else {
            return "Production Build";
        }
    }

    public ConfigurationSection getConfig(String config, Grave grave) {
        return getConfig(config, grave.getOwnerType(), grave.getPermissionList());
    }

    public ConfigurationSection getConfig(String config, Entity entity) {
        return getConfig(config, entity.getType(), getPermissionList(entity));
    }

    public ConfigurationSection getConfig(String config, Entity entity, List<String> permissionList) {
        return getConfig(config, entity.getType(), permissionList);
    }

    public ConfigurationSection getConfig(String config, EntityType entityType, List<String> permissionList) {
        if (permissionList != null && !permissionList.isEmpty()) {
            for (String permission : permissionList) {
                String section = "settings.permission." + permission;

                if (getConfig().isConfigurationSection(section)) {
                    ConfigurationSection configurationSection = getConfig().getConfigurationSection(section);

                    if (configurationSection != null && (versionManager.hasConfigContains()
                            ? configurationSection.contains(config, true)
                            : configurationSection.contains(config))) {
                        return configurationSection;
                    }
                }
            }
        }

        if (entityType != null) {
            String section = "settings.entity." + entityType.name();

            if (getConfig().isConfigurationSection(section)) {
                ConfigurationSection configurationSection = getConfig().getConfigurationSection(section);

                if (configurationSection != null && (versionManager.hasConfigContains()
                        ? configurationSection.contains(config, true)
                        : configurationSection.contains(config))) {
                    return configurationSection;
                }
            }
        }

        return getConfig().getConfigurationSection("settings.default.default");
    }

    private void loadResourceDefaults(FileConfiguration fileConfiguration, String resource) {
        InputStream inputStream = getResource(resource);

        if (inputStream != null) {
            fileConfiguration.addDefaults(YamlConfiguration
                    .loadConfiguration(new InputStreamReader(inputStream, UTF_8)));
        }
    }

    private void bakeDefaults(FileConfiguration fileConfiguration) {
        try {
            fileConfiguration.options().copyDefaults(true);
            fileConfiguration.loadFromString(fileConfiguration.saveToString());
        } catch (InvalidConfigurationException ignored) {
        }
    }

    public List<String> getPermissionList(Entity entity) {
        List<String> permissionList = new ArrayList<>();
        List<String> permissionListSorted = new ArrayList<>();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().startsWith("graves.permission.")) {
                    String permission = permissionAttachmentInfo.getPermission()
                            .replace("graves.permission.", "").toLowerCase();

                    if (getConfig().isConfigurationSection("settings.permission." + permission)) {
                        permissionList.add(permission);
                    }
                }
            }

            ConfigurationSection configurationSection = getConfig().getConfigurationSection("settings.permission");

            if (configurationSection != null) {
                for (String permission : configurationSection.getKeys(false)) {
                    if (permissionList.contains(permission)) {
                        permissionListSorted.add(permission);
                    }
                }
            }
        }

        return permissionListSorted;
    }

    private FileConfiguration getConfigFiles(File folder) {
        FileConfiguration fileConfiguration = new YamlConfiguration();
        File[] files = folder.listFiles();

        if (files != null) {
            Arrays.sort(files);

            List<File> fileList = new LinkedList<>(Arrays.asList(files));
            File mainConfig = new File(getConfigFolder(), "config.yml");

            if (fileList.contains(mainConfig)) {
                fileList.remove(mainConfig);
                fileList.add(0, mainConfig);
            }

            for (File file : fileList) {
                if (YAMLUtil.isValidYAML(file)) {
                    if (file.isDirectory()) {
                        fileConfiguration.addDefaults(getConfigFiles(file));
                    } else {
                        FileConfiguration savedFileConfiguration = getConfigFile(file);

                        if (savedFileConfiguration != null) {
                            fileConfiguration.addDefaults(savedFileConfiguration);
                            bakeDefaults(fileConfiguration);
                            loadResourceDefaults(fileConfiguration, "config" + File.separator + file.getName());
                        } else {
                            warningMessage("Unable to load config " + file.getName());
                        }
                    }
                }
            }
        }

        return fileConfiguration;
    }

    private FileConfiguration getConfigFile(File file) {
        FileConfiguration fileConfiguration = null;

        if (YAMLUtil.isValidYAML(file)) {
            try {
                fileConfiguration = YamlConfiguration.loadConfiguration(file);
            } catch (IllegalArgumentException exception) {
                logStackTrace(exception);
            }
        }

        return fileConfiguration;
    }

    public final File getConfigFolder() {
        return new File(getDataFolder(), "config");
    }

    public final File getPluginsFolder() {
        return getDataFolder().getParentFile();
    }

    public String getVersion() {
        return getDescription().getVersion();
    }

    public String getLatestVersion() {
        return UpdateUtil.getLatestVersion(getSpigotID());
    }

    public final int getSpigotID() {
        return 118271;
    }

    public final int getMetricsID() {
        return 23069; // https://bstats.org/plugin/bukkit/GravesX/23069
    }

    public final int getMetricsIDLegacy() {
        return 12849; // https://bstats.org/plugin/bukkit/Graves/12849
    }

    public void logStackTrace(Exception e) {
        for (StackTraceElement element : e.getStackTrace()) {
            getLogger().severe(element.toString());
        }
    }

    public void logInvalidGraveSite(String grave_uuid, Location affectedGraveLocation, List<String> invalidationReason) {
        String graveWorld;
        try {
            graveWorld = affectedGraveLocation.getWorld().getName();
        } catch (Exception e) {
            graveWorld = "Unknown";
        }
        String graveX = String.valueOf(affectedGraveLocation.getBlockX());
        String graveY = String.valueOf(affectedGraveLocation.getBlockY());
        String graveZ = String.valueOf(affectedGraveLocation.getBlockZ());
        getLogger().warning("Grave "
                + grave_uuid
                + " at location World: "
                + graveWorld
                + ", x: "
                + graveX
                + ", y: "
                + graveY
                + ", z: "
                + graveZ
                + " is invalid. If this was caused by an addon, then report it to the plugin author. Reason: "
                + String.join(", ", invalidationReason));
    }

    /**
     * Checks if the specified player has been granted the specified permission.
     * This method first checks if the Vault integration is available and uses it to check permissions.
     * If Vault is not available, it falls back to the default Bukkit permission check.
     * Additionally, this method logs debug messages based on the permission check results.
     *
     * @param permission the permission to check for
     * @param player the player whose permissions are being checked
     * @return {@code true} if the player has the specified permission, {@code false} otherwise
     */
    public boolean hasGrantedPermission(String permission, Player player) {
        boolean hasPermission;

        if (getIntegrationManager().hasLuckPermsHandler()) {
            hasPermission = getIntegrationManager().getLuckPermsHandler().hasPermission(player, permission);
            debugMessage("[LuckPerms] Player: " + player.getName() + " | Permission: " + permission + " | Has Permission: " + hasPermission, 4);
        } else if (getIntegrationManager().hasVault()) {
            hasPermission = getIntegrationManager().getVault().hasPermission(player, permission);
            debugMessage("[Vault] Player: " + player.getName() + " | Permission: " + permission + " | Has Permission: " + hasPermission, 4);
        } else {
            hasPermission = player.hasPermission(permission);
            debugMessage("[Bukkit] Player: " + player.getName() + " | Permission: " + permission + " | Has Permission: " + hasPermission, 4);
        }

        return hasPermission;
    }

    /**
     * Checks if the specified offline player has been granted the specified permission.
     * This method first checks if the Vault integration is available and uses it to check permissions.
     * If Vault is not available, it falls back to the default Bukkit permission check.
     * Additionally, this method logs debug messages based on the permission check results.
     *
     * @param permission the permission to check for
     * @param offlinePlayer the offline player whose permissions are being checked
     * @return {@code true} if the offline player has the specified permission, {@code false} otherwise
     * @deprecated This method is deprecated because it is less efficient to check permissions for offline players.
     *             Use {@link #hasGrantedPermission(String, Player)} for online players instead.
     */
    @Deprecated
    public boolean hasGrantedPermission(String permission, OfflinePlayer offlinePlayer) {
        boolean hasPermission = false;

        if (getIntegrationManager().hasLuckPermsHandler()) {
            hasPermission = getIntegrationManager().getLuckPermsHandler().hasPermission(offlinePlayer, permission);
            debugMessage("[LuckPerms] Player: " + offlinePlayer.getName() + " | Permission: " + permission + " | Has Permission: " + hasPermission, 4);
        } else if (getIntegrationManager().hasVaultPermProvider()) {
            hasPermission = getIntegrationManager().getVault().hasPermission(offlinePlayer, permission);
            debugMessage("[Vault] Offline Player: " + offlinePlayer.getName() + " | Permission: " + permission + " | Has Permission: " + hasPermission, 4);
        } else if (offlinePlayer.isOnline()) {
            if (offlinePlayer.getPlayer() != null) {
                hasPermission = offlinePlayer.getPlayer().hasPermission(permission);
                debugMessage("[Bukkit] Offline Player: " + offlinePlayer.getName() + " | Permission: " + permission + " | Has Permission: " + hasPermission, 4);
            } else {
                debugMessage("[Bukkit] Failed to get offline player. Assuming player doesn't have permission.", 4);
                debugMessage("[Bukkit] Offline Player: " + offlinePlayer.getName() + " | Permission: " + permission + " | Has Permission: " + hasPermission, 4);
            }
        }

        return hasPermission;
    }
}
