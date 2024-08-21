package org.mystichorizons.vaultHunters.tables;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VaultBlock {
    private final Block block;
    private boolean isOminous;

    public VaultBlock(Block block) {
        this.block = block;
        this.isOminous = false;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isOminous() {
        return isOminous;
    }

    public void setOminous(boolean ominous) {
        this.isOminous = ominous;
    }

    public boolean isValidKey(ItemStack key, Material trialKeyMaterial, Material ominousTrialKeyMaterial) {
        Material keyType = key.getType();
        return keyType == trialKeyMaterial || keyType == ominousTrialKeyMaterial;
    }

    public void handleVaultBlockInteraction(PlayerLoot playerLoot, ItemStack key, Material trialKeyMaterial, Material ominousTrialKeyMaterial) {
        if (isValidKey(key, trialKeyMaterial, ominousTrialKeyMaterial)) {
            if (key.getType() == ominousTrialKeyMaterial) {
                setOminous(true);
            }
            generateVaultLoot(playerLoot);
        } else {
            block.getWorld().createExplosion(block.getLocation(), 0);  // Example: Handle invalid key interaction
        }
    }

    private void generateVaultLoot(PlayerLoot playerLoot) {
        LootTable lootTable = new LootTable(); // Replace with specific vault's loot table
        for (ItemStack item : lootTable.generateLoot()) {
            playerLoot.addLoot(item);
            block.getWorld().dropItemNaturally(block.getLocation(), item);
        }
    }
}
