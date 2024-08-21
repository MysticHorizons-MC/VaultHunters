package org.mystichorizons.vaultHunters.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.LangHandler;
import org.mystichorizons.vaultHunters.handlers.TierItemsHandler;
import org.mystichorizons.vaultHunters.tables.LootItem;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class LootEditor extends GUIManager {

    private final LangHandler langHandler;

    public LootEditor(VaultHunters plugin) {
        super(plugin);
        this.langHandler = plugin.getLangHandler();
    }

    public void openLootEditor(Player player, String tierName) {
        Inventory lootEditor = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Edit Loot for: " + tierName);

        ItemStack glassPane = createGlassPane();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                lootEditor.setItem(i, glassPane);
            }
        }
        lootEditor.setItem(53, createSaveButton());

        List<LootItem> items = plugin.getTierItemsHandler().getLootTableItems();
        for (int i = 0; i < items.size() && i < 45; i++) {
            ItemStack item = items.get(i).getItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Chance: " + items.get(i).getChance() + "%"));
                item.setItemMeta(meta);
            }
            lootEditor.setItem(i, item);
        }

        // Register and open the GUI using inherited methods
        registerGUI("LootEditor", lootEditor);
        openGUI(player, "LootEditor");
    }

    private ItemStack createGlassPane() {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glassPane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glassPane.setItemMeta(meta);
        }
        return glassPane;
    }

    private ItemStack createSaveButton() {
        ItemStack saveButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = saveButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(langHandler.getMessage("vault-gui-save"));
            saveButton.setItemMeta(meta);
        }
        return saveButton;
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event, String tierName) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();

        if (slot < 45) {
            handleItemEdit(event, player, tierName, clickedItem, slot);
        } else if (slot == 53 && clickedItem != null && clickedItem.getType() == Material.GREEN_WOOL) {
            saveLootChanges(player, tierName, event.getInventory());
            player.closeInventory();
            player.sendMessage(langHandler.getMessage("vault-gui-save"));
        }
    }

    private void handleItemEdit(InventoryClickEvent event, Player player, String tierName, ItemStack clickedItem, int slot) {
        if (event.getClick() == ClickType.RIGHT && clickedItem != null && clickedItem.getType() != Material.AIR) {
            new ItemEditor(plugin, clickedItem, tierName).openItemEditor(player);
        } else if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            plugin.getTierItemsHandler().removeLootItem(clickedItem);
            player.sendMessage(langHandler.getMessage("vault-gui-removed-loot"));
            event.getView().setItem(slot, null);
        } else if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                plugin.getTierItemsHandler().addLootItem(cursorItem.clone(), 1.0, 1, 1);
                player.sendMessage(langHandler.getMessage("vault-gui-added-loot"));
                event.getView().setItem(slot, cursorItem.clone());
                event.getWhoClicked().setItemOnCursor(null);
            }
        }
    }

    @EventHandler
    public void handleInventoryClose(InventoryCloseEvent event, String tierName) {
        saveLootChanges((Player) event.getPlayer(), tierName, event.getInventory());
    }

    private void saveLootChanges(Player player, String tierName, Inventory inventory) {
        List<LootItem> items = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                double currentChance = plugin.getTierItemsHandler().getLootTableItems().stream()
                        .filter(lootItem -> lootItem.getItem().isSimilar(item))
                        .findFirst()
                        .map(LootItem::getChance)
                        .orElse(1.0);
                items.add(new LootItem(item, currentChance, 1, 1));
            }
        }
        plugin.getTierItemsHandler().clearLootTable();
        items.forEach(lootItem -> plugin.getTierItemsHandler().addLootItem(lootItem.getItem(), lootItem.getChance(), lootItem.getMinQuantity(), lootItem.getMaxQuantity()));
        plugin.getTierItemsHandler().saveLootTable(tierName);
        player.sendMessage(langHandler.getMessage("vault-gui-save-success"));
    }
}
