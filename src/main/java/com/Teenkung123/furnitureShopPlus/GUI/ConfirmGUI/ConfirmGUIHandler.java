package com.Teenkung123.furnitureShopPlus.GUI.ConfirmGUI;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import com.Teenkung123.furnitureShopPlus.Utils.Colorizer;
import com.Teenkung123.furnitureShopPlus.Utils.ShopItem;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ConfirmGUIHandler implements Listener {

    private final FurnitureShopPlus plugin;

    public ConfirmGUIHandler(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.getGuiWrapper().isConfirmInventory(event.getClickedInventory())) {
            event.setCancelled(true);

            ConfirmRecord record = plugin.getGuiWrapper().getConfirmRecord(event.getClickedInventory());
            if (record == null) return;

            Player player = (Player) event.getWhoClicked();
            CustomStack stack = CustomStack.getInstance(record.getShopItem().namespace());
            if (stack == null) {
                returnToShop(player, record.getShopItem());
                return;
            }

            if (plugin.getConfigLoader().getAdd1Slots().contains(event.getSlot())) {
                updateAmount(record, stack, 1);
            } else if (plugin.getConfigLoader().getAdd8Slots().contains(event.getSlot())) {
                updateAmount(record, stack, 8);
            } else if (plugin.getConfigLoader().getAdd16Slots().contains(event.getSlot())) {
                updateAmount(record, stack, 16);
            } else if (plugin.getConfigLoader().getRemove1Slots().contains(event.getSlot())) {
                updateAmount(record, stack, -1);
            } else if (plugin.getConfigLoader().getRemove8Slots().contains(event.getSlot())) {
                updateAmount(record, stack, -8);
            } else if (plugin.getConfigLoader().getRemove16Slots().contains(event.getSlot())) {
                updateAmount(record, stack, -16);
            } else if (plugin.getConfigLoader().getSet1Slots().contains(event.getSlot())) {
                record.setAmount(1);
                refreshPreview(record, stack);
            } else if (plugin.getConfigLoader().getSet64Slots().contains(event.getSlot())) {
                record.setAmount(64);
                refreshPreview(record, stack);
            } else if (plugin.getConfigLoader().getAcceptSlots().contains(event.getSlot())) {
                buyItem(player, record.getShopItem(), stack.getItemStack(), record.getAmount());
            } else if (plugin.getConfigLoader().getDenySlots().contains(event.getSlot())) {
                returnToShop(player, record.getShopItem());
            }
        }
    }

    private void returnToShop(Player player, ShopItem shopItem) {
        int idx = plugin.getConfigLoader().getShopItemIndex(shopItem);
        int page = Double.valueOf(Math.floor((double) idx / plugin.getConfigLoader().getShopArea().size())).intValue();
        plugin.getShopGUI().openGUI(player, page);
    }

    private void buyItem(Player player, ShopItem shopItem, ItemStack stack, int amount) {
        double price = shopItem.price() * amount;
        if (shopItem.currency().equalsIgnoreCase("Vault")) {
            //Vault Econoy
            Economy economy = plugin.getEconomy();
            if (!economy.has(player, price)) {
                player.sendMessage(Colorizer.colorize(plugin.getMessageLoader().getMessage("NotEnoughMoney")));
                return;
            }
            economy.withdrawPlayer(player, price);
            stack.setAmount(amount);
            player.getInventory().addItem(stack);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageLoader().getMessage("Success"),
                    Placeholder.unparsed("price", String.valueOf(price)),
                    Placeholder.unparsed("symbol", plugin.getMessageLoader().getMessage("VaultCurrencySymbol")),
                    Placeholder.component("item", stack.displayName()),
                    Placeholder.unparsed("amount", String.valueOf(amount))
            ));
            player.closeInventory();
        } else if (plugin.getPlayerPointsAPI() != null){
            PlayerPointsAPI ppAPI = plugin.getPlayerPointsAPI();
            if (ppAPI.look(player.getUniqueId()) < price) {
                player.sendMessage(Colorizer.colorize(plugin.getMessageLoader().getMessage("NotEnoughPlayerPoints")));
                return;
            }
            ppAPI.take(player.getUniqueId(), (int) price);
            stack.setAmount(amount);
            player.getInventory().addItem(stack);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageLoader().getMessage("Success"),
                    Placeholder.unparsed("price", String.valueOf(price)),
                    Placeholder.unparsed("symbol", plugin.getMessageLoader().getMessage("PlayerPointsCurrencySymbol")),
                    Placeholder.component("item", stack.displayName()),
                    Placeholder.unparsed("amount", String.valueOf(amount))
            ));
            player.closeInventory();
        } else {
            player.sendMessage(Colorizer.colorize(plugin.getMessageLoader().getMessage("NoEconomyFound")));
        }
    }

    private void updateAmount(ConfirmRecord record, CustomStack base, int delta) {
        int newAmount = Math.min(64, Math.max(1, record.getAmount() + delta));
        record.setAmount(newAmount);
        refreshPreview(record, base);
    }

    private void refreshPreview(ConfirmRecord record, CustomStack base) {
        ItemStack item = base.getItemStack();
        item.setAmount(record.getAmount());
        for (Integer slot : plugin.getConfigLoader().getPreviewSlots()) {
            record.getInventory().setItem(slot, item);
        }
    }
}
