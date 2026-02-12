package com.Teenkung123.furnitureShopPlus.Preview;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PreviewTimer {

    private final FurnitureShopPlus plugin;
    private final Random random;
    private CustomFurniture furniture;
    private BukkitTask task;
    private UUID lastSpawnedEntityUUID; // Track spawned entity for cleanup

    public PreviewTimer(FurnitureShopPlus plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void start() {
        Location displayLoc = plugin.getConfigLoader().getDisplayLocation();
        if (displayLoc != null) {
            // Async cleanup before starting
            forceCleanupDisplayArea().thenRun(() -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    preview(null, true);
                });
            });
        }
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
            while (plugin.getConfigLoader().isInvalidItem(namespace) && idx < 20) {
                randomIndex = random.nextInt(plugin.getConfigLoader().getShopItemsList().size());
                namespace = plugin.getConfigLoader().getShopItemsList().get(randomIndex).namespace();
                idx++;
            }
        }
        if (plugin.getConfigLoader().isInvalidItem(namespace)) {
            plugin.getLogger().warning("Skipping display of missing furniture: " + namespace);
            return;
        }

        // Enhanced cleanup before spawning new furniture
        tryRemoveArmorStandAndFrames();
        if (furniture != null) {
            try {
                furniture.remove(false);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to remove previous furniture: " + e.getMessage());
            }
            furniture = null;
        }

        try {
            // Calculate spawn location with yaw and optional auto-centering
            Location spawnLocation = calculateSpawnLocation(displayLocation);
            furniture = CustomFurniture.spawnPreciseNonSolid(namespace, spawnLocation);

            if (furniture != null && furniture.getEntity() != null) {
                lastSpawnedEntityUUID = furniture.getEntity().getUniqueId();
            }
        } catch (RuntimeException e) {
            plugin.getLogger().warning("Failed to spawn furniture '" + namespace + "': " + e.getMessage());
            furniture = null;
            lastSpawnedEntityUUID = null;
        }
    }

    /**
     * Calculate spawn location with yaw and auto-centering support.
     */
    private Location calculateSpawnLocation(Location baseLocation) {
        Location spawnLoc = baseLocation.getBlock().getLocation().clone();

        // Apply auto-centering if enabled
        if (plugin.getConfigLoader().isAutoCenterDisplay()) {
            spawnLoc.add(0.5, 0, 0.5);
        }

        // Apply yaw rotation
        spawnLoc.setYaw(plugin.getConfigLoader().getDisplayYaw());

        return spawnLoc;
    }

    /**
     * Remove armor stands and item frames near display location.
     * Uses synchronous approach since it's called from sync context.
     */
    private void tryRemoveArmorStandAndFrames() {
        Location displayLoc = plugin.getConfigLoader().getDisplayLocation();
        if (displayLoc == null) return;

        double radius = plugin.getConfigLoader().getCleanupRadius();

        try {
            for (Entity entity : displayLoc.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof Player) continue;

                try {
                    if (CustomFurniture.byAlreadySpawned(entity) != null) {
                        CustomFurniture.byAlreadySpawned(entity).remove(false);
                    }
                    if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
                        entity.remove();
                    }
                    if (entity.getType() == EntityType.ARMOR_STAND) {
                        entity.remove();
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to remove entity " + entity.getType() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to cleanup display area: " + e.getMessage());
        }
    }

    /**
     * Force cleanup display area with async chunk loading.
     * Returns a CompletableFuture that completes when cleanup is done.
     */
    public CompletableFuture<Void> forceCleanupDisplayArea() {
        Location displayLoc = plugin.getConfigLoader().getDisplayLocation();
        if (displayLoc == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        // Async chunk loading for optimal performance
        displayLoc.getWorld().getChunkAtAsync(displayLoc).thenAccept(chunk -> {
            // Execute cleanup on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    double radius = plugin.getConfigLoader().getCleanupRadius();

                    for (Entity entity : displayLoc.getNearbyEntities(radius, radius, radius)) {
                        if (entity instanceof Player) continue;

                        try {
                            if (CustomFurniture.byAlreadySpawned(entity) != null) {
                                CustomFurniture.byAlreadySpawned(entity).remove(false);
                            }
                            if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
                                entity.remove();
                            }
                            if (entity.getType() == EntityType.ARMOR_STAND) {
                                entity.remove();
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to remove entity during force cleanup: " + e.getMessage());
                        }
                    }

                    plugin.getLogger().info("Display area cleanup completed.");
                    future.complete(null);
                } catch (Exception e) {
                    plugin.getLogger().severe("Force cleanup failed: " + e.getMessage());
                    future.completeExceptionally(e);
                }
            });
        }).exceptionally(throwable -> {
            plugin.getLogger().severe("Failed to load chunk for cleanup: " + throwable.getMessage());
            future.completeExceptionally(throwable);
            return null;
        });

        return future;
    }

    public void removeFurniture() {
        if (furniture != null) {
            try {
                furniture.remove(false);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to remove furniture on disable: " + e.getMessage());
            }
            furniture = null;
        }
        lastSpawnedEntityUUID = null;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

}
