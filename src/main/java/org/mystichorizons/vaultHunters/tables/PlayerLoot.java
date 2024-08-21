package org.mystichorizons.vaultHunters.tables;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerLoot {
    private final Player player;
    private final List<ItemStack> lootedItems;

    public PlayerLoot(Player player) {
        this.player = player;
        this.lootedItems = new ArrayList<>();
    }

    public Player getPlayer() {
        return player;
    }

    public List<ItemStack> getLootedItems() {
        return lootedItems;
    }

    public void addLoot(ItemStack item) {
        lootedItems.add(item);
    }

    public boolean hasLooted(ItemStack item) {
        return lootedItems.contains(item);
    }

    public void clearLoot() {
        lootedItems.clear();
    }
}
