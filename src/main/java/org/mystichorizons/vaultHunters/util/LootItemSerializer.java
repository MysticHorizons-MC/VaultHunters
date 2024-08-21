package org.mystichorizons.vaultHunters.util;

import org.bukkit.inventory.ItemStack;
import org.mystichorizons.vaultHunters.tables.LootItem;

import java.util.HashMap;
import java.util.Map;

public class LootItemSerializer {

    public static Map<String, Object> serializeLootItem(LootItem lootItem) {
        Map<String, Object> lootItemMap = new HashMap<>();
        lootItemMap.put("item", ItemStackSerializer.serializeItemStack(lootItem.getItem()));
        lootItemMap.put("chance", lootItem.getChance());
        lootItemMap.put("minQuantity", lootItem.getMinQuantity());
        lootItemMap.put("maxQuantity", lootItem.getMaxQuantity());
        return lootItemMap;
    }

    public static LootItem deserializeLootItem(Map<String, Object> lootItemMap) {
        ItemStack item = ItemStackSerializer.deserializeItemStack((Map<String, Object>) lootItemMap.get("item"));
        double chance = (double) lootItemMap.get("chance");
        int minQuantity = (int) lootItemMap.get("minQuantity");
        int maxQuantity = (int) lootItemMap.get("maxQuantity");

        return new LootItem(item, chance, minQuantity, maxQuantity);
    }
}
