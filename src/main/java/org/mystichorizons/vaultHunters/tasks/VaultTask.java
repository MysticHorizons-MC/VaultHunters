package org.mystichorizons.vaultHunters.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.HologramHandler;
import org.mystichorizons.vaultHunters.handlers.ParticleHandler;
import org.mystichorizons.vaultHunters.injector.VaultLootInjector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VaultTask {

    private final VaultHunters plugin;
    private final ParticleHandler particleHandler;
    private final VaultLootInjector vaultLootInjector;
    private final HologramHandler hologramHandler;
    private final int rangeSquared;

    public VaultTask(VaultHunters plugin, ParticleHandler particleHandler, VaultLootInjector vaultLootInjector, HologramHandler hologramHandler) {
        this.plugin = plugin;
        this.particleHandler = particleHandler;
        this.vaultLootInjector = vaultLootInjector;
        this.hologramHandler = hologramHandler;
        this.rangeSquared = plugin.getServer().getViewDistance() * plugin.getServer().getViewDistance() * 256; // Blocks squared
    }

    public void startMonitoring() {
        plugin.getLogger().info("Starting VaultTask...");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkAndUpdateVaultsForPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Runs every second (20 ticks) on the main thread
    }

    private void checkAndUpdateVaultsForPlayer(Player player) {
        Location playerLocation = player.getLocation();

        for (Block block : getNearbyVaultBlocks(playerLocation)) {
            if (block != null && block.getType() == Material.VAULT) {
                Location blockLocation = block.getLocation();

                // Inject loot using the VaultLootInjector on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    boolean lootInjected = vaultLootInjector.injectRandomTierLoot(block);

                    if (lootInjected) {
                        particleHandler.playParticles(blockLocation); // Trigger particles
                        hologramHandler.createOrUpdateHologram(blockLocation, "TierName", "CooldownTime"); // Update hologram
                    }
                });
            }
        }
    }

    private Set<Block> getNearbyVaultBlocks(Location location) {
        Set<Block> vaultBlocks = new HashSet<>();
        int radius = (int) Math.sqrt(rangeSquared);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
                    if (block.getType() == Material.VAULT) {
                        vaultBlocks.add(block);
                    }
                }
            }
        }
        return vaultBlocks;
    }

    public void handleBlockBreak(Block block) {
        if (block.getType() == Material.VAULT) {
            // Remove hologram when vault block is broken
            hologramHandler.removeHologram(block.getLocation());
        }
    }
}
