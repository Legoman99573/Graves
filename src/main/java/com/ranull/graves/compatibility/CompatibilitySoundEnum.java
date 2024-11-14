package com.ranull.graves.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.lang.reflect.Method;

public class CompatibilitySoundEnum {
    public static Sound getSound(String soundName) {
        try {
            Method method = Sound.class.getMethod("valueOf", String.class);
            return (Sound) method.invoke(null, soundName);
        } catch (NoSuchMethodException e) {
            Bukkit.getServer().getLogger().severe(soundName + " does not exist in sound enum.");
            e.printStackTrace();
        } catch (Exception e) {
            Bukkit.getServer().getLogger().severe("An issue occurred while playing sound enum " + soundName + ". Cause: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
