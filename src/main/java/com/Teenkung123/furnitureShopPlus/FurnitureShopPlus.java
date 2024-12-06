package com.Teenkung123.furnitureShopPlus;

import com.Teenkung123.furnitureShopPlus.Commands.MainCommand;
import com.Teenkung123.furnitureShopPlus.GUI.ConfirmGUI.ConfirmGUI;
import com.Teenkung123.furnitureShopPlus.GUI.ConfirmGUI.ConfirmGUIHandler;
import com.Teenkung123.furnitureShopPlus.GUI.ConfirmGUI.ConfirmRecord;
import com.Teenkung123.furnitureShopPlus.GUI.GUIWrapper;
import com.Teenkung123.furnitureShopPlus.GUI.ShopGUI.ShopGUI;
import com.Teenkung123.furnitureShopPlus.GUI.ShopGUI.ShopGUIHandler;
import com.Teenkung123.furnitureShopPlus.GUI.ShopGUI.ShopGUIRecord;
import com.Teenkung123.furnitureShopPlus.Preview.PreviewTimer;
import com.Teenkung123.furnitureShopPlus.Preview.WorldGuardHandler;
import com.Teenkung123.furnitureShopPlus.Utils.RegionChecker;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class FurnitureShopPlus extends JavaPlugin {

    private static Logger log;
    private ConfigLoader configLoader;
    private MessageLoader messageLoader;
    private GUIWrapper guiWrapper;
    private ShopGUI shopGUI;
    private ConfirmGUI confirmGUI;
    private PreviewTimer previewTimer;
    private RegionChecker regionChecker;

    private Economy economy;
    private PlayerPointsAPI ppAPI;

    @Override
    public void onEnable() {
        log = getLogger();

        configLoader = new ConfigLoader(this);
        messageLoader = new MessageLoader(this);
        guiWrapper = new GUIWrapper();
        shopGUI = new ShopGUI(this);
        confirmGUI = new ConfirmGUI(this);
        previewTimer = new PreviewTimer(this);
        regionChecker = new RegionChecker(this);

        configLoader.loadConfig();
        messageLoader.loadMessages();

        Bukkit.getPluginManager().registerEvents(guiWrapper, this);
        Bukkit.getPluginManager().registerEvents(new ShopGUIHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new ConfirmGUIHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldGuardHandler(this), this);

        //noinspection DataFlowIssue
        getCommand("furnitureshopplus").setExecutor(new MainCommand(this));

        setupEconomy();

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.ppAPI = PlayerPoints.getInstance().getAPI();
        }

        previewTimer.start();
    }

    @Override
    public void onDisable() {
        closeAllInventories();
        previewTimer.stop();
        previewTimer.removeFurniture();
    }

    public static Logger getLog() {
        return log;
    }

    public void reload() {
        closeAllInventories();
        previewTimer.stop();
        previewTimer.removeFurniture();

        configLoader = new ConfigLoader(this);
        configLoader.loadConfig();
        messageLoader.loadMessages();
        previewTimer.start();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found, Disabling Plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("Economy provider not found, Disabling Plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public MessageLoader getMessageLoader() {
        return messageLoader;
    }

    public GUIWrapper getGuiWrapper() {
        return guiWrapper;
    }

    public ShopGUI getShopGUI() {
        return shopGUI;
    }

    public ConfirmGUI getConfirmGUI() {
        return confirmGUI;
    }

    public PreviewTimer getPreviewTimer() {
        return previewTimer;
    }

    public Economy getEconomy() {
        return economy;
    }

    public PlayerPointsAPI getPlayerPointsAPI() {
        return ppAPI;
    }

    public RegionChecker getRegionChecker() {
        return regionChecker;
    }

    private void closeAllInventories() {
        getLogger().info("Closing all " + (guiWrapper.getShopInventories().size() + guiWrapper.getConfirmInventories().size()) + " inventories...");

        for (ShopGUIRecord inv : guiWrapper.getShopInventories()) {
            guiWrapper.removePluginInventory(inv.inventory());
        }

        for (ConfirmRecord inv : guiWrapper.getConfirmInventories()) {
            guiWrapper.removePluginInventory(inv.inventory());
        }
    }
}
