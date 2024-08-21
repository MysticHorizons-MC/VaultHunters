package org.mystichorizons.vaultHunters.tables;

import org.bukkit.inventory.ItemStack;

public class LootItem {
    private final ItemStack item;
    private final double chance;
    private final int minQuantity;
    private final int maxQuantity;

    public LootItem(ItemStack item, double chance, int minQuantity, int maxQuantity) {
        this.item = item;
        this.chance = chance;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getChance() {
        return chance;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public ItemStack generateItem() {
        int quantity = (minQuantity == maxQuantity) ? minQuantity : minQuantity + (int)(Math.random() * (maxQuantity - minQuantity + 1));
        ItemStack generatedItem = item.clone();
        generatedItem.setAmount(quantity);
        return generatedItem;
    }
}
