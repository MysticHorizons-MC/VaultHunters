package org.mystichorizons.vaultHunters.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.PlayerDataManager;
import org.mystichorizons.vaultHunters.handlers.LangHandler;

public class BypassListener implements Listener {

    private final VaultHunters plugin;
    private final LangHandler langHandler;

    public BypassListener(VaultHunters plugin) {
        this.plugin = plugin;
        this.langHandler = plugin.getLangHandler();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the player is bypassing
        PlayerDataManager.PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        boolean isBypassing = playerData.isBypassing();

        if (isBypassing) {
            player.sendMessage(langHandler.getMessage("vault-bypass"));
        }
    }
}
