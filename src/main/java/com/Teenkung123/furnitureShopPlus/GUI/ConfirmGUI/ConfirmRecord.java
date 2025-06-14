package com.Teenkung123.furnitureShopPlus.GUI.ConfirmGUI;

import com.Teenkung123.furnitureShopPlus.Utils.ShopItem;
import org.bukkit.inventory.Inventory;

public class ConfirmRecord {
    private final Inventory inventory;
    private final ShopItem shopItem;
    private int amount = 1;

    public ConfirmRecord(Inventory inventory, ShopItem shopItem) {
        this.inventory = inventory;
        this.shopItem = shopItem;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ShopItem getShopItem() {
        return shopItem;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
