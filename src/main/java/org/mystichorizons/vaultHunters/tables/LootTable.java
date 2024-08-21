package org.mystichorizons.vaultHunters.tables;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootTable {
    private final List<LootItem> lootItems;
    private final Random random;

    public LootTable() {
        this.lootItems = new ArrayList<>();
        this.random = new Random();
    }

    public void addLootItem(LootItem lootItem) {
        lootItems.add(lootItem);
    }

    public void removeLootItem(LootItem lootItem) {
        lootItems.remove(lootItem);
    }

    public List<ItemStack> generateLoot() {
        List<ItemStack> generatedLoot = new ArrayList<>();
        for (LootItem lootItem : lootItems) {
            if (random.nextDouble() <= lootItem.getChance()) {
                generatedLoot.add(lootItem.generateItem());
            }
        }
        return generatedLoot;
    }

    public List<LootItem> getLootItems() {
        return lootItems;
    }
}
