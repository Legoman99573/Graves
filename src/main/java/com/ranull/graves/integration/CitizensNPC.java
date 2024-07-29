package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.listener.integration.citizensnpcs.CitizensNPCInteractListener;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Manages NPC interactions and corpse creation related to player graves using Citizens2.
 * Extends EntityDataManager to handle entity data.
 */
public final class CitizensNPC extends EntityDataManager {
    private final Graves plugin;
    private final CitizensNPCInteractListener citizensNPCInteractListener;

    /**
     * Constructs a new CitizensNPC instance with the specified Graves plugin.
     *
     * @param plugin The main Graves plugin instance.
     */
    public CitizensNPC(Graves plugin) {
        super(plugin);

        this.plugin = plugin;
        this.citizensNPCInteractListener = new CitizensNPCInteractListener(plugin, this);

        registerListeners();
    }

    /**
     * Registers the NPC interaction listeners.
     */
    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(citizensNPCInteractListener, plugin);
    }

    /**
     * Unregisters the NPC interaction listeners.
     */
    public void unregisterListeners() {
        if (citizensNPCInteractListener != null) {
            HandlerList.unregisterAll(citizensNPCInteractListener);
        }
    }

    /**
     * Creates NPC corpses based on the cached entity data.
     */
    public void createCorpses() {
        for (ChunkData chunkData : plugin.getCacheManager().getChunkMap().values()) {
            for (EntityData entityData : chunkData.getEntityDataMap().values()) {
                if (entityData.getType() == EntityData.Type.CITIZENSNPC) {
                    if (plugin.getCacheManager().getGraveMap().containsKey(entityData.getUUIDGrave())) {
                        Grave grave = plugin.getCacheManager().getGraveMap().get(entityData.getUUIDGrave());

                        if (grave != null) {
                            createCorpse(entityData.getUUIDEntity(), entityData.getLocation(), grave, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a new NPC corpse at the specified location with the given grave data.
     *
     * @param location The location to spawn the NPC.
     * @param grave    The grave data for the NPC.
     */
    public void createCorpse(Location location, Grave grave) {
        createCorpse(UUID.randomUUID(), location, grave, true);
    }

    /**
     * Creates a new NPC corpse with a specific UUID at the given location using the provided grave data.
     *
     * @param uuid              The UUID for the NPC.
     * @param location          The location to spawn the NPC.
     * @param grave             The grave data for the NPC.
     * @param createEntityData  Whether to create entity data for the NPC.
     */
    public void createCorpse(UUID uuid, Location location, Grave grave, boolean createEntityData) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (plugin.getConfig("citizens.corpse.enabled", grave).getBoolean("citizens.corpse.enabled")
                    && grave.getOwnerType() == EntityType.PLAYER) {
                Player player = plugin.getServer().getPlayer(grave.getOwnerUUID());
                Location npcLocation = location.clone();

                if (player != null && npcLocation.getWorld() != null) {
                    location.getBlock().setType(Material.AIR);
                    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, grave.getUUID().toString());
                    npc.spawn(npcLocation);

                    npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(
                            grave.getOwnerName(),
                            grave.getOwnerTextureSignature(),
                            grave.getOwnerTexture()
                    );

                    try {
                        double x = plugin.getConfig("citizens.corpse.offset.x", grave)
                                .getDouble("citizens.corpse.offset.x");
                        double y = plugin.getConfig("citizens.corpse.offset.y", grave)
                                .getDouble("citizens.corpse.offset.y");
                        double z = plugin.getConfig("citizens.corpse.offset.z", grave)
                                .getDouble("citizens.corpse.offset.z");
                        npcLocation.add(x, y, z);
                    } catch (IllegalArgumentException handled) {
                        npcLocation.add(0.5, -0.2, 0.5);
                    }

                    npc.setProtected(plugin.getConfig("citizens.corpse.collide", grave).getBoolean("citizens.corpse.collide"));

                    // Set NPC equipment directly if it's a Player
                    if (npc.getEntity() instanceof Player) {
                        Player npcPlayer = (Player) npc.getEntity();

                        setNPCEquipment(npcPlayer, grave, EquipmentSlot.HEAD, "citizens.corpse.armor");
                        setNPCEquipment(npcPlayer, grave, EquipmentSlot.CHEST, "citizens.corpse.armor");
                        setNPCEquipment(npcPlayer, grave, EquipmentSlot.LEGS, "citizens.corpse.armor");
                        setNPCEquipment(npcPlayer, grave, EquipmentSlot.FEET, "citizens.corpse.armor");
                        setNPCEquipment(npcPlayer, grave, EquipmentSlot.HAND, "citizens.corpse.hand");

                        if (plugin.getVersionManager().hasSecondHand()) {
                            setNPCEquipment(npcPlayer, grave, EquipmentSlot.OFF_HAND, "citizens.corpse.hand");
                        }
                    }

                    if (plugin.getConfig("citizens.corpse.glow.enabled", grave)
                            .getBoolean("citizens.corpse.glow.enabled")) {
                        try {
                            npc.data().setPersistent(NPC.Metadata.valueOf("GLOWING_COLOR"), ChatColor.valueOf(plugin
                                    .getConfig("citizens.corpse.glow.color", grave)
                                    .getString("citizens.corpse.glow.color")).toString());
                        } catch (IllegalArgumentException ignored) {
                            npc.data().setPersistent(NPC.Metadata.GLOWING, true);
                        }
                    }

                    npc.data().setPersistent("grave_uuid", grave.getUUID().toString());

                    plugin.debugMessage("Spawning Citizens NPC for " + grave.getUUID() + " at "
                            + npcLocation.getWorld().getName() + ", " + (npcLocation.getBlockX() + 0.5) + "x, "
                            + (npcLocation.getBlockY() + 0.5) + "y, " + (npcLocation.getBlockZ() + 0.5) + "z", 1);

                    if (createEntityData) {
                        createEntityData(location, uuid, grave.getUUID(), EntityData.Type.CITIZENSNPC);
                    }
                }
            }
        });
    }

    private void setNPCEquipment(Player npcPlayer, Grave grave, EquipmentSlot slot, String configPath) {
        if (plugin.getConfig(configPath, grave).getBoolean(configPath) && grave.getEquipmentMap().containsKey(slot)) {
            ItemStack item = grave.getEquipmentMap().get(slot);
            switch (slot) {
                case HEAD:
                    npcPlayer.getInventory().setHelmet(item);
                    break;
                case CHEST:
                case BODY:
                    npcPlayer.getInventory().setChestplate(item);
                    break;
                case LEGS:
                    npcPlayer.getInventory().setLeggings(item);
                    break;
                case FEET:
                    npcPlayer.getInventory().setBoots(item);
                    break;
                case HAND:
                    npcPlayer.getInventory().setItemInMainHand(item);
                    break;
                case OFF_HAND:
                    npcPlayer.getInventory().setItemInOffHand(item);
                    break;
            }
        }
    }

    /**
     * Removes the NPC corpse associated with the given grave.
     *
     * @param grave The grave whose associated NPC corpse should be removed.
     */
    public void removeCorpse(Grave grave) {
        NPC npc = getNPCByUUID(grave.getUUID());
        if (npc != null) {
            npc.destroy();
        }
        removeCorpse(getEntityDataNPCMap(getLoadedEntityDataList(grave)));
    }

    /**
     * Removes the NPC corpse associated with the given entity data.
     *
     * @param entityData The entity data whose associated NPC corpse should be removed.
     */
    public void removeCorpse(EntityData entityData) {
        removeCorpse(getEntityDataNPCMap(Collections.singletonList(entityData)));
    }

    /**
     * Removes multiple NPC corpses based on the provided entity data map.
     *
     * @param entityDataMap A map of entity data to NPC instances to be removed.
     */
    public void removeCorpse(Map<EntityData, NPC> entityDataMap) {
        List<EntityData> entityDataList = new ArrayList<>();

        for (Map.Entry<EntityData, NPC> entry : entityDataMap.entrySet()) {
            entry.getValue().destroy();
            entityDataList.add(entry.getKey());
        }

        plugin.getDataManager().removeEntityData(entityDataList);
    }

    /**
     * Retrieves a map of entity data to NPC instances based on the provided entity data list.
     *
     * @param entityDataList The list of entity data to match with NPC instances.
     * @return A map of entity data to NPC instances.
     */
    private Map<EntityData, NPC> getEntityDataNPCMap(List<EntityData> entityDataList) {
        Map<EntityData, NPC> entityDataMap = new HashMap<>();

        for (EntityData entityData : entityDataList) {
            NPC npc = getNPCByUUID(entityData.getUUIDGrave());
            if (npc != null) {
                entityDataMap.put(entityData, npc);
            }
        }

        return entityDataMap;
    }

    private NPC getNPCByUUID(UUID uuid) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (uuid.equals(npc.getUniqueId())) {
                return npc;
            }
        }
        return null;
    }
}