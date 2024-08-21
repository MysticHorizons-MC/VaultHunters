package org.mystichorizons.vaultHunters.util;

import org.mystichorizons.vaultHunters.tables.LootItem;
import org.mystichorizons.vaultHunters.tables.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LootTableSerializer {

    public static List<Map<String, Object>> serializeLootTable(LootTable lootTable) {
        List<Map<String, Object>> serializedLootItems = new ArrayList<>();
        for (LootItem lootItem : lootTable.getLootItems()) {
            serializedLootItems.add(LootItemSerializer.serializeLootItem(lootItem));
        }
        return serializedLootItems;
    }

    public static LootTable deserializeLootTable(List<Map<String, Object>> serializedLootItems) {
        LootTable lootTable = new LootTable();
        for (Map<String, Object> lootItemMap : serializedLootItems) {
            LootItem lootItem = LootItemSerializer.deserializeLootItem(lootItemMap);
            lootTable.addLootItem(lootItem);
        }
        return lootTable;
    }
}
