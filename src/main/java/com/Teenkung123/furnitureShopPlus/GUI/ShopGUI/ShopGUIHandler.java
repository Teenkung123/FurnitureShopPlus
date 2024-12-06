package com.Teenkung123.furnitureShopPlus.GUI.ShopGUI;

import com.Teenkung123.furnitureShopPlus.ConfigLoader;
import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import com.Teenkung123.furnitureShopPlus.Utils.Colorizer;
import com.Teenkung123.furnitureShopPlus.Utils.ShopItem;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopGUIHandler implements Listener {

    private final FurnitureShopPlus plugin;

    public ShopGUIHandler(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.getGuiWrapper().isShopInventory(event.getClickedInventory())) {
            event.setCancelled(true);
            Inventory clickedInventory = event.getClickedInventory();
            ShopGUIRecord record = plugin.getGuiWrapper().getShopGUIRecord(clickedInventory);
            ConfigLoader configLoader = plugin.getConfigLoader();
            Player player = (Player) event.getWhoClicked();
            if (record == null) return;

            if (configLoader.getNextPage().contains(event.getSlot())) {
                // Next Page Logic
                if (Math.floor((double) configLoader.getShopItemsList().size() / configLoader.getShopArea().size()) == record.page() + 1) return;
                plugin.getShopGUI().openGUI(player, (int) Math.min(record.page() + 1, Math.floor((double) configLoader.getShopItemsList().size() / configLoader.getShopArea().size())));
            } else if (configLoader.getPrevPage().contains(event.getSlot())) {
                // Prev Page Login
                if (record.page() == 0) return;
                plugin.getShopGUI().openGUI(player, Math.max(0, record.page() - 1));
            } else if (configLoader.getShopArea().contains(event.getSlot())) {
                // Shop Item Logic
                int itemIdx = record.page() * configLoader.getShopArea().size() + configLoader.getShopArea().indexOf(event.getSlot());
                if (itemIdx >= configLoader.getShopItemsList().size()) return;
                ShopItem shopItem = configLoader.getShopItemsList().get(itemIdx);
                CustomStack customStack = CustomStack.getInstance(shopItem.namespace());
                if (customStack == null) return;
                ItemStack itemStack = customStack.getItemStack();
                if (!configLoader.isInvalidItem(shopItem.namespace())) {
                    player.sendMessage(Colorizer.colorize(plugin.getMessageLoader().getMessage("InvalidItem")));
                    return;
                }

                if (event.getClick() == ClickType.LEFT) {
                    plugin.getConfirmGUI().openConfirmGUI(player, shopItem.namespace());
                } else if (event.getClick() == ClickType.RIGHT) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("Preview"), Placeholder.component("item", itemStack.displayName())));
                    plugin.getPreviewTimer().preview(shopItem.namespace(), true);
                    player.closeInventory();
                }
            }
        }
    }
}
