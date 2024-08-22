package org.mystichorizons.vaultHunters.tables;

import org.bukkit.Material;
import org.bukkit.block.Block;

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

    private void generateVaultLoot(PlayerLoot playerLoot) {
        LootTable lootTable = new LootTable();
        for (ItemStack item : lootTable.generateLoot()) {
            playerLoot.addLoot(item);
            block.getWorld().dropItemNaturally(block.getLocation(), item);
        }
    }
}
