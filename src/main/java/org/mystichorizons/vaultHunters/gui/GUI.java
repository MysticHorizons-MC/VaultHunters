package org.mystichorizons.vaultHunters.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.LangHandler;
import org.mystichorizons.vaultHunters.handlers.VaultTiersHandler;

import java.util.Arrays;
import java.util.List;

public class GUI {

    private final VaultHunters plugin;
    private final LangHandler langHandler;

    public GUI(VaultHunters plugin) {
        this.plugin = plugin;
        this.langHandler = plugin.getLangHandler();
    }

    public void openMainMenu(Player player) {
        Inventory mainMenu = Bukkit.createInventory(null, 54, langHandler.getMessage("vault-gui"));

        // Add gray stained glass pane border
        ItemStack glassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                mainMenu.setItem(i, glassPane);
            }
        }

        // Populate vault tiers
        VaultTiersHandler.VaultTier[] vaultTiers = plugin.getVaultTiersHandler().getAllTiers();
        int slotIndex = 10; // Starting slot (second slot in the second row)
        for (VaultTiersHandler.VaultTier tier : vaultTiers) {
            ItemStack tierItem = createVaultTierItem(tier);
            mainMenu.setItem(slotIndex, tierItem);

            // Move to the next slot, skipping the borders
            slotIndex++;
            if (slotIndex % 9 == 8) slotIndex += 2;
            if (slotIndex >= 44) break; // Stop if we reach the end of the second to last row
        }

        // Add the "Create Vault" item to the middle slot on the bottom row (slot 49)
        mainMenu.setItem(49, createItem(Material.WRITABLE_BOOK, langHandler.getMessage("vault-gui-create")));

        plugin.getGUIManager().registerGUI("MainMenu", mainMenu);
        player.openInventory(mainMenu);
    }

    ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createVaultTierItem(VaultTiersHandler.VaultTier tier) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', tier.getName()));
        List<String> lore = Arrays.asList(
                ChatColor.GRAY + langHandler.formatMessage("tier-list-format.tier", "tier", tier.getName()),
                ChatColor.GRAY + "Chance: " + tier.getChance() + "%",
                ChatColor.GRAY + "Loot Items: " + tier.getNumberOfItems(),
                ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + ChatColor.ITALIC + "Left Click to Edit",
                ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + ChatColor.ITALIC + "Right Click to Delete"
        );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 49) { // "Create Vault" item
            new CreateVault(plugin).openCreateVaultTier(player);
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() == Material.CHEST) {
            String tierName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (event.getClick() == ClickType.LEFT) {
                VaultTiersHandler.VaultTier tier = plugin.getVaultTiersHandler().getTier(tierName);
                new Editor(plugin).openVaultTierEditor(player, tier);
            } else if (event.getClick() == ClickType.RIGHT) {
                plugin.getVaultTiersHandler().deleteTier(tierName);
                player.sendMessage(langHandler.formatMessage("vault-gui-deleted-vault", "tier", tierName));
                openMainMenu(player);
            }
        }
    }
}
