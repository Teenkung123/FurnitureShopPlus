package com.Teenkung123.furnitureShopPlus.GUI;

import com.Teenkung123.furnitureShopPlus.GUI.ConfirmGUI.ConfirmRecord;
import com.Teenkung123.furnitureShopPlus.GUI.ShopGUI.ShopGUIRecord;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GUIWrapper implements Listener {

    private final Map<Inventory, ShopGUIRecord> shopInventories = new HashMap<>();
    private final Map<Inventory, ConfirmRecord> confirmInventories = new HashMap<>();

    // Set to track inventories being closed programmatically
    private final Set<Inventory> inventoriesBeingClosed = new HashSet<>();

    public void addPluginInventory(ShopGUIRecord record) {
        shopInventories.put(record.inventory(), record);
    }

    public void addPluginInventory(ConfirmRecord record) {
        confirmInventories.put(record.inventory(), record);
    }

    /**
     * Removes the inventory from tracking maps and closes it programmatically.
     * Uses a flag to prevent recursive event triggering.
     *
     * @param inventory The inventory to remove and close.
     */
    public void removePluginInventory(Inventory inventory) {
        // If the inventory is already being closed programmatically, do not attempt to close it again
        if (inventoriesBeingClosed.contains(inventory)) {
            return;
        }

        // Add to the set to indicate it's being closed programmatically
        inventoriesBeingClosed.add(inventory);

        // Close the inventory for all viewers
        for (HumanEntity ent : inventory.getViewers()) {
            ent.closeInventory();
        }

        // Remove from tracking maps
        shopInventories.remove(inventory);
        confirmInventories.remove(inventory);
    }

    /**
     * Checks if the inventory is managed by this plugin.
     *
     * @param inventory The inventory to check.
     * @return True if it's a plugin-managed inventory, false otherwise.
     */
    public boolean isPluginInventory(Inventory inventory) {
        return shopInventories.containsKey(inventory) || confirmInventories.containsKey(inventory);
    }

    public boolean isShopInventory(Inventory inventory) {
        return shopInventories.containsKey(inventory);
    }

    public boolean isConfirmInventory(Inventory inventory) {
        return confirmInventories.containsKey(inventory);
    }

    public Collection<ShopGUIRecord> getShopInventories() {
        return shopInventories.values();
    }

    public Collection<ConfirmRecord> getConfirmInventories() {
        return confirmInventories.values();
    }

    public ShopGUIRecord getShopGUIRecord(Inventory inventory) {
        return shopInventories.getOrDefault(inventory, null);
    }

    public ConfirmRecord getConfirmRecord(Inventory inventory) {
        return confirmInventories.getOrDefault(inventory, null);
    }

    /**
     * Event handler for inventory closure.
     * Differentiates between programmatic and manual closures to prevent recursion.
     *
     * @param event The inventory close event.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (isPluginInventory(inventory)) {
            if (inventoriesBeingClosed.contains(inventory)) {
                // Programmatically closed inventory; remove from the set and do nothing else
                inventoriesBeingClosed.remove(inventory);
            } else {
                // Player manually closed the inventory; remove it from tracking maps
                shopInventories.remove(inventory);
                confirmInventories.remove(inventory);
            }
        }
    }
}
