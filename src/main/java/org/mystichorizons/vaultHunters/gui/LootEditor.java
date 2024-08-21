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

        TierItemsHandler.TierItem[] items = plugin.getTierItemsHandler().getTierItems(tierName).toArray(new TierItemsHandler.TierItem[0]);
        for (int i = 0; i < items.length && i < 45; i++) {
            ItemStack item = items[i].getItemStack();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Chance: " + items[i].getChance() + "%"));
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
            player.closeInventory();
            player.sendMessage(langHandler.getMessage("vault-gui-save"));
        }
    }

    private void handleItemEdit(InventoryClickEvent event, Player player, String tierName, ItemStack clickedItem, int slot) {
        if (event.getClick() == ClickType.RIGHT && clickedItem != null && clickedItem.getType() != Material.AIR) {
            new ItemEditor(plugin, clickedItem, tierName).openItemEditor(player);
        } else if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            plugin.getTierItemsHandler().removeItemFromTier(tierName, clickedItem);
            player.sendMessage(langHandler.getMessage("vault-gui-removed-loot"));
            event.getView().setItem(slot, null);
        } else if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                plugin.getTierItemsHandler().addItemToTier(tierName, cursorItem.clone(), 1.0);
                player.sendMessage(langHandler.getMessage("vault-gui-added-loot"));
                event.getView().setItem(slot, cursorItem.clone());
                event.getWhoClicked().setItemOnCursor(null);
            }
        }
    }

    public void handleInventoryClose(InventoryCloseEvent event, String tierName) {
        InventoryView view = event.getView();
        saveInventoryState(view, tierName);
    }

    private void saveInventoryState(InventoryView view, String tierName) {
        List<TierItemsHandler.TierItem> items = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack item = view.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                double currentChance = plugin.getTierItemsHandler().getTierItems(tierName).stream()
                        .filter(tierItem -> tierItem.getItemStack().isSimilar(item))
                        .findFirst()
                        .map(TierItemsHandler.TierItem::getChance)
                        .orElse(1.0);
                items.add(new TierItemsHandler.TierItem(item, currentChance));
            }
        }
        plugin.getTierItemsHandler().saveTierItems(tierName, items);
    }
}
