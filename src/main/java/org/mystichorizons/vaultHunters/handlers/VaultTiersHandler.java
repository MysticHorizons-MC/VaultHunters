package org.mystichorizons.vaultHunters.handlers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mystichorizons.vaultHunters.tables.LootItem;
import org.mystichorizons.vaultHunters.tables.LootTable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.mystichorizons.vaultHunters.util.ItemStackSerializer.deserializeItemStack;
import static org.mystichorizons.vaultHunters.util.ItemStackSerializer.serializeItemStack;

public class VaultTiersHandler {

    private final JavaPlugin plugin;
    private final Map<String, VaultTier> vaultTiers;
    private final File tiersFile;
    private final FileConfiguration config;

    public VaultTiersHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.vaultTiers = new HashMap<>();
        this.tiersFile = new File(plugin.getDataFolder(), "tiers.yml");
        this.config = YamlConfiguration.loadConfiguration(tiersFile);
        loadVaultTiers();
    }

    // Load vault tiers from the tiers.yml file
    public void loadVaultTiers() {
        if (!tiersFile.exists()) {
            plugin.saveResource("tiers.yml", false);
        }

        Set<String> tierKeys = config.getConfigurationSection("tiers").getKeys(false);
        for (String key : tierKeys) {
            String tierPath = "tiers." + key;
            String name = config.getString(tierPath + ".name", key);
            String file = config.getString(tierPath + ".file", key + ".yml");
            double chance = config.getDouble(tierPath + ".chance", 0.0);
            String numberOfItems = config.getString(tierPath + ".number_of_items", "1-1");

            // Load the associated LootTable from the file
            LootTable lootTable = loadLootTable(file);

            VaultTier tier = new VaultTier(name, file, chance, numberOfItems, lootTable);
            vaultTiers.put(key.toLowerCase(), tier);
        }
    }

    // Save vault tiers back to the tiers.yml file
    public void saveVaultTiers() {
        for (Map.Entry<String, VaultTier> entry : vaultTiers.entrySet()) {
            String key = entry.getKey();
            VaultTier tier = entry.getValue();
            String tierPath = "tiers." + key;
            config.set(tierPath + ".name", tier.getName());
            config.set(tierPath + ".file", tier.getFile());
            config.set(tierPath + ".chance", tier.getChance());
            config.set(tierPath + ".number_of_items", tier.getNumberOfItems());

            // Save the associated LootTable to its file
            saveLootTable(tier.getFile(), tier.getLootTable());
        }
        saveConfigFile();
    }

    // Load a LootTable from the specified file
    public LootTable loadLootTable(String fileName) {
        File file = new File(plugin.getDataFolder(), "items/" + fileName);
        LootTable lootTable = new LootTable();
        if (file.exists()) {
            FileConfiguration lootConfig = YamlConfiguration.loadConfiguration(file);
            List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) lootConfig.getList("items");
            if (itemMaps != null) {
                for (Map<String, Object> itemMap : itemMaps) {
                    ItemStack itemStack = deserializeItemStack((Map<String, Object>) itemMap.get("item"));
                    double chance = (double) itemMap.getOrDefault("chance", 1.0);
                    int minQuantity = (int) itemMap.getOrDefault("minQuantity", 1);
                    int maxQuantity = (int) itemMap.getOrDefault("maxQuantity", 1);
                    lootTable.addLootItem(new LootItem(itemStack, chance, minQuantity, maxQuantity));
                }
            }
        }
        return lootTable;
    }

    // Save a LootTable to the specified file
    public void saveLootTable(String fileName, LootTable lootTable) {
        File file = new File(plugin.getDataFolder(), "items/" + fileName);
        YamlConfiguration lootConfig = new YamlConfiguration();
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (LootItem lootItem : lootTable.getLootItems()) {
            Map<String, Object> itemMap = serializeItemStack(lootItem.getItem());
            itemMap.put("chance", lootItem.getChance());
            itemMap.put("minQuantity", lootItem.getMinQuantity());
            itemMap.put("maxQuantity", lootItem.getMaxQuantity());
            itemMaps.add(itemMap);
        }
        lootConfig.set("items", itemMaps);
        try {
            lootConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save loot table file: " + fileName);
            e.printStackTrace();
        }
    }

    // Get a vault tier by name
    public VaultTier getTier(String tierName) {
        return vaultTiers.getOrDefault(tierName.toLowerCase(), new VaultTier("&eCommon", "Common.yml", 50.0, "5-10", new LootTable()));
    }

    // Get all vault tier names
    public Set<String> getAllTierNames() {
        return vaultTiers.keySet();
    }

    // Get all vault tiers as an array
    public VaultTier[] getAllTiers() {
        return vaultTiers.values().toArray(new VaultTier[0]);
    }

    // Check if a vault tier exists
    public boolean vaultTierExists(String tierName) {
        return vaultTiers.containsKey(tierName.toLowerCase());
    }

    // Create a new vault tier
    public void createVaultTier(String tierName) {
        String lowerCaseTierName = tierName.toLowerCase();
        if (vaultTierExists(lowerCaseTierName)) {
            throw new IllegalArgumentException("Vault tier with this name already exists!");
        }
        VaultTier newTier = new VaultTier("&e" + tierName, tierName + ".yml", 0.0, "1-1", new LootTable());
        vaultTiers.put(lowerCaseTierName, newTier);
        saveVaultTiers();
    }

    // Delete a vault tier from memory and file
    public void deleteTier(String tierName) {
        String lowerCaseTierName = tierName.toLowerCase();

        // Remove from memory
        vaultTiers.remove(lowerCaseTierName);

        // Remove from configuration file
        config.set("tiers." + lowerCaseTierName, null);
        saveConfigFile();

        // Remove associated items/<tier>.yml file
        File tierFile = new File(plugin.getDataFolder(), "items/" + lowerCaseTierName + ".yml");
        if (tierFile.exists() && !tierFile.delete()) {
            plugin.getLogger().warning("Failed to delete file: " + tierFile.getPath());
        }
    }

    // Check if a tier is saved in the tiers.yml file
    public boolean isTierSaved(String tierName) {
        return config.contains("tiers." + tierName.toLowerCase());
    }

    // Get a random vault tier based on chances
    public VaultTier getRandomTier() {
        double totalWeight = vaultTiers.values().stream().mapToDouble(VaultTier::getChance).sum();
        double random = Math.random() * totalWeight;

        for (VaultTier tier : vaultTiers.values()) {
            random -= tier.getChance();
            if (random <= 0) {
                return tier;
            }
        }
        return vaultTiers.getOrDefault("common", new VaultTier("&eCommon", "Common.yml", 50.0, "5-10", new LootTable())); // Default to "Common"
    }

    // Save a specific tier
    public void saveTier(String tierName) {
        VaultTier tier = vaultTiers.get(tierName.toLowerCase());
        if (tier != null) {
            String tierPath = "tiers." + tierName.toLowerCase();
            config.set(tierPath + ".name", tier.getName());
            config.set(tierPath + ".file", tier.getFile());
            config.set(tierPath + ".chance", tier.getChance());
            config.set(tierPath + ".number_of_items", tier.getNumberOfItems());
            saveLootTable(tier.getFile(), tier.getLootTable());
            saveConfigFile();
        } else {
            plugin.getLogger().warning("Attempted to save non-existent tier: " + tierName);
        }
    }

    // Save the configuration file
    private void saveConfigFile() {
        try {
            config.save(tiersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save tiers.yml file!");
            e.printStackTrace();
        }
    }

    // Nested VaultTier class representing a vault tier
    public static class VaultTier {
        private final String name;
        private final String file;
        private final double chance;
        private final String numberOfItems;
        private final LootTable lootTable;

        public VaultTier(String name, String file, double chance, String numberOfItems, LootTable lootTable) {
            this.name = name;
            this.file = file;
            this.chance = chance;
            this.numberOfItems = numberOfItems;
            this.lootTable = lootTable;
        }

        public String getName() {
            return name;
        }

        public String getFile() {
            return file;
        }

        public double getChance() {
            return chance;
        }

        public String getNumberOfItems() {
            return numberOfItems;
        }

        public int getMinItems() {
            String[] range = numberOfItems.split("-");
            return Integer.parseInt(range[0]);
        }

        public int getMaxItems() {
            String[] range = numberOfItems.split("-");
            return Integer.parseInt(range[1]);
        }

        public LootTable getLootTable() {
            return lootTable;
        }
    }
}
