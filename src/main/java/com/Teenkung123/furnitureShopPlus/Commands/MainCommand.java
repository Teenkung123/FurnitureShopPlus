package com.Teenkung123.furnitureShopPlus.Commands;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {
    private final FurnitureShopPlus plugin;

    public MainCommand(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getShopGUI().openGUI(player, 0);
            }
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("furnitureshopplus.reload")) {
                        long ms = System.currentTimeMillis();
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("Reloading")));
                        plugin.reload();
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("Reloaded"), Placeholder.unparsed("time", String.valueOf(System.currentTimeMillis() - ms))));
                    } else {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoPermission")));
                    }
                }
            }
        }
        return true;
    }

}
