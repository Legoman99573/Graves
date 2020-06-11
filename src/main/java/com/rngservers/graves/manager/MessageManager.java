package com.rngservers.graves.manager;

import com.rngservers.graves.Graves;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class MessageManager {
    private Graves plugin;

    public MessageManager(Graves plugin) {
        this.plugin = plugin;
    }

    public void graveLoot(Location location, Player player) {
        String lootMessage = Objects.requireNonNull(plugin.getConfig().getString("settings.lootMessage"))
                .replace("&", "§");
        if (!lootMessage.equals("")) {
            player.sendMessage(lootMessage);
        }

        String lootSound = Objects.requireNonNull(plugin.getConfig().getString("settings.lootSound"));
        if (!lootSound.equals("")) {
            Objects.requireNonNull(location.getWorld()).playSound(location, Sound.valueOf(lootSound.toUpperCase()), 1, 1);
        }

        String lootEffect = Objects.requireNonNull(plugin.getConfig().getString("settings.lootEffect"));
        if (!lootEffect.equals("")) {
            Objects.requireNonNull(location.getWorld()).playEffect(location, Effect.valueOf(lootEffect), 0);
        }
    }

    public void graveOpen(Location location) {
        String openSound = Objects.requireNonNull(plugin.getConfig().getString("settings.openSound"));
        if (!openSound.equals("")) {
            Objects.requireNonNull(location.getWorld()).playSound(location,
                    Sound.valueOf(openSound.toUpperCase()), 1, 1);
        }
    }

    public void graveClose(Location location) {
        String closeSound = Objects.requireNonNull(plugin.getConfig().getString("settings.closeSound"));
        if (!closeSound.equals("")) {
            Objects.requireNonNull(location.getWorld()).playSound(location,
                    Sound.valueOf(closeSound.toUpperCase()), 1, 1);
        }
    }

    public void maxGraves(Player player) {
        String maxGravesMessage = Objects.requireNonNull(plugin.getConfig()
                .getString("settings.maxGravesMessage"))
                .replace("&", "§");
        if (!maxGravesMessage.equals("")) {
            player.sendMessage(maxGravesMessage);
        }
    }

    public void buildDenied(Player player) {
        String buildDeniedMessage = Objects.requireNonNull(plugin.getConfig()
                .getString("settings.buildDeniedMessage"))
                .replace("&", "§");
        if (!buildDeniedMessage.equals("")) {
            player.sendMessage(buildDeniedMessage);
        }
    }

    public void graveProtect(Player player, Location location) {
        String protectMessage = Objects.requireNonNull(plugin.getConfig().getString("settings.protectMessage"))
                .replace("&", "§");
        if (!protectMessage.equals("")) {
            player.sendMessage(protectMessage);
        }

        String protectSound = Objects.requireNonNull(plugin.getConfig().getString("settings.protectSound"));
        if (!protectSound.equals("")) {
            Objects.requireNonNull(location.getWorld())
                    .playSound(location, Sound.valueOf(protectSound.toUpperCase()), 1, 1);
        }
    }

    public void tokenNoTokenMessage(Player player) {
        String graveTokenName = Objects.requireNonNull(plugin.getConfig().getString("settings.tokenName"))
                .replace("&", "§");
        String graveTokenNoTokenMessage = Objects.requireNonNull(plugin.getConfig()
                .getString("settings.tokenNoTokenMessage"))
                .replace("$name", graveTokenName).replace("&", "§");
        if (!graveTokenNoTokenMessage.equals("")) {
            player.sendMessage(graveTokenNoTokenMessage);
        }
    }

    public void graveChangeProtect(Location location) {
        String protectChangeSound = Objects.requireNonNull(plugin.getConfig().getString("settings.protectChangeSound"));
        if (!protectChangeSound.equals("")) {
            Objects.requireNonNull(location.getWorld()).playSound(location,
                    Sound.valueOf(protectChangeSound.toUpperCase()), 1.0F, 1.0F);
        }
    }

    public void permissionDenied(CommandSender sender) {
        String permissionDeniedMessage = Objects.requireNonNull(plugin.getConfig().getString("settings.permissionDeniedMessage"))
                .replace("&", "§");
        if (!permissionDeniedMessage.equals("")) {
            sender.sendMessage(permissionDeniedMessage);
        }
    }
}
