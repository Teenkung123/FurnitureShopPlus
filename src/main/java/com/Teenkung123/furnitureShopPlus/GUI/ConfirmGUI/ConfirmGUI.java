package com.Teenkung123.furnitureShopPlus.GUI.ConfirmGUI;

import com.Teenkung123.furnitureShopPlus.ConfigLoader;
import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import com.Teenkung123.furnitureShopPlus.Utils.Colorizer;
import com.Teenkung123.furnitureShopPlus.Utils.ItemBuilder;
import com.Teenkung123.furnitureShopPlus.Utils.ShopItem;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class ConfirmGUI {

    private final FurnitureShopPlus plugin;

    public ConfirmGUI(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    public void openConfirmGUI(Player player , String namespace) {
        ConfirmRecord record = buildGUI(namespace);
        player.openInventory(record.getInventory());
    }

    public ConfirmRecord buildGUI(String namespace) {
        ShopItem shopItem = plugin.getConfigLoader().getShopItemByName(namespace);
        ConfigLoader configLoader = plugin.getConfigLoader();
        Inventory inventory = Bukkit.createInventory(null, configLoader.getConfirmGUILayoutPattern().size()*9, Colorizer.colorize(configLoader.getConfirmGUIName()));
        Map<String, ItemBuilder> layoutBuilder = configLoader.getConfirmGUILayoutBuilder();
        int idx = 0;
        for (String pattern : configLoader.getConfirmGUILayoutPattern()) {
            for (String c : pattern.split("")) {
                if (layoutBuilder.containsKey(c)) {
                    inventory.setItem(idx, layoutBuilder.get(c).getItemStack());
                }
                idx++;
            }
        }

        CustomStack stack = CustomStack.getInstance(shopItem.namespace());
        if (stack == null) {
            return new ConfirmRecord(inventory, shopItem);
        }
        for (Integer i : configLoader.getPreviewSlots()) {
            inventory.setItem(i, stack.getItemStack());
        }
        ConfirmRecord record = new ConfirmRecord(inventory, shopItem);
        plugin.getGuiWrapper().addPluginInventory(record);
        return record;
    }

}
