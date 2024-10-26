package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.StringUtil;
import me.imdanix.text.MiniTranslator;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the creation and manipulation of ItemStacks related to graves.
 */
public final class ItemStackManager extends EntityDataManager {
    /**
     * The main plugin instance associated with Graves.
     * <p>
     * This {@link Graves} instance represents the core plugin that this Graves is part of. It provides access
     * to the plugin's functionality, configuration, and other services.
     * </p>
     */
    private final Graves plugin;

    private Entity e = null;

    /**
     * Initializes a new instance of the ItemStackManager class.
     *
     * @param plugin The plugin instance.
     */
    public ItemStackManager(Graves plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    /**
     * Creates an ItemStack representing the obituary of a grave.
     *
     * @param grave The grave to create an obituary for.
     * @return The created ItemStack.
     */
    public ItemStack getGraveObituary(Grave grave) {
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
        Enchantment durability = plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");

        if (bookMeta != null) {
            if (plugin.getIntegrationManager().hasMiniMessage()) {
                List<String> lineList = new ArrayList<>();
                List<String> loreList = new ArrayList<>();

                for (String lore : plugin.getConfig("obituary.line", grave).getStringList("obituary.line")) {
                    lineList.add(MiniMessage.convertLegacyToMiniMessage(StringUtil.parseString(lore, grave.getLocationDeath(), grave, plugin)));
                }

                for (String string : plugin.getConfig("obituary.lore", grave).getStringList("obituary.lore")) {
                    loreList.add(MiniMessage.convertLegacyToMiniMessage(StringUtil.parseString(string, grave.getLocationDeath(), grave, plugin)));
                }

                // Split lineList into pages, with each page having up to 13 lines
                List<List<String>> pages = splitIntoPages(lineList, 13);

                int customModelData = plugin.getConfig("obituary.model-data", grave).getInt("obituary.model-data", -1);

                if (customModelData > -1) {
                    bookMeta.setCustomModelData(customModelData);
                }

                if (plugin.getConfig("obituary.glow", grave).getBoolean("obituary.glow")) {
                    bookMeta.addEnchant(durability, 1, true);

                    if (!plugin.getVersionManager().is_v1_7()) {
                        bookMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                }

                if (!plugin.getVersionManager().is_v1_7() && !plugin.getVersionManager().is_v1_8()
                        && !plugin.getVersionManager().is_v1_9()) {
                    bookMeta.setGeneration(null);
                }

                // Convert pages from List<List<String>> to List<Component>
                List<Component> componentPages = pages.stream()
                        .map(page -> MiniMessage.miniMessage().deserialize(String.join("\n", page)))
                        .collect(Collectors.toList());

                List<Component> componentList = loreList.stream()
                        .map(lore -> MiniMessage.miniMessage().deserialize(lore))
                        .collect(Collectors.toList());

                String title = plugin.getConfig("obituary.title", grave).getString("obituary.title");

                String author = plugin.getConfig("obituary.author", grave).getString("obituary.author");

                String titleOriginal = StringUtil.parseString(title, grave, plugin);

                String authorOriginal = StringUtil.parseString(author, grave, plugin);

                Component titleConverted = MiniMessage.miniMessage.deserialize(MiniMessage.convertLegacyToMiniMessage(titleOriginal));

                Component authorConverted = MiniMessage.miniMessage.deserialize(MiniMessage.convertLegacyToMiniMessage(authorOriginal));

                return MiniMessage.formatBookMeta(itemStack,
                        titleConverted,
                        authorConverted,
                        componentPages, componentList);
            } else {
                List<String> lineList = new ArrayList<>();
                List<String> loreList = new ArrayList<>();

                for (String lore : plugin.getConfig("obituary.line", grave).getStringList("obituary.line")) {
                    lineList.add(StringUtil.parseString(lore, grave.getLocationDeath(), grave, plugin));
                }

                for (String string : plugin.getConfig("obituary.lore", grave).getStringList("obituary.lore")) {
                    loreList.add(StringUtil.parseString(string, grave.getLocationDeath(), grave, plugin));
                }

                // Split lineList into pages, with each page having up to 13 lines
                List<List<String>> pages = splitIntoPages(lineList, 13);

                int customModelData = plugin.getConfig("obituary.model-data", grave).getInt("obituary.model-data", -1);

                if (customModelData > -1) {
                    bookMeta.setCustomModelData(customModelData);
                }

                if (plugin.getConfig("obituary.glow", grave).getBoolean("obituary.glow")) {
                    bookMeta.addEnchant(durability, 1, true);

                    if (!plugin.getVersionManager().is_v1_7()) {
                        bookMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                }

                if (!plugin.getVersionManager().is_v1_7() && !plugin.getVersionManager().is_v1_8()
                        && !plugin.getVersionManager().is_v1_9()) {
                    bookMeta.setGeneration(null);
                }

                // Convert pages back to List<String> for legacy handling
                List<String> stringPages = pages.stream()
                        .map(page -> String.join("\n", page))
                        .collect(Collectors.toList());

                bookMeta.setPages(String.join("\n", stringPages));
                bookMeta.setLore(loreList);
                bookMeta.setTitle(ChatColor.WHITE + StringUtil.parseString(plugin.getConfig("obituary.title", grave)
                        .getString("obituary.title"), grave, plugin));
                bookMeta.setAuthor(StringUtil.parseString(plugin.getConfig("obituary.author", grave)
                        .getString("obituary.author"), grave, plugin));
                itemStack.setItemMeta(bookMeta);
            }
        }

        return itemStack;
    }

    /**
     * Splits a list of strings into sublists, each containing up to maxLinesPerPage lines.
     * @param lines The list of strings to split.
     * @param maxLinesPerPage The maximum number of lines per page.
     * @return A list of pages, where each page is a list of strings.
     */
    private List<List<String>> splitIntoPages(List<String> lines, int maxLinesPerPage) {
        List<List<String>> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += maxLinesPerPage) {
            pages.add(new ArrayList<>(lines.subList(i, Math.min(i + maxLinesPerPage, lines.size()))));
        }
        return pages;
    }

    /**
     * Creates an ItemStack representing the head of a grave owner.
     *
     * @param grave The grave to create a head for.
     * @return The created ItemStack.
     */
    public ItemStack getGraveHead(Grave grave) {
        ItemStack itemStack = plugin.getCompatibility().getSkullItemStack(grave, plugin);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            List<String> loreList = new ArrayList<>();

            for (String string : plugin.getConfig("head.lore", grave).getStringList("head.lore")) {
                if (plugin.getIntegrationManager().hasMiniMessage()) {
                    String loreNew = StringUtil.parseString("&7" + string, grave.getLocationDeath(), grave, plugin);
                    loreList.add(MiniMessage.parseString(loreNew));
                } else {
                    loreList.add(ChatColor.GRAY + StringUtil.parseString(string, grave.getLocationDeath(), grave, plugin));
                }

            }

            int customModelData = plugin.getConfig("head.model-data", grave).getInt("head.model-data", -1);

            if (customModelData > -1) {
                itemMeta.setCustomModelData(customModelData);
            }

            itemMeta.setLore(loreList);

            String displayName;
            if (plugin.getIntegrationManager().hasMiniMessage()) {
                String displayNameNew = StringUtil.parseString("&f" + plugin.getConfig("head.name", grave)
                        .getString("head.name"), grave, plugin);
                displayName = MiniMessage.parseString(displayNameNew);
            } else {
                displayName = ChatColor.WHITE + StringUtil.parseString(plugin.getConfig("head.name", grave)
                        .getString("head.name"), grave, plugin);
            }

            itemMeta.setDisplayName(displayName);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    /**
     * Creates an ItemStack representing a grave in a list.
     *
     * @param number The number to display on the item.
     * @param grave  The grave to create the ItemStack for.
     * @return The created ItemStack.
     */
    public ItemStack createGraveListItemStack(int number, Grave grave) {
        Material material;
        Enchantment durability = plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");

        if (plugin.getConfig("gui.menu.list.item.block", grave).getBoolean("gui.menu.list.item.block")) {
            String materialString = plugin.getConfig("block.material", grave)
                    .getString("block.material", "CHEST");

            if (materialString.equals("PLAYER_HEAD") && !plugin.getVersionManager().hasBlockData()) {
                materialString = "SKULL_ITEM";
            }

            material = Material.matchMaterial(materialString);
        } else {
            material = Material.matchMaterial(plugin.getConfig("gui.menu.list.item.material", grave)
                    .getString("gui.menu.list.item.block", "CHEST"));
        }

        if (material == null) {
            material = Material.CHEST;
        }

        ItemStack itemStack = new ItemStack(material);

        if (itemStack.getType().name().equals("PLAYER_HEAD") || itemStack.getType().name().equals("SKULL_ITEM")) {
            itemStack = plugin.getCompatibility().getSkullItemStack(grave, plugin);
        }

        if (itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String name;
            if (plugin.getIntegrationManager().hasMiniMessage()) {
                String newName = StringUtil.parseString("&f" + plugin.getConfig("gui.menu.list.name", grave)
                        .getString("gui.menu.list.name"), grave, plugin).replace("%number%",
                        String.valueOf(number));
                name = MiniMessage.parseString(newName);
            } else {
                name = ChatColor.WHITE + StringUtil.parseString(plugin.getConfig("gui.menu.list.name", grave)
                        .getString("gui.menu.list.name"), grave, plugin).replace("%number%",
                        String.valueOf(number));
            }
            List<String> loreList = new ArrayList<>();
            int customModelData = plugin.getConfig("gui.menu.list.model-data", grave)
                    .getInt("gui.menu.list.model-data", -1);

            for (String string : plugin.getConfig("gui.menu.list.lore", grave).getStringList("gui.menu.list.lore")) {
                e = Bukkit.getEntity(grave.getOwnerUUID());
                if (plugin.getIntegrationManager().hasMiniMessage()) {
                    String loreOriginal = StringUtil.parseString("&7" + string, e, grave.getLocationDeath(), grave, plugin);
                    loreList.add(MiniMessage.parseString(loreOriginal));
                } else {
                    loreList.add(ChatColor.GRAY + StringUtil.parseString(string, e, grave.getLocationDeath(), grave, plugin));
                }
            }

            if (plugin.getConfig().getBoolean("gui.menu.list.glow")) {
                itemMeta.addEnchant(durability, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            if (customModelData > -1) {
                itemMeta.setCustomModelData(customModelData);
            }

            itemMeta.setDisplayName(name);
            itemMeta.setLore(loreList);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    /**
     * Creates an ItemStack for a grave menu slot.
     *
     * @param slot  The slot number.
     * @param grave The grave to create the ItemStack for.
     * @return The created ItemStack.
     */
    public ItemStack createGraveMenuItemStack(int slot, Grave grave) {
        String materialString = plugin.getConfig("gui.menu.grave.slot." + slot + ".material", grave)
                .getString("gui.menu.grave.slot." + slot + ".material", "PAPER");
        Material material = Material.matchMaterial(materialString);
        Enchantment durability = plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");

        if (material == null) {
            material = Material.PAPER;

            plugin.debugMessage(materialString.toUpperCase() + " is not a Material ENUM", 1);
        }

        ItemStack itemStack = new ItemStack(material);

        if (itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String name;
            if (plugin.getIntegrationManager().hasMiniMessage()) {
                String newName = StringUtil.parseString("&f" + plugin.getConfig("gui.menu.grave.slot." + slot + ".name", grave)
                        .getString("gui.menu.grave.slot." + slot + ".name"), grave, plugin);
                name = MiniMessage.parseString(newName);
            } else {
                name = ChatColor.WHITE + StringUtil.parseString(plugin.getConfig("gui.menu.grave.slot." + slot + ".name", grave)
                        .getString("gui.menu.grave.slot." + slot + ".name"), grave, plugin);
            }
            List<String> loreList = new ArrayList<>();
            int customModelData = plugin.getConfig("gui.menu.grave.slot." + slot + ".model-data", grave)
                    .getInt("gui.menu.grave.slot." + slot + ".model-data", -1);

            for (String string : plugin.getConfig("gui.menu.grave.slot." + slot + ".lore", grave)
                    .getStringList("gui.menu.grave.slot." + slot + ".lore")) {
                e = Bukkit.getEntity(grave.getOwnerUUID());
                if (plugin.getIntegrationManager().hasMiniMessage()) {
                    String newLore = StringUtil.parseString("&7" + string, e, grave.getLocationDeath(), grave, plugin);
                    loreList.add(MiniMessage.parseString(newLore));
                } else {
                    loreList.add(ChatColor.GRAY + StringUtil.parseString(string, e, grave.getLocationDeath(), grave, plugin));
                }

            }

            if (plugin.getConfig().getBoolean("gui.menu.grave.slot." + slot + ".glow")) {
                itemMeta.addEnchant(durability, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            if (customModelData > -1) {
                itemMeta.setCustomModelData(customModelData);
            }

            itemMeta.setDisplayName(name);
            itemMeta.setLore(loreList);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}