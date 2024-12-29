package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.event.GraveProjectileHitEvent;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ProjectileHitListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs a new ProjectileHitListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public ProjectileHitListener(Graves plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHitGrave(ProjectileHitEvent event) {
        Block block = event.getHitBlock();

        Location location = block != null ? block.getLocation() : null;
        if (block == null) return;
        Grave grave = plugin.getBlockManager().getGraveFromBlock(block);

        if (grave != null) {
            if (!plugin.getConfig("drop.projectile.enabled", grave).getBoolean("drop.projectile.enabled")) return;

            if (event.getEntity().getShooter() instanceof Player) {
                if (!plugin.getConfig("drop.projectile.player", grave).getBoolean("drop.projectile.player")) return;
                Player player = ((Player) event.getEntity().getShooter()).getPlayer();

                GraveProjectileHitEvent graveProjectileHitEvent = new GraveProjectileHitEvent(location, player, grave, event.getEntity(), block);

                plugin.getServer().getPluginManager().callEvent(graveProjectileHitEvent);

                if (!graveProjectileHitEvent.isCancelled() && !graveProjectileHitEvent.isAddon()) {
                    plugin.getGraveManager().breakGrave(location, grave);
                    plugin.getGraveManager().closeGrave(grave);
                    plugin.getGraveManager().playEffect("effect.loot", location, grave);
                    if (plugin.getIntegrationManager().hasNoteBlockAPI()) {
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
                        }
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                        }
                    }
                    String playerName = player != null ? player.getDisplayName() : "Unknown";
                    plugin.debugMessage("Grave destroyed by projectile " + event.getEntity().getName().toLowerCase() + " caused by player " + playerName + ".", 1);
                }
            } else if (event.getEntity().getShooter() instanceof LivingEntity) {
                if (!plugin.getConfig("drop.projectile.living-entity", grave).getBoolean("drop.projectile.living-entity")) return;

                LivingEntity livingEntity = ((LivingEntity) event.getEntity().getShooter());
                GraveProjectileHitEvent graveProjectileHitEvent = new GraveProjectileHitEvent(location, livingEntity, grave, event.getEntity(), block);

                plugin.getServer().getPluginManager().callEvent(graveProjectileHitEvent);

                if (!graveProjectileHitEvent.isCancelled() && !graveProjectileHitEvent.isAddon()) {
                    plugin.getGraveManager().breakGrave(location, grave);
                    plugin.getGraveManager().closeGrave(grave);
                    plugin.getGraveManager().playEffect("effect.loot", location, grave);
                    if (plugin.getIntegrationManager().hasNoteBlockAPI()) {
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(grave.getOwnerUUID())) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(grave.getOwnerUUID());
                        }
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                        }
                    }
                    plugin.debugMessage("Grave destroyed by projectile " + event.getEntity().getName().toLowerCase() + "  caused by living entity " + livingEntity.getType().name().toLowerCase() + ".", 1);
                }
            } else {
                if (!plugin.getConfig("drop.projectile.other", grave).getBoolean("drop.projectile.other")) return;

                GraveProjectileHitEvent graveProjectileHitEvent = new GraveProjectileHitEvent(location, grave, event.getEntity(), block);

                plugin.getServer().getPluginManager().callEvent(graveProjectileHitEvent);

                if (!graveProjectileHitEvent.isCancelled() && !graveProjectileHitEvent.isAddon()) {
                    plugin.getGraveManager().breakGrave(location, grave);
                    plugin.getGraveManager().closeGrave(grave);
                    plugin.getGraveManager().playEffect("effect.loot", location, grave);
                    if (plugin.getIntegrationManager().hasNoteBlockAPI()) {
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(grave.getOwnerUUID())) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(grave.getOwnerUUID());
                        }
                        if (plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                            plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                        }
                    }
                    plugin.debugMessage("Grave destroyed by projectile " + event.getEntity().getName().toLowerCase() + "  caused by unknown player or living entity.", 1);
                }
            }
        }
    }
}