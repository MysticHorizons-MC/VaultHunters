package org.mystichorizons.vaultHunters.injector;

import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Vault;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.HologramHandler;
import org.mystichorizons.vaultHunters.handlers.TierItemsHandler;
import org.mystichorizons.vaultHunters.handlers.VaultTiersHandler;

import java.util.List;
import java.util.Random;

public class VaultLootInjector {

    private final VaultHunters plugin;
    private final VaultTiersHandler vaultTiersHandler;
    private final TierItemsHandler tierItemsHandler;
    private final HologramHandler hologramHandler;

    public VaultLootInjector(VaultHunters plugin, VaultTiersHandler vaultTiersHandler, TierItemsHandler tierItemsHandler, HologramHandler hologramHandler) {
        this.plugin = plugin;
        this.vaultTiersHandler = vaultTiersHandler;
        this.tierItemsHandler = tierItemsHandler;
        this.hologramHandler = hologramHandler;
    }

    public boolean injectRandomTierLoot(Block vaultBlock) {
        VaultTiersHandler.VaultTier tier = vaultTiersHandler.getRandomTier();
        if (tier != null) {
            // Inject loot into the block's NBT data
            injectLootIntoNBT(vaultBlock, tier);

            // Set the block state to EJECTING
            setVaultState(vaultBlock, Vault.State.EJECTING);

            // Update the hologram to show the current tier
            updateHologram(vaultBlock, tier.getName());

            // Set the block state to INACTIVE after ejection
            setVaultState(vaultBlock, Vault.State.INACTIVE);

            // Start cooldown and handle re-injection after cooldown
            startCooldown(vaultBlock, tier);

            return true;
        } else {
            plugin.getLogger().warning("No tier found for vault loot injection");
            return false;
        }
    }

    private void injectLootIntoNBT(Block vaultBlock, VaultTiersHandler.VaultTier tier) {
        NBTBlock nbtBlock = new NBTBlock(vaultBlock);
        NBTCompound blockData = nbtBlock.getData();

        // Clear existing loot data
        blockData.removeKey("VaultLoot");

        // Inject new loot data
        NBTCompound lootCompound = blockData.addCompound("VaultLoot");
        List<TierItemsHandler.TierItem> tierItems = tierItemsHandler.getTierItems(tier.getName());
        Random random = new Random();

        // Add items to NBT
        for (int i = 0; i < tierItems.size(); i++) {
            TierItemsHandler.TierItem tierItem = tierItems.get(i);
            NBTCompound itemCompound = lootCompound.addCompound("item" + i);
            itemCompound.setItemStack("itemStack", tierItem.getItemStack());
            itemCompound.setDouble("chance", tierItem.getChance());
        }

        // Optionally randomize loot
        if (plugin.getConfigHandler().getConfig().getBoolean("options.randomize-loot", true)) {
            int maxLoot = plugin.getConfigHandler().getConfig().getInt("options.loot-amount", 5);
            for (int i = lootCompound.getKeys().size(); i > maxLoot; i--) {
                lootCompound.removeKey("item" + random.nextInt(i));
            }
        }

        // No need to set the data back, NBT API handles this internally
    }

    public void dropLootFromNBT(Block vaultBlock, Player player) {
        NBTBlock nbtBlock = new NBTBlock(vaultBlock);
        NBTCompound blockData = nbtBlock.getData();

        if (!blockData.hasKey("VaultLoot")) return;

        NBTCompound lootCompound = blockData.getCompound("VaultLoot");

        // Drop items based on the stored NBT data
        for (String key : lootCompound.getKeys()) {
            NBTCompound itemCompound = lootCompound.getCompound(key);
            ItemStack itemStack = itemCompound.getItemStack("itemStack");
            double chance = itemCompound.getDouble("chance");

            if (new Random().nextDouble() * 100 <= chance) {
                vaultBlock.getWorld().dropItemNaturally(vaultBlock.getLocation(), itemStack);
            }
        }

        // Clear the loot data after dropping items
        blockData.removeKey("VaultLoot");
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

    private void startCooldown(Block vaultBlock, VaultTiersHandler.VaultTier currentTier) {
        long cooldownTimeMillis = plugin.getConfigHandler().getCooldownTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Set the vault back to ACTIVE state
                setVaultState(vaultBlock, Vault.State.ACTIVE);

                // Inject new loot tier
                injectRandomTierLoot(vaultBlock);
            }
        }.runTaskLater(plugin, cooldownTimeMillis / 50); // Convert milliseconds to ticks
    }
}
