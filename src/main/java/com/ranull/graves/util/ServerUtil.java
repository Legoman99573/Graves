package com.ranull.graves.util;

import com.ranull.graves.Graves;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import oshi.SystemInfo;
import oshi.hardware.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility class for gathering server information and generating server dumps.
 * This class includes methods for retrieving various system and server-related information.
 */
public final class ServerUtil {

    /**
     * Gathers server information and generates a dump in string format.
     *
     * @param plugin The Graves plugin instance.
     * @return A string containing the server dump information.
     */
    public static String getServerDumpInfo(Graves plugin) {
        List<String> stringList = new ArrayList<>();
        // Add basic server information
        stringList.add("=================");
        stringList.add("Java Information:");
        stringList.add("=================");
        stringList.add("Java Version: " + getSystemProperty("java.version"));
        stringList.add("Java Vendor: " + getSystemProperty("java.vendor"));
        stringList.add("Java Vendor URL: " + getSystemProperty("java.vendor.url"));
        stringList.add("Java Home: " + getSystemProperty("java.home"));
        stringList.add("Java VM Specification Version: " + getSystemProperty("java.vm.specification.version"));
        stringList.add("Java VM Specification Vendor: " + getSystemProperty("java.vm.specification.vendor"));
        stringList.add("Java VM Specification Name: " + getSystemProperty("java.vm.specification.name"));
        stringList.add("Java VM Version: " + getSystemProperty("java.vm.version"));
        stringList.add("Java VM Vendor: " + getSystemProperty("java.vm.vendor"));
        stringList.add("Java VM Name: " + getSystemProperty("java.vm.name"));
        stringList.add("");

        stringList.add("=============================");
        stringList.add("Operating System Information:");
        stringList.add("=============================");
        stringList.add("OS Name: " + getOsName());
        stringList.add("OS Version: " + getSystemProperty("os.version"));
        stringList.add("OS Architecture: " + getSystemProperty("os.arch"));
        stringList.add("User Name: " + getSystemProperty("user.name"));
        stringList.add("User Home: " + getSystemProperty("user.home"));
        stringList.add("User Directory: " + getSystemProperty("user.dir"));
        stringList.add("Docker Container: " + isRunningInDocker());
        if (isRunningInDocker()) {
            stringList.add("Rootless Container: " + isRootlessDocker());
            stringList.add("Running with Panel: " + isRunningWithPanel());
        }

        // Check for top-level access
        if (isRunningAsRoot()) {
            stringList.add("WARNING: This " + plugin.getServer().getName() + " server is running with top-level access (root/administrator)");
            plugin.getLogger().warning("This server is running with top-level access (root/administrator), which is unsafe and can lead to security vulnerabilities. We recommend creating a user account or running the server in a rootless Docker container.");
        }
        stringList.add("");

        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        String cpuName = processor.getProcessorIdentifier().getName();
        String vendorName = processor.getProcessorIdentifier().getVendor();
        CentralProcessor.ProcessorIdentifier identifier = processor.getProcessorIdentifier();

        stringList.add("================");
        stringList.add("CPU Information:");
        stringList.add("================");
        stringList.add("CPU: " + (cpuName != null ? cpuName : "Not available"));
        stringList.add("Vendor: " + (vendorName != null ? vendorName : "Not available"));
        stringList.add("CPU Identifier: " + (identifier.getIdentifier() != null ? identifier.getIdentifier() : "Not available"));
        // Current frequency for each core
        long[] currentFreqs = processor.getCurrentFreq();
        if (currentFreqs != null && currentFreqs.length > 0) {
            stringList.add("CPU Current Frequencies:");
            for (int i = 0; i < currentFreqs.length; i++) {
                stringList.add(" - Core " + i + ": " + formatFrequency(currentFreqs[i]));
            }
        } else {
            stringList.add("CPU Current Frequencies: Not available");
        }
        double maxFreq = processor.getMaxFreq();
        stringList.add("CPU Maximum Frequency: " + formatFrequency(maxFreq));
        stringList.add("CPU Architecture: " + identifier.getMicroarchitecture());
        stringList.add("CPU Stepping: " + identifier.getStepping());

        List<CentralProcessor.ProcessorCache> cacheSizes = processor.getProcessorCaches();
        if (cacheSizes != null && !cacheSizes.isEmpty()) {
            stringList.add("CPU Cache Sizes: ");
            for (CentralProcessor.ProcessorCache cache : cacheSizes) {
                stringList.add(" - " + cache.getCacheSize() + " bytes, Level: " + cache.getLevel());
            }
        } else {
            stringList.add("CPU Cache Sizes: Not available");
        }
        stringList.add("");


        HardwareAbstractionLayer hal = systemInfo.getHardware();
        ComputerSystem computerSystem = hal.getComputerSystem();
        List<PhysicalMemory> ramList = hal.getMemory().getPhysicalMemory();
        stringList.add("=======================");
        stringList.add("System RAM Information:");
        stringList.add("=======================");
        stringList.add("Number of RAM Sticks: " + ramList.size());

        for (PhysicalMemory ram : ramList) {
            String vendor = ram.getManufacturer();
            stringList.add("=======");
            stringList.add(ramList + ":");
            stringList.add("=======");
            stringList.add("Vendor: " + (vendor != null ? vendor : "Unknown"));
            stringList.add("Memory Size: " + formatBytes(ram.getCapacity()));
            stringList.add("Speed: " + ram.getClockSpeed() + " MHz");
            stringList.add("Memory Type: " + ram.getMemoryType());
        }

        GlobalMemory memory = hal.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;

        stringList.add("Total Memory: " + formatBytes(totalMemory));
        stringList.add("Free Memory: " + formatBytes(availableMemory));
        stringList.add("Used Memory: " + formatBytes(usedMemory));
        stringList.add("");

        stringList.add("==============================");
        stringList.add("System Disk Space Information:");
        stringList.add("==============================");
        for (HWDiskStore disk : hal.getDiskStores()) {
            stringList.add("Disk: " + disk.getName());
            stringList.add("Model: " + (disk.getModel() != null ? disk.getModel() : "Not Available"));
            stringList.add("Total Space: " + formatBytes(disk.getSize()));
            stringList.add("Total Space: " + formatBytes(disk.getSize()));
            stringList.add("Free Space: " + formatBytes(disk.getSize() - disk.getWriteBytes()));
            stringList.add("Read Rate: " + formatBytes(disk.getReadBytes()));
            stringList.add("Write Rate: " + formatBytes(disk.getWriteBytes()));
            stringList.add("");
        }
        stringList.add("");

        stringList.add("=============================");
        stringList.add("Minecraft Server Information:");
        stringList.add("=============================");
        stringList.add("Implementation Name: " + plugin.getServer().getName());
        stringList.add("Implementation Version: " + plugin.getServer().getVersion());
        stringList.add("Bukkit Version: " + plugin.getServer().getBukkitVersion());
        try {
            stringList.add("NMS Version: " + getNmsVersion(plugin.getServer()));
        } catch (Exception e) {
            stringList.add("NMS Version: " + Bukkit.getServer().getVersion());
        }
        stringList.add("Player Count: " + plugin.getServer().getOnlinePlayers().size());
        stringList.add("Player List: " + getPlayerList());
        stringList.add("Plugin Count: " + plugin.getServer().getPluginManager().getPlugins().length);
        stringList.add("Plugin List: " + getPluginList());
        stringList.add("Server used /reload: " + plugin.wasReloaded());
        stringList.add("");

        stringList.add("===================");
        stringList.add("Graves Information:");
        stringList.add("===================");
        // Add plugin-specific information
        stringList.add(plugin.getDescription().getName() + " Version: " + plugin.getDescription().getVersion());

        if (plugin.getVersionManager().hasAPIVersion()) {
            stringList.add(plugin.getDescription().getName() + " API Version: " + plugin.getDescription().getAPIVersion());
        }

        List<String> depends = getHardDependencies();
        List<String> softDepends = getSoftDependencies();

        if (depends != null) {
            stringList.add(plugin.getDescription().getName() + " Hard Dependencies: " + String.join(", ", depends));
        } else {
            stringList.add(plugin.getDescription().getName() + " Hard Dependencies: None");
        }

        if (softDepends != null) {
            stringList.add(plugin.getDescription().getName() + " Soft Dependencies: " + String.join(", ", softDepends));
        } else {
            stringList.add(plugin.getDescription().getName() + " Soft Dependencies: None");
        }

        String databaseTypefromConfig = plugin.getConfig().getString("settings.storage.type", "SQLITE").toUpperCase();
        stringList.add(plugin.getDescription().getName() + " Database Type: " + databaseTypefromConfig);
        if (databaseTypefromConfig.equals("MYSQL") || databaseTypefromConfig.equals("MARIADB") || databaseTypefromConfig.equals("POSTGRESQL")) {
            try {
                stringList.add(plugin.getDescription().getName() + " Database Version: " + plugin.getDataManager().getDatabaseVersion());
            } catch (Exception e) {
                stringList.add(plugin.getDescription().getName() + " Database Version: Unknown");
            }
        }
        if (plugin.getIntegrationManager().hasLuckPermsHandler()) {
            stringList.add(plugin.getDescription().getName() + " Permissions Provider: LuckPerms");
        } else if (plugin.getIntegrationManager().hasVaultPermProvider()) {
            stringList.add(plugin.getDescription().getName() + " Permissions Provider: Vault");
        } else {
            stringList.add(plugin.getDescription().getName() + " Permissions Provider: Bukkit");
        }
        stringList.add(plugin.getDescription().getName() + " Plugin Release: " + plugin.getPluginReleaseType());
        stringList.add(plugin.getDescription().getName() + " Config Version: " + plugin.getConfig().getInt("config-version"));

        File configDir = new File("plugins/GravesX/config");
        if (configDir.exists() && configDir.isDirectory()) {
            File[] files = configDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".yml")) {
                        try {
                            String configContent = readFileToString(file);
                            String maskedConfigContent = maskPasswords(configContent, plugin.getConfig());
                            String configBase64 = Base64.getEncoder().encodeToString(maskedConfigContent.getBytes());
                            stringList.add("Config " + file.getName() + " Base64: " + configBase64);
                        } catch (IOException e) {
                            stringList.add("Config " + file.getName() + " could not be read.");
                        }
                    }
                }
            }
        }

        // Join all information into a single string separated by new lines
        return joinLines(stringList);
    }

    private static String formatFrequency(double frequency) {
        if (frequency >= 1_000_000_000_000.0) {
            return String.format("%.2f THz", frequency / 1_000_000_000_000.0);
        } else if (frequency >= 1_000_000_000) {
            return String.format("%.2f GHz", frequency / 1_000_000_000);
        } else if (frequency >= 1_000_000) {
            return String.format("%.2f MHz", frequency / 1_000_000);
        } else if (frequency >= 1_000) {
            return String.format("%.2f kHz", frequency / 1_000);
        } else {
            return frequency + " Hz";
        }
    }

    /**
     * Reads a file into a string.
     *
     * @param file The file to read.
     * @return The content of the file as a string.
     * @throws IOException If an I/O error occurs.
     */
    private static String readFileToString(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data);
        }
    }


    /**
     * Retrieves the NMS version using reflection to ensure compatibility.
     *
     * @param server The Bukkit server instance.
     * @return The NMS version string.
     * @throws Exception if the method to retrieve NMS version is not found.
     */
    private static String getNmsVersion(Object server) throws Exception {
        Class<?> serverClass = server.getClass();
        Method method = serverClass.getMethod("getVersion"); // Replace with actual method name if different
        return (String) method.invoke(server);
    }

    /**
     * Formats a byte count into a human-readable string with appropriate units.
     *
     * @param bytes The number of bytes.
     * @return A string with the byte count formatted in B, KB, MB, GB, TB, or PB.
     */
    private static String formatBytes(long bytes) {
        boolean isNegative = bytes < 0; // Check if the value is negative
        long absoluteBytes = Math.abs(bytes); // Use absolute value for formatting

        StringBuilder result = new StringBuilder();
        if (absoluteBytes < 1024) {
            result.append(absoluteBytes).append(" B");
        } else {
            int exp = (int) (Math.log(absoluteBytes) / Math.log(1024));
            String pre = ("KMGTPE").charAt(exp - 1) + "";
            result.append(String.format("%.1f %sB", absoluteBytes / Math.pow(1024, exp), pre));
        }

        if (isNegative) {
            result.insert(0, "-"); // Add the minus sign for negative values
        }

        return result.toString();
    }

    /**
     * Gets the value of a system property or returns "Unknown" if the property is not set.
     *
     * @param key The name of the system property.
     * @return The value of the system property or "Unknown" if not set.
     */
    private static String getSystemProperty(String key) {
        String value = System.getProperty(key);
        return value != null ? value : "Unknown";
    }

    /**
     * Gets the detailed OS name from the system properties or files.
     *
     * @return A string with the OS name.
     */
    private static String getOsName() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("linux")) {
            return getLinuxOsName();
        }
        return osName;
    }

    /**
     * Reads the OS name from the /etc/os-release file on Unix-like systems.
     *
     * @return A string with the OS name.
     */
    private static String getLinuxOsName() {
        String osReleaseFile = "/etc/os-release";
        try (BufferedReader reader = new BufferedReader(new FileReader(osReleaseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PRETTY_NAME=")) {
                    return line.substring("PRETTY_NAME=".length()).replace("\"", "");
                }
            }
        } catch (IOException e) {
            return "Linux (Unknown)";
        }
        return "Linux (Unknown)";
    }

    /**
     * Checks if the server is running in a Docker container.
     *
     * @return True if running in Docker, otherwise false.
     */
    private static boolean isRunningInDocker() {
        File cgroupFile = new File("/proc/self/cgroup");
        try (BufferedReader reader = new BufferedReader(new FileReader(cgroupFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("/docker/")) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Ignored
        }
        return false;
    }

    /**
     * Checks if the server is running in a rootless Docker container.
     *
     * @return True if running in a rootless Docker container, otherwise false.
     */
    private static boolean isRootlessDocker() {
        File uidFile = new File("/proc/self/uid_map");
        return uidFile.exists();
    }

    /**
     * Checks if the server is running with a panel (e.g., hosting panel).
     *
     * @return True if running with a panel, otherwise false.
     */
    private static boolean isRunningWithPanel() {
        // Example: check for known panel files or environment variables
        return new File("/.panel").exists();
    }

    /**
     * Checks if the server is running with root-level access.
     *
     * @return True if running as root, otherwise false.
     */
    private static boolean isRunningAsRoot() {
        return System.getProperty("user.name").equals("root");
    }

    /**
     * Masks passwords in the configuration string with asterisks while retaining the character count.
     *
     * @param configString The configuration string.
     * @param config       The FileConfiguration object.
     * @return The modified configuration string with passwords masked.
     */
    private static String maskPasswords(String configString, FileConfiguration config) {
        // Replace passwords with '*' characters while retaining original character count
        String maskedConfigString = configString;
        Set<String> keys = new HashSet<>(config.getKeys(true));
        for (String path : keys) {
            Object value = config.get(path);
            if (value instanceof String && isPasswordField(path)) {
                String password = (String) value;
                maskedConfigString = maskedConfigString.replace(password, repeat('*', password.length()));
            }
        }
        return maskedConfigString;
    }

    /**
     * Determines if the given configuration path corresponds to a password field.
     *
     * @param path The configuration path.
     * @return True if the path is a password field, otherwise false.
     */
    private static boolean isPasswordField(String path) {
        return path.toLowerCase().contains("password") || path.toLowerCase().contains("secret");
    }

    /**
     * Joins a list of strings into a single string, separated by new lines.
     *
     * @param lines The list of lines to join.
     * @return A single string with lines joined by new lines.
     */
    private static String joinLines(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Repeats a character a specified number of times.
     *
     * @param ch   The character to repeat.
     * @param times The number of times to repeat the character.
     * @return A string with the character repeated.
     */
    private static String repeat(char ch, int times) {
        char[] chars = new char[times];
        Arrays.fill(chars, ch);
        return new String(chars);
    }

    /**
     * Gets a list of online players' names.
     *
     * @return A comma-separated string of online player names.
     */
    private static String getPlayerList() {
        StringBuilder sb = new StringBuilder();
        for (Player player : Bukkit.getOnlinePlayers()) {
            sb.append(player.getName()).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
    }

    /**
     * Gets a list of plugins with their names and versions.
     *
     * @return A comma-separated string of plugin names and versions.
     */
    private static String getPluginList() {
        StringBuilder sb = new StringBuilder();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            sb.append(plugin.getName()).append(" ").append(plugin.getDescription().getVersion()).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
    }

    private static List<String> getHardDependencies() {
        List<String> depends = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getDepend().contains("GravesX")) {
                depends.add(plugin.getName() + " v." + plugin.getDescription().getVersion());
            }
        }
        return depends.isEmpty() ? null : depends;
    }

    private static List<String> getSoftDependencies() {
        List<String> softDepends = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getSoftDepend().contains("GravesX")) {
                softDepends.add(plugin.getName() + " v." + plugin.getDescription().getVersion());
            }
        }
        return softDepends.isEmpty() ? null : softDepends;
    }
}