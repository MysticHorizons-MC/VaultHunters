package org.mystichorizons.vaultHunters;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mystichorizons.vaultHunters.commands.VaultHuntersCommand;
import org.mystichorizons.vaultHunters.handlers.*;
import org.mystichorizons.vaultHunters.injector.VaultLootInjector;
import org.mystichorizons.vaultHunters.listeners.BypassListener;
import org.mystichorizons.vaultHunters.listeners.VaultListener;
import org.mystichorizons.vaultHunters.tasks.VaultTask;
import org.mystichorizons.vaultHunters.gui.GUIManager;

import java.io.File;

public final class VaultHunters extends JavaPlugin {

    private ConfigHandler configHandler;
    private LangHandler langHandler;
    private VaultTiersHandler vaultTiersHandler;
    private TierItemsHandler tierItemsHandler;
    private HologramHandler hologramHandler;
    private PlayerDataManager playerDataManager;
    private VaultTask vaultTask;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        // Check if the NBT-API is initialized properly
        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Display the banner on load
        displayBanner();

        // Ensure all necessary files are created
        saveDefaultFiles();

        // Initialize configuration and language handlers
        this.configHandler = new ConfigHandler(this);
        this.langHandler = new LangHandler(this, configHandler);

        // Initialize other handlers
        this.vaultTiersHandler = new VaultTiersHandler(this);
        this.tierItemsHandler = new TierItemsHandler(this);
        this.hologramHandler = new HologramHandler(this, langHandler);
        this.playerDataManager = new PlayerDataManager();
        this.guiManager = new GUIManager(this);

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        // Start the VaultTask
        startVaultTask();

        // Log that the plugin is enabled
        sendConsoleMessage(ChatColor.GREEN, "[VaultHunters] Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Perform any necessary cleanup
        sendConsoleMessage(ChatColor.RED, "[VaultHunters] Plugin disabled!");
    }

    // Register commands
    private void registerCommands() {
        if (getCommand("vaulthunters") == null) {
            getLogger().severe("Command 'vaulthunters' not found in plugin.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("vaulthunters").setExecutor(new VaultHuntersCommand(this));
    }

    // Register event listeners
    private void registerListeners() {
        new VaultListener(this);
        new BypassListener(this);
        new GUIManager(this);
    }

    // Start the VaultTask
    private void startVaultTask() {
        HologramHandler hologramHandler = new HologramHandler(this, langHandler);  // Initialize the hologram handler
        ParticleHandler particleHandler = new ParticleHandler(configHandler);  // Initialize the particle handler
        VaultLootInjector vaultLootInjector = new VaultLootInjector(this, vaultTiersHandler, tierItemsHandler, hologramHandler);  // Initialize the loot injector

        // Initialize and start the VaultTask
        this.vaultTask = new VaultTask(this, particleHandler, vaultLootInjector, hologramHandler);
        this.vaultTask.startMonitoring();
    }

    // Utility method to send colored messages to the console
    private void sendConsoleMessage(ChatColor color, String message) {
        getServer().getConsoleSender().sendMessage(color + message);
    }

    // Display a banner in the console on plugin load
    private void displayBanner() {
        String pluginName = ChatColor.GREEN + "VaultHunters";
        String version = ChatColor.GRAY + "v" + getDescription().getVersion();  // Replace the placeholder with actual version
        String author = ChatColor.WHITE + "Author: " + ChatColor.AQUA + "Alphine";

        String spacer = " ".repeat(30); // Adjust the number to center the text

        sendConsoleMessage(ChatColor.DARK_GRAY, spacer);
        sendConsoleMessage(ChatColor.DARK_GRAY, spacer);
        sendConsoleMessage(ChatColor.DARK_GRAY, spacer + centerText(pluginName, 49));
        sendConsoleMessage(ChatColor.DARK_GRAY, spacer + centerText(version, 49));
        sendConsoleMessage(ChatColor.DARK_GRAY, spacer);
        sendConsoleMessage(ChatColor.DARK_GRAY, spacer + centerText(author, 49));
        sendConsoleMessage(ChatColor.DARK_GRAY, spacer);
    }

    // Utility method to center text within a given width
    private String centerText(String text, int width) {
        int padding = (width - ChatColor.stripColor(text).length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    // Ensure default files are created if they do not exist
    private void saveDefaultFiles() {
        // Ensure the config.yml is created
        saveDefaultConfig();

        // Ensure the lang files are created
        saveResourceIfNotExists("lang/en_US.yml");

        // Ensure other necessary files are created
        saveResourceIfNotExists("tiers.yml");
        saveResourceIfNotExists("items/Common.yml");
        saveResourceIfNotExists("items/Uncommon.yml");
        saveResourceIfNotExists("items/Rare.yml");
        saveResourceIfNotExists("items/Epic.yml");
        saveResourceIfNotExists("items/Legendary.yml");
        saveResourceIfNotExists("items/Mythic.yml");
        saveResourceIfNotExists("items/Godly.yml");
    }

    // Utility method to save a resource file if it does not exist
    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    // Getters for handlers, data manager, and GUI manager
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public LangHandler getLangHandler() {
        return langHandler;
    }

    public VaultTiersHandler getVaultTiersHandler() {
        return vaultTiersHandler;
    }

    public TierItemsHandler getTierItemsHandler() {
        return tierItemsHandler;
    }

    public HologramHandler getHologramHandler() {
        return hologramHandler;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }
}
