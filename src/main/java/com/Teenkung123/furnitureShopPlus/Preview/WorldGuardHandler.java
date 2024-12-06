package com.Teenkung123.furnitureShopPlus.Preview;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldGuardHandler implements Listener {

    private final FurnitureShopPlus plugin;

    public WorldGuardHandler(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getRegionChecker().isPlayerInRegion(event.getPlayer())) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (!player.isOp()) event.setCancelled(true);
            if (player.isOp() && event.getAction() == Action.LEFT_CLICK_BLOCK) { return; }
            plugin.getShopGUI().openGUI(player, 0);
        }
    }

}
