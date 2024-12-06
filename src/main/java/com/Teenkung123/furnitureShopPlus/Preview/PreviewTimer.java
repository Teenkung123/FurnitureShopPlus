package com.Teenkung123.furnitureShopPlus.Preview;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Random;

public class PreviewTimer {

    private final FurnitureShopPlus plugin;
    private final Random random;
    private CustomFurniture furniture;
    private BukkitTask task;

    public PreviewTimer(FurnitureShopPlus plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void start() {
        if (CustomFurniture.byAlreadySpawned(plugin.getConfigLoader().getDisplayLocation().getBlock()) != null) {
            CustomFurniture.byAlreadySpawned(plugin.getConfigLoader().getDisplayLocation().getBlock()).remove(false);
        }
        preview(null, true);
        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                preview(null, false);
            });
        }, plugin.getConfigLoader().getDisplayInterval(), plugin.getConfigLoader().getDisplayInterval());
    }

    public void preview(@SuppressWarnings("SameParameterValue") String namespace, boolean bypassDistance) {
        Location displayLocation = plugin.getConfigLoader().getDisplayLocation();
        if (displayLocation == null) {
            return;
        }
        if (!displayLocation.getNearbyPlayers(plugin.getConfigLoader().getPauseDisplayRange()).isEmpty() && !bypassDistance) {
           return;
        }

        if (namespace == null) {
            int randomIndex = random.nextInt(plugin.getConfigLoader().getShopItemsList().size());
            namespace = plugin.getConfigLoader().getShopItemsList().get(randomIndex).namespace();
            int idx = 0;
            while (!plugin.getConfigLoader().isInvalidItem(namespace) && idx < 20) {
                randomIndex = random.nextInt(plugin.getConfigLoader().getShopItemsList().size());
                namespace = plugin.getConfigLoader().getShopItemsList().get(randomIndex).namespace();
                idx++;
            }
        }
        if (!plugin.getConfigLoader().isInvalidItem(namespace)) {
            return;
        }
        if (furniture == null) {
            furniture = CustomFurniture.spawnPreciseNonSolid(namespace, displayLocation.getBlock().getLocation().add(0.5, 0, 0.5));
            return;
        }
        furniture.remove(false);
        furniture = CustomFurniture.spawnPreciseNonSolid(namespace, displayLocation.getBlock().getLocation().add(0.5, 0, 0.5));
    }

    public void removeFurniture() {
        if (furniture != null) {
            furniture.remove(false);
            furniture = null;
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

}
