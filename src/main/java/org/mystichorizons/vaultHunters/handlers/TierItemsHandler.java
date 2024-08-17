package org.mystichorizons.vaultHunters.handlers;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TierItemsHandler {

    private final JavaPlugin plugin;
    private final Map<String, List<TierItem>> tieredItems;

    public TierItemsHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.tieredItems = new HashMap<>();
        loadTieredItems();
    }

    public synchronized void loadTieredItems() {
        File itemsFolder = new File(plugin.getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
        }

        for (File itemFile : itemsFolder.listFiles()) {
            if (itemFile.isFile() && itemFile.getName().endsWith(".yml")) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(itemFile);
                String tierName = itemFile.getName().replace(".yml", "");
                List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) config.getList("items");
                List<TierItem> items = new ArrayList<>();
                if (itemMaps != null) {
                    for (Map<String, Object> itemMap : itemMaps) {
                        ItemStack itemStack = deserializeItemStack((Map<String, Object>) itemMap.get("item"));
                        double chance = (double) itemMap.getOrDefault("chance", 1.0);
                        items.add(new TierItem(itemStack, chance));
                    }
                } else {
                    plugin.getLogger().warning("No items found in tier: " + tierName);
                }
                tieredItems.put(tierName, items);
            }
        }
    }

    public synchronized List<TierItem> getTierItems(String tier) {
        return tieredItems.getOrDefault(tier, new ArrayList<>());
    }

    public synchronized void saveTierItems(String tier, List<TierItem> items) {
        File itemFile = new File(plugin.getDataFolder(), "items/" + tier + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (TierItem tierItem : items) {
            Map<String, Object> itemMap = serializeItemStack(tierItem.getItemStack());
            itemMap.put("chance", tierItem.getChance());
            itemMaps.add(itemMap);
        }
        config.set("items", itemMaps);
        try {
            config.save(itemFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not save tier items for tier: " + tier);
        }
    }

    public synchronized void addItemToTier(String tier, ItemStack item, double chance) {
        List<TierItem> items = getTierItems(tier);
        items.add(new TierItem(item, chance));
        saveTierItems(tier, items);
    }

    public synchronized void editTierItem(String tier, int index, ItemStack newItem, double newChance) {
        List<TierItem> items = getTierItems(tier);
        if (index >= 0 && index < items.size()) {
            items.set(index, new TierItem(newItem, newChance));
            saveTierItems(tier, items);
        } else {
            plugin.getLogger().warning("Item index out of bounds for tier: " + tier);
        }
    }

    public synchronized void removeItemFromTier(String tier, int index) {
        List<TierItem> items = getTierItems(tier);
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            saveTierItems(tier, items);
        } else {
            plugin.getLogger().warning("Item index out of bounds for tier: " + tier);
        }
    }

    public synchronized void removeItemFromTier(String tier, ItemStack item) {
        List<TierItem> items = getTierItems(tier);
        boolean removed = items.removeIf(tierItem -> tierItem.getItemStack().equals(item));
        if (removed) {
            saveTierItems(tier, items);
        } else {
            plugin.getLogger().warning("Item not found in tier: " + tier);
        }
    }

    public synchronized ItemStack getRandomItemFromTier(String tier) {
        List<TierItem> items = getTierItems(tier);
        double totalWeight = items.stream().mapToDouble(TierItem::getChance).sum();
        double random = new Random().nextDouble() * totalWeight;

        for (TierItem item : items) {
            random -= item.getChance();
            if (random <= 0) {
                return item.getItemStack();
            }
        }
        return null; // Fallback, though this should never happen if weights are correctly set
    }

    public synchronized void addNBTDataToItem(ItemStack item, String key, String value) {
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(key, value);
        nbtItem.applyNBT(item); // Applies the NBT data to the item
    }

    public synchronized String getNBTDataFromItem(ItemStack item, String key) {
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.getString(key);
    }

    // Helper method to serialize an ItemStack to a Map
    private Map<String, Object> serializeItemStack(ItemStack item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("type", item.getType().toString());
        itemMap.put("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            itemMap.put("meta", meta.serialize());
        }

        NBTItem nbtItem = new NBTItem(item);
        itemMap.put("nbt", nbtItem.toString()); // Store NBT data as a string

        return itemMap;
    }

    // Helper method to deserialize a Map to an ItemStack
    private ItemStack deserializeItemStack(Map<String, Object> itemMap) {
        Material type = Material.valueOf((String) itemMap.get("type"));
        int amount = (int) itemMap.get("amount");

        ItemStack item = new ItemStack(type, amount);

        if (itemMap.containsKey("meta")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metaMap = (Map<String, Object>) itemMap.get("meta");
            ItemMeta meta = plugin.getServer().getItemFactory().getItemMeta(type);
            if (meta != null) {
                if (metaMap.containsKey("customModelData")) {
                    Object customModelData = metaMap.get("customModelData");
                    if (customModelData instanceof Integer) {
                        meta.setCustomModelData((Integer) customModelData);
                    } else if (customModelData instanceof String) {
                        try {
                            meta.setCustomModelData(Integer.parseInt((String) customModelData));
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Failed to parse customModelData: " + customModelData);
                        }
                    }
                }

                if (metaMap.containsKey("displayName")) {
                    meta.displayName(Component.text((String) metaMap.get("displayName")));
                }

                // Add handling for other meta properties like lore
                if (metaMap.containsKey("lore")) {
                    @SuppressWarnings("unchecked")
                    List<String> loreStrings = (List<String>) metaMap.get("lore");
                    List<Component> loreComponents = new ArrayList<>();
                    for (String lore : loreStrings) {
                        loreComponents.add(Component.text(lore));
                    }
                    meta.lore(loreComponents);
                }

                item.setItemMeta(meta);
            }
        }

        // Handle NBT data
        if (itemMap.containsKey("nbt")) {
            NBTItem nbtItem = new NBTItem(item);
            ReadWriteNBT nbtData = NBT.parseNBT(itemMap.get("nbt").toString());
            nbtItem.mergeCompound(nbtData);
            nbtItem.applyNBT(item);
        }

        return item;
    }

    // Inner class representing an item with an associated drop chance
    public static class TierItem {
        private final ItemStack itemStack;
        private final double chance;

        public TierItem(ItemStack itemStack, double chance) {
            this.itemStack = itemStack;
            this.chance = chance;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public double getChance() {
            return chance;
        }
    }
}
