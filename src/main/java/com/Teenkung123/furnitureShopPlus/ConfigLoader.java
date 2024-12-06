package com.Teenkung123.furnitureShopPlus;

import com.Teenkung123.furnitureShopPlus.Utils.ItemBuilder;
import com.Teenkung123.furnitureShopPlus.Utils.ShopItem;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigLoader {

    private final FurnitureShopPlus plugin;
    private final FileConfiguration config;

    // ShopGUI fields
    private final List<String> shopLayoutPattern = new ArrayList<>();
    private final Map<String, ItemBuilder> shopLayoutBuilder = new HashMap<>();
    private String shopGUIName = "Default Shop GUI Name";

    // ConfirmGUI fields
    private final List<String> confirmLayoutPattern = new ArrayList<>();
    private final Map<String, ItemBuilder> confirmLayoutBuilder = new HashMap<>();
    private String confirmGUIName = "Default Confirm GUI Name";

    // Shop related fields
    private final List<Integer> shopArea = new ArrayList<>();
    private final List<Integer> nextPage = new ArrayList<>();
    private final List<Integer> prevPage = new ArrayList<>();
    private Location displayLocation;
    private String worldguardRegion = "FurnitureShop";
    private Integer displayInterval = 600;
    private Integer PauseDisplayRange = 5;

    // Confirm related fields
    private final List<Integer> denySlots = new ArrayList<>();
    private final List<Integer> acceptSlots = new ArrayList<>();
    private final List<Integer> previewSlots = new ArrayList<>();

    // Shop items
    private final List<String> invalidItems = new ArrayList<>();
    private final Map<String, ShopItem> shopItems = new HashMap<>();

    public ConfigLoader(FurnitureShopPlus plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();

        loadShopGUI();
        loadConfirmGUI();
        loadShopSection();
        loadConfirmSection();
        loadShopItems();
    }

    /**
     * Loads ShopGUI settings: name, layout pattern, and layout items.
     */
    private void loadShopGUI() {
        ConfigurationSection shopGUISection = config.getConfigurationSection("ShopGUI");
        if (shopGUISection == null) {
            plugin.getLogger().warning("ShopGUI section is missing in config.");
            return;
        }

        // GUI Name
        this.shopGUIName = shopGUISection.getString("Name", "Default Shop GUI Name");

        // Layout Pattern
        List<String> patterns = shopGUISection.getStringList("LayoutPattern");
        if (patterns.isEmpty()) {
            plugin.getLogger().warning("ShopGUI.LayoutPattern is empty or missing in config.");
        }
        shopLayoutPattern.addAll(patterns);

        // Layout Items
        ConfigurationSection layoutSection = shopGUISection.getConfigurationSection("Layout");
        if (layoutSection != null) {
            for (String key : layoutSection.getKeys(false)) {
                ConfigurationSection itemSection = layoutSection.getConfigurationSection(key);
                if (itemSection != null) {
                    shopLayoutBuilder.put(key, new ItemBuilder(itemSection));
                } else {
                    plugin.getLogger().warning("Item section for key '" + key + "' is missing in ShopGUI.Layout.");
                }
            }
        } else {
            plugin.getLogger().warning("ShopGUI.Layout section is missing in config.");
        }
    }

    /**
     * Loads ConfirmGUI settings: name, layout pattern, and layout items.
     */
    private void loadConfirmGUI() {
        ConfigurationSection confirmGUISection = config.getConfigurationSection("ConfirmGUI");
        if (confirmGUISection == null) {
            plugin.getLogger().warning("ConfirmGUI section is missing in config.");
            return;
        }

        // GUI Name
        this.confirmGUIName = confirmGUISection.getString("Name", "Default Confirm GUI Name");

        // Layout Pattern
        List<String> patterns = confirmGUISection.getStringList("LayoutPattern");
        if (patterns.isEmpty()) {
            plugin.getLogger().warning("ConfirmGUI.LayoutPattern is empty or missing in config.");
        }
        confirmLayoutPattern.addAll(patterns);

        // Layout Items
        ConfigurationSection layoutSection = confirmGUISection.getConfigurationSection("Layout");
        if (layoutSection != null) {
            for (String key : layoutSection.getKeys(false)) {
                ConfigurationSection itemSection = layoutSection.getConfigurationSection(key);
                if (itemSection != null) {
                    confirmLayoutBuilder.put(key, new ItemBuilder(itemSection));
                } else {
                    plugin.getLogger().warning("Item section for key '" + key + "' is missing in ConfirmGUI.Layout.");
                }
            }
        } else {
            plugin.getLogger().warning("ConfirmGUI.Layout section is missing in config.");
        }
    }

    /**
     * Loads the shop navigation slots (ShopArea, PrevPage, NextPage).
     */
    private void loadShopSection() {
        ConfigurationSection shopSection = config.getConfigurationSection("Shop");
        if (shopSection == null) {
            plugin.getLogger().warning("Shop section is missing in config.");
            return;
        }

        List<Integer> areas = shopSection.getIntegerList("ShopArea");
        List<Integer> next = shopSection.getIntegerList("NextPage");
        List<Integer> prev = shopSection.getIntegerList("PrevPage");

        List<Integer> location = shopSection.getIntegerList("DisplayLocation");
        String displayWorld = shopSection.getString("DisplayWorld");
        String worldguardRegion = shopSection.getString("WorldguardRegion");
        int displayInterval = shopSection.getInt("DisplayInterval");
        int PauseDisplayRange = shopSection.getInt("PauseDisplayRange");

        if (areas.isEmpty()) {
            plugin.getLogger().warning("ShopArea is empty or missing in config.");
        }
        if (next.isEmpty()) {
            plugin.getLogger().warning("NextPage is empty or missing in config.");
        }
        if (prev.isEmpty()) {
            plugin.getLogger().warning("PrevPage is empty or missing in config.");
        }

        if (location.isEmpty() || location.size() < 3) {
            plugin.getLogger().warning("DisplayLocation is invalid or missing in config. setting to default location. (0, 0, 0)");
            location = Arrays.asList(0, 0, 0);
        }

        if (displayWorld == null) {
            plugin.getLogger().warning("DisplayWorld is empty or missing in config. setting to default world. (world)");
        }

        if (worldguardRegion == null) {
            plugin.getLogger().warning("WorldguardRegion is empty or missing in config.");
        }

        if (displayInterval == 0) {
            plugin.getLogger().warning("DisplayInterval is empty or missing in config.");
        }
        if (PauseDisplayRange == 0) {
            plugin.getLogger().warning("PauseDisplayRange is empty or missing in config.");
        }

        displayLocation = new Location(Bukkit.getWorld(shopSection.getString("DisplayWorld", "world")), location.get(0), location.get(1), location.get(2));
        this.worldguardRegion = worldguardRegion;
        this.displayInterval = displayInterval;
        this.PauseDisplayRange = PauseDisplayRange;

        shopArea.addAll(areas);
        nextPage.addAll(next);
        prevPage.addAll(prev);
    }

    /**
     * Loads the confirm GUI slots for Deny, Accept, and Preview.
     */
    private void loadConfirmSection() {
        ConfigurationSection confirmSection = config.getConfigurationSection("Confirm");
        if (confirmSection == null) {
            plugin.getLogger().warning("Confirm section is missing in config.");
            return;
        }

        List<Integer> deny = confirmSection.getIntegerList("Deny");
        List<Integer> accept = confirmSection.getIntegerList("Accept");
        List<Integer> preview = confirmSection.getIntegerList("Preview");

        if (deny.isEmpty()) {
            plugin.getLogger().warning("Confirm.Deny is empty or missing in config.");
        }
        if (accept.isEmpty()) {
            plugin.getLogger().warning("Confirm.Accept is empty or missing in config.");
        }
        if (preview.isEmpty()) {
            plugin.getLogger().warning("Confirm.Preview is empty or missing in config.");
        }

        denySlots.addAll(deny);
        acceptSlots.addAll(accept);
        previewSlots.addAll(preview);
    }

    /**
     * Loads the shop items from shop.yml.
     */
    private void loadShopItems() {
        File file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("shop.yml does not exist. Creating a new one with default values.");
            plugin.saveResource("shop.yml", false);
        }

        FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> itemsList = shopConfig.getMapList("Items");

        if (itemsList.isEmpty()) {
            plugin.getLogger().warning("No items found in shop.yml under 'Items'.");
            return;
        }

        for (Map<?, ?> itemMap : itemsList) {
            if (itemMap.size() != 1) {
                plugin.getLogger().warning("Invalid item entry: " + itemMap + ". Each item should have exactly one key.");
                continue;
            }

            Map.Entry<?, ?> entry = itemMap.entrySet().iterator().next();
            if (!(entry.getKey() instanceof String itemName)) {
                plugin.getLogger().warning("Item key is not a string: " + entry.getKey());
                continue;
            }

            Object value = entry.getValue();

            if (!(value instanceof Map)) {
                plugin.getLogger().warning("Item value for '" + itemName + "' is not a map.");
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) value;

            String currency = Objects.toString(properties.get("currency"), "vault");
            double price;
            try {
                price = Double.parseDouble(properties.get("price").toString());
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid price for item '" + itemName + "'. Setting price to 0.");
                price = 0.0;
            }

            ShopItem shopItem = new ShopItem(itemName, currency, price);
            shopItems.put(itemName, shopItem);

            if (CustomStack.isInRegistry(itemName)) {
                invalidItems.add(itemName);
            }
        }
    }

    // --- Getters ---

    // ShopGUI Getters
    public String getShopGUIName() {
        return shopGUIName;
    }

    public List<String> getShopGUILayoutPattern() {
        return Collections.unmodifiableList(shopLayoutPattern);
    }

    public Map<String, ItemBuilder> getShopGUILayoutBuilder() {
        return Collections.unmodifiableMap(shopLayoutBuilder);
    }

    // ConfirmGUI Getters
    public String getConfirmGUIName() {
        return confirmGUIName;
    }

    public List<String> getConfirmGUILayoutPattern() {
        return Collections.unmodifiableList(confirmLayoutPattern);
    }

    public Map<String, ItemBuilder> getConfirmGUILayoutBuilder() {
        return Collections.unmodifiableMap(confirmLayoutBuilder);
    }

    // Shop Navigation Getters
    public List<Integer> getShopArea() {
        return Collections.unmodifiableList(shopArea);
    }

    public List<Integer> getNextPage() {
        return Collections.unmodifiableList(nextPage);
    }

    public List<Integer> getPrevPage() {
        return Collections.unmodifiableList(prevPage);
    }

    // Confirm Slots Getters
    public List<Integer> getDenySlots() {
        return Collections.unmodifiableList(denySlots);
    }

    public List<Integer> getAcceptSlots() {
        return Collections.unmodifiableList(acceptSlots);
    }

    public List<Integer> getPreviewSlots() {
        return Collections.unmodifiableList(previewSlots);
    }

    // Shop Items Getters

    public ShopItem getShopItemByName(String name) {
        return shopItems.get(name);
    }

    public List<ShopItem> getShopItemsList() {
        return new ArrayList<>(shopItems.values());
    }

    public Integer getShopItemIndex(ShopItem item) {
        return new ArrayList<>(shopItems.values()).indexOf(item);
    }

    public List<String> getInvalidItems() {
        return Collections.unmodifiableList(invalidItems);
    }

    public boolean isInvalidItem(String name) {
        return invalidItems.contains(name);
    }

    // Displaying related Getters

    public Location getDisplayLocation() {
        return displayLocation;
    }

    public String getWorldguardRegion() {
        return worldguardRegion;
    }

    public int getDisplayInterval() {
        return displayInterval;
    }

    public int getPauseDisplayRange() {
        return PauseDisplayRange;
    }

}
