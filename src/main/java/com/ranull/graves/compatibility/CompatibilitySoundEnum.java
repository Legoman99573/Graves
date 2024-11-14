package com.ranull.graves.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.lang.reflect.Method;

/**
 * Handles Compatibility for Sound Enums to prevent runtime errors on versions older than 1.21.2.
 */
public class CompatibilitySoundEnum {

    /**
     * Retrieves the {@link Sound} value associated with the given sound name.
     *
     * <p>For Minecraft 1.21.2 and newer, this method returns a constant from the {@link Sound} class directly.
     * For versions older than 1.21.2, it uses reflection to access the corresponding enum value.</p>
     *
     * <p>If the sound name does not exist in the sound constants or enum, or if an exception occurs during retrieval,
     * the method logs the error and returns {@code null}.</p>
     *
     * @param soundName The name of the sound to retrieve. This should be the name of the sound as a string
     *                  (e.g., "BLOCK_ANVIL_LAND").
     * @return The corresponding {@link Sound} value, or {@code null} if the sound name is invalid or an error occurs.
     */
    public static Sound valueOf(String soundName) {
        try {
            Method method = Sound.class.getMethod("valueOf", String.class);
            return (Sound) method.invoke(null, soundName);
        } catch (NoSuchMethodException e) {
            Bukkit.getServer().getLogger().severe(soundName + " does not exist in sound enum.");
            e.printStackTrace();
        } catch (Exception e) {
            Bukkit.getServer().getLogger().severe("An issue occurred while retrieving sound enum " + soundName + ". Cause: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}