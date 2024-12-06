package com.Teenkung123.furnitureShopPlus.GUI.ShopGUI;

import com.Teenkung123.furnitureShopPlus.ConfigLoader;
import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import com.Teenkung123.furnitureShopPlus.Utils.Colorizer;
import com.Teenkung123.furnitureShopPlus.Utils.ItemBuilder;
import com.Teenkung123.furnitureShopPlus.Utils.ShopItem;
import de.tr7zw.nbtapi.NBT;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopGUI {

    private final FurnitureShopPlus plugin;

    public ShopGUI(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, int page) {
        Inventory inv = buildInventory(page);
        putItems(inv, page);
        player.openInventory(inv);
    }

    public Inventory buildInventory(int page) {
        ConfigLoader configLoader = plugin.getConfigLoader();
        Inventory inventory = Bukkit.createInventory(null, configLoader.getShopGUILayoutPattern().size() * 9, Colorizer.colorize(configLoader.getShopGUIName() + page));
        Map<String, ItemBuilder> layoutBuilder = configLoader.getShopGUILayoutBuilder();
        int idx = 0;
        for (String layout : configLoader.getShopGUILayoutPattern()) {
            for (String c : layout.split("")) {
                if (layoutBuilder.containsKey(c)) {
                    inventory.setItem(idx, layoutBuilder.get(c).getItemStack());
                }
                idx++;
            }
        }

        plugin.getGuiWrapper().addPluginInventory(new ShopGUIRecord(inventory, page));
        return inventory;
    }

    public void putItems(Inventory inventory, Integer page) {
        ConfigLoader configLoader = plugin.getConfigLoader();
        List<ShopItem> items = configLoader.getShopItemsList();
        List<Integer> shopAreaSlots = configLoader.getShopArea();

        int itemsPerPage = shopAreaSlots.size();
        int itemIdx = page * itemsPerPage;

        // Iterate through each slot defined in shopAreaSlots
        for (Integer slot : shopAreaSlots) {
            if (itemIdx >= items.size()) {
                break;
            }
            ShopItem shopItem = items.get(itemIdx);
            ItemStack stack = buildShopItem(shopItem);
            inventory.setItem(slot, stack);
            itemIdx++;
        }
    }

    private ItemStack buildShopItem(ShopItem shopItem) {
        if (!CustomStack.isInRegistry(shopItem.namespace())) {
            ItemStack stack = new ItemStack(Material.BARRIER);
            stack.editMeta(meta -> {
                meta.displayName(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("InvalidItemGUI"), Placeholder.unparsed("namespace", shopItem.namespace())));
            });
            return stack;
        }

        ItemStack stack = new ItemBuilder(CustomStack.getInstance(shopItem.namespace())).getItemStack();
        NBT.modify(stack, nbt -> {
            nbt.setString("furnitureShopPlus", shopItem.namespace());
        });
        String symbol = plugin.getMessageLoader().getMessage("VaultCurrencySymbol");
        if (!shopItem.currency().equalsIgnoreCase("Vault")) symbol = plugin.getMessageLoader().getMessage("PlayerPointsCurrencySymbol");
        List<Component> lores = stack.lore();
        if (lores == null) lores = new ArrayList<>();
        for (String lore : plugin.getMessageLoader().getConfig().getStringList("ItemLore")) {
            lores.add(MiniMessage.miniMessage().deserialize(lore,
                    Placeholder.unparsed("price", String.valueOf(shopItem.price())),
                    Placeholder.unparsed("symbol", symbol),
                    Placeholder.component("item_name", stack.displayName())
            ));
        }
        stack.lore(lores);

        return stack;
    }




}
