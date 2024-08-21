package org.mystichorizons.vaultHunters.handlers;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.mystichorizons.vaultHunters.tables.LootItem;
import org.mystichorizons.vaultHunters.tables.LootTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TierItemsHandler {

    private final JavaPlugin plugin;
    private final LootTable lootTable;

    public TierItemsHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.lootTable = new LootTable();
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
                List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) config.getList("items");
                if (itemMaps != null) {
                    for (Map<String, Object> itemMap : itemMaps) {
                        ItemStack itemStack = deserializeItemStack((Map<String, Object>) itemMap.get("item"));
                        double chance = (double) itemMap.getOrDefault("chance", 1.0);
                        int minQuantity = (int) itemMap.getOrDefault("minQuantity", 1);
                        int maxQuantity = (int) itemMap.getOrDefault("maxQuantity", 1);
                        lootTable.addLootItem(new LootItem(itemStack, chance, minQuantity, maxQuantity));
                    }
                } else {
                    plugin.getLogger().warning("No items found in file: " + itemFile.getName());
                }
            }
        }
    }

    public synchronized void addLootItem(ItemStack item, double chance, int minQuantity, int maxQuantity) {
        lootTable.addLootItem(new LootItem(item, chance, minQuantity, maxQuantity));
    }

    public synchronized void removeLootItem(ItemStack item) {
        lootTable.removeLootItem(new LootItem(item, 0.0, 0, 0));
    }

    public synchronized void clearLootTable() {
        lootTable.getLootItems().clear();
    }

    public synchronized void clearLootTable(String tier) {
        lootTable.getLootItems().removeIf(lootItem -> lootItem.getItem().getItemMeta().getDisplayName().equals(tier));
    }

    public synchronized void clearLootTable(ItemStack item) {
        lootTable.getLootItems().removeIf(lootItem -> lootItem.getItem().isSimilar(item));
    }

    public synchronized List<ItemStack> generateLoot() {
        return lootTable.generateLoot();
    }

    public synchronized void saveLootTable(String tier) {
        File itemFile = new File(plugin.getDataFolder(), "items/" + tier + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (LootItem lootItem : lootTable.getLootItems()) {
            Map<String, Object> itemMap = serializeItemStack(lootItem.getItem());
            itemMap.put("chance", lootItem.getChance());
            itemMap.put("minQuantity", lootItem.getMinQuantity());
            itemMap.put("maxQuantity", lootItem.getMaxQuantity());
            itemMaps.add(itemMap);
        }
        config.set("items", itemMaps);
        try {
            config.save(itemFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not save loot table for tier: " + tier);
        }
    }

    private Map<String, Object> serializeItemStack(ItemStack item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("type", item.getType().toString());
        itemMap.put("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            itemMap.put("meta", meta.serialize());
        }

        return itemMap;
    }

    private ItemStack deserializeItemStack(Map<String, Object> itemMap) {
        Material type = Material.valueOf((String) itemMap.get("type"));
        int amount = (int) itemMap.get("amount");

        ItemStack item = new ItemStack(type, amount);

        if (itemMap.containsKey("meta")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metaMap = (Map<String, Object>) itemMap.get("meta");
            ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(metaMap, ItemMeta.class);
            if (meta != null) {
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    /**
     * Get all LootItems currently in the loot table.
     *
     * @return List of LootItems.
     */
    public synchronized List<LootItem> getLootTableItems() {
        return new ArrayList<>(lootTable.getLootItems());
    }

    /**
     * Get a specific LootItem by matching its ItemStack.
     *
     * @param itemStack The ItemStack to match.
     * @return The matching LootItem, or null if not found.
     */
    public synchronized LootItem getItem(ItemStack itemStack) {
        for (LootItem lootItem : lootTable.getLootItems()) {
            if (lootItem.getItem().isSimilar(itemStack)) {
                return lootItem;
            }
        }
        return null;
    }
}
