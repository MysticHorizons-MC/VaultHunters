package org.mystichorizons.vaultHunters.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.PlayerDataManager;
import org.mystichorizons.vaultHunters.handlers.ConfigHandler;
import org.mystichorizons.vaultHunters.handlers.LangHandler;
import org.mystichorizons.vaultHunters.gui.GUI;

public class VaultHuntersCommand implements CommandExecutor {

    private final VaultHunters plugin;
    private final ConfigHandler configHandler;
    private final LangHandler langHandler;

    public VaultHuntersCommand(VaultHunters plugin) {
        this.plugin = plugin;
        this.configHandler = plugin.getConfigHandler();
        this.langHandler = plugin.getLangHandler();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                showInfo(sender);
                break;

            case "reload":
                reloadConfigs(sender);
                break;

            case "gui":
                openGUI(sender);
                break;

            case "bypass":
                toggleBypass(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /vaulthunters for a list of commands.");
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "VaultHunters Commands:");
        if (sender.hasPermission("vaulthunters.info")) {
            sender.sendMessage(ChatColor.YELLOW + "/vaulthunters info" + ChatColor.WHITE + " - Shows the plugin's information.");
        }
        if (sender.hasPermission("vaulthunters.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "/vaulthunters reload" + ChatColor.WHITE + " - Reloads the Config, Items Folder, tiers.yml, and lang folder.");
        }
        if (sender.hasPermission("vaulthunters.gui")) {
            sender.sendMessage(ChatColor.YELLOW + "/vaulthunters gui" + ChatColor.WHITE + " - Opens the GUI.");
        }
        if (sender.hasPermission("vaulthunters.bypass")) {
            sender.sendMessage(ChatColor.YELLOW + "/vaulthunters bypass" + ChatColor.WHITE + " - Toggle cooldown bypass.");
        }
    }

    private void showInfo(CommandSender sender) {
        if (sender.hasPermission("vaulthunters.info")) {
            sender.sendMessage(ChatColor.GOLD + "VaultHunters Plugin v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GOLD + "Authors: " + plugin.getDescription().getAuthors());
        } else {
            sender.sendMessage(langHandler.getMessage("vault-no-permission"));
        }
    }

    private void reloadConfigs(CommandSender sender) {
        if (sender.hasPermission("vaulthunters.reload")) {
            configHandler.reloadConfig();
            plugin.getTierItemsHandler().loadTieredItems();
            plugin.getVaultTiersHandler().loadVaultTiers();
            langHandler.reloadLang();
            sender.sendMessage(langHandler.getMessage("vault-reload"));
        } else {
            sender.sendMessage(langHandler.getMessage("vault-no-permission"));
        }
    }

    private void openGUI(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return;
        }

        Player player = (Player) sender;
        if (player.hasPermission("vaulthunters.gui")) {
            new GUI(plugin).openMainMenu(player);
        } else {
            sender.sendMessage(langHandler.getMessage("vault-no-permission"));
        }
    }

    private void toggleBypass(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return;
        }

        Player player = (Player) sender;
        if (player.hasPermission("vaulthunters.bypass")) {
            PlayerDataManager.PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

            // Toggle the bypass status
            boolean bypass = !playerData.isBypassing();
            playerData.setBypassing(bypass);

            // Send a message to the player with the current bypass status
            String status = bypass ? langHandler.getMessage("other-format.enabled") : langHandler.getMessage("other-format.disabled");
            player.sendMessage(langHandler.formatMessage("vault-bypass-status", "bypass_status", status));
        } else {
            sender.sendMessage(langHandler.getMessage("vault-no-permission"));
        }
    }
}
