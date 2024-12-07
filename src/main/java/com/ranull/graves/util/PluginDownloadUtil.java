package com.ranull.graves.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PluginDownloadUtil {

    /**
     * Downloads a plugin from Spiget and saves it to the plugins folder, replacing it if it exists.
     *
     * @param pluginId      The Spigot resource ID of the plugin.
     * @param pluginName    The name of the plugin file (without the ".jar" extension).
     * @param pluginsFolder The path to the plugins' folder.
     * @throws IOException If the download or file operations fail.
     */
    public static void downloadAndReplacePlugin(long pluginId, String pluginName, String pluginsFolder, CommandSender commandSender) throws IOException {
        downloadAndReplacePlugin(String.valueOf(pluginId), pluginName, pluginsFolder, commandSender);
    }

    /**
     * Downloads a plugin from Spiget and saves it to the plugins folder, replacing it if it exists.
     *
     * @param pluginId      The Spigot resource ID of the plugin.
     * @param pluginName    The name of the plugin file (without the ".jar" extension).
     * @param pluginsFolder The path to the plugins' folder.
     * @throws IOException If the download or file operations fail.
     */
    public static void downloadAndReplacePlugin(String pluginId, String pluginName, String pluginsFolder, CommandSender commandSender) throws IOException {
        String downloadUrl = "https://api.spiget.org/v2/resources/" + pluginId + "/download";
        String pluginJarPath = pluginsFolder + File.separator + pluginName + ".jar";

        // Ensure plugins folder exists
        Files.createDirectories(Paths.get(pluginsFolder));

        // Download the plugin
        sendMessageToExecutor(commandSender, ChatColor.GREEN + "Downloading plugin: " + pluginName + "...");
        File tempFile = downloadFile(downloadUrl, commandSender);

        if (tempFile == null) return;

        // Replace the existing plugin or move the new one
        File pluginFile = new File(pluginJarPath);
        if (pluginFile.exists()) {
            sendMessageToExecutor(commandSender, ChatColor.GREEN + "Replacing existing plugin: " + pluginFile.getName());
            Files.delete(pluginFile.toPath());
        } else {
            sendMessageToExecutor(commandSender, ChatColor.GREEN + "Adding new plugin: " + pluginFile.getName());
        }
        Files.move(tempFile.toPath(), pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        sendMessageToExecutor(commandSender, ChatColor.GREEN + "Plugin " + pluginName + " successfully updated in \"" + pluginsFolder + "\". Restart the server in order to take effect.");
    }

    /**
     * Downloads a file from the given URL to a temporary file.
     *
     * @param fileUrl The URL to download the file from.
     * @return A temporary file containing the downloaded data.
     * @throws IOException If the download fails.
     */
    private static File downloadFile(String fileUrl, CommandSender commandSender) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            sendMessageToExecutor(commandSender, ChatColor.RED + "Failed to download file. HTTP Response Code: " + responseCode);
            return null;
        }

        File tempFile = File.createTempFile("plugin", ".jar");
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        connection.disconnect();
        return tempFile;
    }

    private static void sendMessageToExecutor(CommandSender commandSender, String message) {
        commandSender.sendMessage(message);
    }
}
