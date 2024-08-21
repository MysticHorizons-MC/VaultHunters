package org.mystichorizons.vaultHunters.injector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.Vault;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.HologramHandler;
import org.mystichorizons.vaultHunters.handlers.TierItemsHandler;
import org.mystichorizons.vaultHunters.handlers.VaultTiersHandler;
import org.mystichorizons.vaultHunters.tables.LootItem;
import org.mystichorizons.vaultHunters.tables.LootTable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VaultLootInjector {

    private final VaultHunters plugin;
    private final VaultTiersHandler vaultTiersHandler;
    private final TierItemsHandler tierItemsHandler;
    private final HologramHandler hologramHandler;
    private final Map<UUID, Long> playerCooldowns;
    private final NamespacedKey tierKey;  // Added the NamespacedKey for persistent data

    public VaultLootInjector(VaultHunters plugin, VaultTiersHandler vaultTiersHandler, TierItemsHandler tierItemsHandler, HologramHandler hologramHandler) {
        this.plugin = plugin;
        this.vaultTiersHandler = vaultTiersHandler;
        this.tierItemsHandler = tierItemsHandler;
        this.hologramHandler = hologramHandler;
        this.playerCooldowns = new HashMap<>();
        this.tierKey = new NamespacedKey(plugin, "vault_tier");
        createVaultsFolder();
    }

    private void createVaultsFolder() {
        File vaultsFolder = new File(plugin.getDataFolder(), "vaults");
        if (!vaultsFolder.exists()) {
            if (vaultsFolder.mkdirs()) {
                plugin.getLogger().info("Vaults folder created successfully.");
            } else {
                plugin.getLogger().warning("Failed to create vaults folder.");
            }
        }
    }

    public boolean injectRandomTierLoot(Block vaultBlock, Player player) {
        UUID playerId = player.getUniqueId();

        // Determine the current tier of the vault, if any
        VaultTiersHandler.VaultTier currentTier = getCurrentVaultTier(vaultBlock);

        // If no current tier is found, or we want to randomly assign a new tier, get a new random tier
        VaultTiersHandler.VaultTier newTier = (currentTier != null) ? currentTier : vaultTiersHandler.getRandomTier();

        if (newTier != null) {
            // Assign the new tier to the vault block
            assignTierToVault(vaultBlock, newTier);

            // Check if the player is still on cooldown
            if (isOnCooldown(playerId, vaultBlock)) {
                player.sendMessage("You cannot loot this vault yet. Please wait for the cooldown to expire.");
                return false;
            }

            // Generate loot using the custom LootTable class
            LootTable lootTable = createLootTableForTier(newTier);
            List<ItemStack> lootItems = lootTable.generateLoot();

            if (vaultBlock.getState() instanceof Lootable) {
                lootItems.forEach(item -> vaultBlock.getWorld().dropItemNaturally(vaultBlock.getLocation(), item));
            } else {
                plugin.getLogger().warning("Vault block is not lootable or does not support loot tables.");
                return false;
            }

            // Update the hologram to show the new tier
            updateHologram(vaultBlock, newTier.getName());

            // Set the block state to INACTIVE after ejection
            setVaultState(vaultBlock, Vault.State.INACTIVE);

            // Record the cooldown and start the cooldown process
            startCooldown(vaultBlock, playerId, newTier);

            return true;
        } else {
            plugin.getLogger().warning("No tier found for vault loot injection.");
            return false;
        }
    }

    private LootTable createLootTableForTier(VaultTiersHandler.VaultTier tier) {
        LootTable lootTable = new LootTable();
        List<TierItemsHandler.TierItem> tierItems = tierItemsHandler.getTierItems(tier.getName());

        for (TierItemsHandler.TierItem tierItem : tierItems) {
            lootTable.addLootItem(new LootItem(tierItem.getItemStack(), tierItem.getChance(), tier.getMinItems(), tier.getMaxItems())); // Adjust min/max quantities based on tier
        }

        return lootTable;
    }

    private void updateHologram(Block vaultBlock, String tierName) {
        if (hologramHandler.isHologramEnabled()) {
            hologramHandler.createOrUpdateHologram(
                    vaultBlock.getLocation().add(0, plugin.getConfigHandler().getConfig().getDouble("vaults.hologram.above-vault", 1.5), 0),
                    tierName,
                    "0"
            );
        }
    }

    private void setVaultState(Block vaultBlock, Vault.State state) {
        if (vaultBlock.getBlockData() instanceof Vault vault) {
            vault.setTrialSpawnerState(state);
            vaultBlock.setBlockData(vault);
        } else {
            plugin.getLogger().warning("Vault block is not of type Vault.");
        }
    }

    private void startCooldown(Block vaultBlock, UUID playerId, VaultTiersHandler.VaultTier currentTier) {
        long cooldownTimeMillis = plugin.getConfigHandler().getCooldownTimeMillis();
        long cooldownEndTime = System.currentTimeMillis() + cooldownTimeMillis;

        playerCooldowns.put(playerId, cooldownEndTime);

        // Save the cooldown data
        savePlayerCooldown(vaultBlock, playerId, cooldownEndTime);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Set the vault back to ACTIVE state
                setVaultState(vaultBlock, Vault.State.ACTIVE);

                // Delete the cooldown file as the cooldown has ended
                deleteCooldownFile(vaultBlock);

                // Inject new loot tier (if player is near)
                if (isPlayerNearVault(vaultBlock, playerId)) {
                    injectRandomTierLoot(vaultBlock, plugin.getServer().getPlayer(playerId));
                }
            }
        }.runTaskLater(plugin, cooldownTimeMillis / 50); // Convert milliseconds to ticks
    }

    private String getVaultBlockId(Block vaultBlock) {
        return vaultBlock.getWorld().getName() + "_" + vaultBlock.getX() + "_" + vaultBlock.getY() + "_" + vaultBlock.getZ();
    }

    private boolean isOnCooldown(UUID playerId, Block vaultBlock) {
        Long cooldownEndTime = playerCooldowns.get(playerId);

        if (cooldownEndTime == null) {
            // Try to load the cooldown from the file
            cooldownEndTime = loadPlayerCooldown(vaultBlock);
            if (cooldownEndTime != null) {
                playerCooldowns.put(playerId, cooldownEndTime);
            }
        }

        return cooldownEndTime != null && cooldownEndTime > System.currentTimeMillis();
    }

    private boolean isPlayerNearVault(Block vaultBlock, UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            double distance = vaultBlock.getLocation().distance(player.getLocation());
            return distance <= plugin.getConfigHandler().getConfig().getDouble("vaults.player-near-distance", 10.0);
        }
        return false;
    }

    public void savePlayerCooldown(Block vaultBlock, UUID playerId, long cooldownEndTime) {
        String vaultId = getVaultBlockId(vaultBlock);
        File vaultFile = new File(plugin.getDataFolder(), "vaults/" + vaultId + ".yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(vaultFile);
        config.set("playerId", playerId.toString());
        config.set("cooldownEndTime", cooldownEndTime);

        try {
            config.save(vaultFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save cooldown for vault " + vaultId + ": " + e.getMessage());
        }
    }

    public Long loadPlayerCooldown(Block vaultBlock) {
        String vaultId = getVaultBlockId(vaultBlock);
        File vaultFile = new File(plugin.getDataFolder(), "vaults/" + vaultId + ".yml");

        if (!vaultFile.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(vaultFile);
        return config.getLong("cooldownEndTime", 0);
    }

    public void deleteCooldownFile(Block vaultBlock) {
        String vaultId = getVaultBlockId(vaultBlock);
        File vaultFile = new File(plugin.getDataFolder(), "vaults/" + vaultId + ".yml");

        if (vaultFile.exists()) {
            if (vaultFile.delete()) {
                plugin.getLogger().info("Cooldown file for vault " + vaultId + " deleted successfully.");
            } else {
                plugin.getLogger().warning("Failed to delete cooldown file for vault " + vaultId);
            }
        }
    }

    private VaultTiersHandler.VaultTier getCurrentVaultTier(Block vaultBlock) {
        if (!(vaultBlock.getState() instanceof TileState)) {
            return null;
        }

        TileState tileState = (TileState) vaultBlock.getState();
        PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
        String tierName = dataContainer.get(tierKey, PersistentDataType.STRING);

        if (tierName != null) {
            return vaultTiersHandler.getTier(tierName);
        } else {
            return null; // No tier assigned
        }
    }

    private void assignTierToVault(Block vaultBlock, VaultTiersHandler.VaultTier tier) {
        if (!(vaultBlock.getState() instanceof TileState)) {
            return;
        }

        TileState tileState = (TileState) vaultBlock.getState();
        PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
        dataContainer.set(tierKey, PersistentDataType.STRING, tier.getName());
        tileState.update();
    }
}
