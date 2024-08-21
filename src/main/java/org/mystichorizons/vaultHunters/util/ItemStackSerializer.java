package org.mystichorizons.vaultHunters.util;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ItemStackSerializer {

    public static Map<String, Object> serializeItemStack(ItemStack item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("type", item.getType().toString());
        itemMap.put("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            itemMap.put("meta", meta.serialize());
        }

        return itemMap;
    }

    public static ItemStack deserializeItemStack(Map<String, Object> itemMap) {
        Material type = Material.valueOf((String) itemMap.get("type"));
        int amount = (int) itemMap.get("amount");

        ItemStack item = new ItemStack(type, amount);

        if (itemMap.containsKey("meta")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metaMap = (Map<String, Object>) itemMap.get("meta");

            // Using Bukkit's ConfigurationSerialization to deserialize ItemMeta
            ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(metaMap, ItemMeta.class);
            if (meta != null) {
                item.setItemMeta(meta);
            }
        }

        return item;
    }
}
