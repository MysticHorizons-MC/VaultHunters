package org.mystichorizons.vaultHunters.tables;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BlockLoot {
    private final Block block;
    private final LootTable lootTable;

    public BlockLoot(Block block, LootTable lootTable) {
        this.block = block;
        this.lootTable = lootTable;
    }

    public Block getBlock() {
        return block;
    }

    public LootTable getLootTable() {
        return lootTable;
    }

    public boolean isLootableBlock() {
        Material type = block.getType();
        return type == Material.CHEST || type == Material.ENDER_CHEST || type == Material.BARREL;
    }

    public void generateLoot(PlayerLoot playerLoot) {
        if (isLootableBlock()) {
            for (ItemStack item : lootTable.generateLoot()) {
                playerLoot.addLoot(item);
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        }
    }
}
