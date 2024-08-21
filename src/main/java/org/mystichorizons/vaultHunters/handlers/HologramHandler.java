package org.mystichorizons.vaultHunters.handlers;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class HologramHandler {

    private final JavaPlugin plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private boolean useDecentHolograms = false;
    private final Map<Location, Hologram> decentHologramsMap = new HashMap<>();
    private final Map<Location, ArmorStand> internalHologramsMap = new HashMap<>();

    public HologramHandler(JavaPlugin plugin, LangHandler langHandler, ConfigHandler configHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.configHandler = configHandler;
        detectHologramPlugins();
    }

    private void detectHologramPlugins() {
        Plugin decentHologramsPlugin = Bukkit.getPluginManager().getPlugin("DecentHolograms");

        if (decentHologramsPlugin != null && decentHologramsPlugin.isEnabled()) {
            useDecentHolograms = true;
            plugin.getLogger().info("DecentHolograms detected and will be used for holograms.");
        } else {
            plugin.getLogger().info("No supported hologram plugins detected. Falling back to internal hologram methods.");
        }
    }

    public void createOrUpdateHologram(Location location, String tierName, String cooldownTime) {
        if (useDecentHolograms) {
            createOrUpdateDecentHologram(location, tierName, cooldownTime);
        } else {
            createOrUpdateInternalHologram(location, tierName, cooldownTime);
        }
    }

    private void createOrUpdateDecentHologram(Location location, String tierName, String cooldownTime) {
        String sanitizedLocation = sanitizeLocation(location);
        String hologramName = "vault_hologram_" + sanitizedLocation;

        Hologram hologram = DHAPI.getHologram(hologramName);
        if (hologram == null) {
            hologram = DHAPI.createHologram(hologramName, location.add(0, configHandler.getAboveVault(), 0));
        }

        DHAPI.updateHologram(String.valueOf(hologram));
        String hoverName = configHandler.getHoverName().replace("%tier_name%", tierName);
        DHAPI.addHologramLine(hologram, hoverName);

        if (configHandler.isShowCooldown()) {
            String cooldownMessage = langHandler.formatMessage("vault-hologram.cooldown-format", "time", cooldownTime);
            DHAPI.addHologramLine(hologram, cooldownMessage);
        }

        hologram.showAll();
        decentHologramsMap.put(location, hologram);
    }

    private void createOrUpdateInternalHologram(Location location, String tierName, String cooldownTime) {
        ArmorStand hologram = internalHologramsMap.get(location);
        if (hologram == null) {
            hologram = location.getWorld().spawn(location.add(0, configHandler.getAboveVault(), 0), ArmorStand.class);
            hologram.setGravity(false);
            hologram.setVisible(false);
            internalHologramsMap.put(location, hologram);
        }

        String hoverName = configHandler.getHoverName().replace("%tier_name%", tierName);
        hologram.setCustomName(hoverName);
        hologram.setCustomNameVisible(true);

        if (configHandler.isShowCooldown()) {
            ArmorStand cooldownHologram = location.getWorld().spawn(location.add(0, -0.25, 0), ArmorStand.class);
            cooldownHologram.setCustomName(langHandler.formatMessage("vault-hologram.cooldown-format", "time", cooldownTime));
            cooldownHologram.setCustomNameVisible(true);
            cooldownHologram.setGravity(false);
            cooldownHologram.setVisible(false);
            internalHologramsMap.put(location, cooldownHologram);
        }
    }

    private String sanitizeLocation(Location location) {
        return String.format("%s_%d_%d_%d",
                location.getWorld().getName().replaceAll("[^a-zA-Z0-9_-]", ""),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    public void removeHologram(Location location) {
        if (useDecentHolograms) {
            removeDecentHologram(location);
        } else {
            removeInternalHologram(location);
        }
    }

    private void removeDecentHologram(Location location) {
        Hologram hologram = decentHologramsMap.remove(location);
        if (hologram != null) {
            hologram.delete();
        }
    }

    private void removeInternalHologram(Location location) {
        ArmorStand hologram = internalHologramsMap.remove(location);
        if (hologram != null) {
            hologram.remove();
        }
    }

    public boolean isHologramEnabled() {
        return useDecentHolograms || configHandler.isHologramEnabled();
    }
}
