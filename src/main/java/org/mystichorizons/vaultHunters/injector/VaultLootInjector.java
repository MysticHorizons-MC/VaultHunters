package org.mystichorizons.vaultHunters.injector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Vault;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.HologramHandler;
import org.mystichorizons.vaultHunters.handlers.TierItemsHandler;
import org.mystichorizons.vaultHunters.handlers.VaultTiersHandler;

import java.util.ArrayList;
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
            // Generate loot based on the tier
            List<ItemStack> lootItems = getLootForTier(tier);

            if (vaultBlock.getState() instanceof Lootable lootable) {
                lootable.clearLootTable(); // Clear any existing loot table
                lootItems.forEach(item -> vaultBlock.getWorld().dropItemNaturally(vaultBlock.getLocation(), item));
            } else {
                plugin.getLogger().warning("Vault block is not lootable or does not support loot tables.");
                return false;
            }

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

    private List<ItemStack> getLootForTier(VaultTiersHandler.VaultTier tier) {
        List<ItemStack> lootItems = new ArrayList<>();
        List<TierItemsHandler.TierItem> tierItems = tierItemsHandler.getTierItems(tier.getName());

        if (tierItems.isEmpty()) {
            plugin.getLogger().warning("No items found for tier: " + tier.getName());
            return lootItems;
        }

        Random random = new Random();
        int minItems = tier.getMinItems();
        int maxItems = tier.getMaxItems();
        int numberOfItemsToGenerate = minItems + random.nextInt(maxItems - minItems + 1);

        for (int i = 0; i < numberOfItemsToGenerate; i++) {
            ItemStack randomItem = getRandomItemFromTier(tierItems);
            if (randomItem != null) {
                lootItems.add(randomItem);
            }
        }

        return lootItems;
    }

    private ItemStack getRandomItemFromTier(List<TierItemsHandler.TierItem> tierItems) {
        double totalWeight = tierItems.stream().mapToDouble(TierItemsHandler.TierItem::getChance).sum();
        double random = new Random().nextDouble() * totalWeight;

        for (TierItemsHandler.TierItem item : tierItems) {
            random -= item.getChance();
            if (random <= 0) {
                return item.getItemStack();
            }
        }
        return null;
    }

    public void dropLootFromNBT(Block vaultBlock, Player player) {
        if (vaultBlock.getState() instanceof Lootable lootable) {
            if (lootable.getLootTable() == null) {
                plugin.getLogger().warning("No loot table set for this vault block.");
                return;
            }

            // Build LootContext with optional luck based on PotionEffect
            LootContext.Builder builder = new LootContext.Builder(vaultBlock.getLocation())
                    .lootedEntity(player)
                    .luck(player.hasPotionEffect(PotionEffectType.LUCK) ? 1.0f : 0.0f);

            LootContext lootContext = builder.build();

            lootable.getLootTable().populateLoot(new Random(), lootContext);
        } else {
            plugin.getLogger().warning("Vault block is not lootable.");
        }
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
