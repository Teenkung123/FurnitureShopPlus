package com.Teenkung123.furnitureShopPlus.Utils;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ItemBuilder {

    private final String type;
    private @NotNull String material;
    private String displayName;
    private List<String> lore;
    private int amount;
    private int customModelData;

    public ItemBuilder(ConfigurationSection section) {
        this.type = section.getString("Type", "Vanilla");
        this.material = section.getString("Material", "STONE");
        this.displayName = section.getString("DisplayName", null);
        this.lore = section.getStringList("Lore");
        this.amount = section.getInt("Amount", 1);
        this.customModelData = section.getInt("CustomModelData", 0);

        if (type.equalsIgnoreCase("Vanilla")) {
            if (Material.getMaterial(material) == null) {
                FurnitureShopPlus.getLog().info("Material " + material + " not found.");
            }
            return;
        }
        if (!CustomStack.isInRegistry(material)) {
            FurnitureShopPlus.getLog().info("ItemsAdder item " + material + " not found.");
        }
    }

    public ItemBuilder(String type, @NotNull String material, String displayName, List<String> lore, int amount, int customModelData) {
        this.type = type;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.amount = amount;
        this.customModelData = customModelData;
    }

    public ItemBuilder(CustomStack customStack) {

        ItemStack stack = customStack.getItemStack();
        this.type = "ItemsAdder";
        this.material = customStack.getNamespacedID();
        this.amount = stack.getAmount();
    }


    public String getType() {
        return type;
    }

    public @NotNull String getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getAmount() {
        return amount;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setMaterial(@NotNull String material) {
        this.material = material;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public void setLore(String lore) {
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }
        this.lore.add(lore);
    }

    public ItemStack getItemStack() {
        ItemStack stack;
        if (type.equalsIgnoreCase("ItemsAdder")) {
            stack = CustomStack.getInstance(material).getItemStack();
        } else {
            //noinspection DataFlowIssue
            stack = new ItemStack(Material.getMaterial(material));
        }
        stack.setAmount(amount);
        stack.editMeta(itemMeta -> {
            if (displayName != null) itemMeta.displayName(Colorizer.colorize(displayName));
            if (customModelData != 0) itemMeta.setCustomModelData(customModelData);
            if (lore != null) {
                List<Component> loreList = new ArrayList<>();
                for (String lore : this.lore) {
                    loreList.add(Colorizer.colorize((lore)));
                }
                itemMeta.lore(loreList);
            }
        });
        return stack;
    }
}
