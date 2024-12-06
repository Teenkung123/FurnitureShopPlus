package com.Teenkung123.furnitureShopPlus.Utils;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class RegionChecker {

    private final FurnitureShopPlus plugin;
    
    public RegionChecker(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks if a player is inside a specific WorldGuard region in a specific world.
     *
     * @param player      The player to check.
     * @return            True if the player is inside the region, false otherwise.
     */
    public boolean isPlayerInRegion(Player player) {
        String worldName = plugin.getConfigLoader().getDisplayLocation().getWorld().getName();
        String regionName = plugin.getConfigLoader().getWorldguardRegion();

        // Get the world object
        World world =  plugin.getConfigLoader().getDisplayLocation().getWorld();
        if (world == null) {
            plugin.getLogger().warning("WorldGuard: World '" + worldName + "' not found.");
            return false;
        }

        // Get the player's location
        Location playerLocation = player.getLocation();

        // Get the RegionManager for the world
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            plugin.getLogger().warning("RegionManager for world '" + worldName + "' is null.");
            return false;
        }

        // Get the set of regions at the player's location
        ApplicableRegionSet regions = regionManager.getApplicableRegions(BlockVector3.at(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()));

        // Check if the specified region is among the applicable regions
        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }

        return false;
    }
}
