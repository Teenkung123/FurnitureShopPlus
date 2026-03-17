package com.Teenkung123.furnitureShopPlus.Commands;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommandTabCompleter implements TabCompleter {

    private final FurnitureShopPlus plugin;

    public MainCommandTabCompleter(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - show available subcommands based on permissions
            if (sender.hasPermission("furnitureshopplus.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("furnitureshopplus.admin.missing")) {
                completions.add("missing");
            }
            if (sender.hasPermission("furnitureshopplus.admin.remove")) {
                completions.add("remove");
            }
            if (sender.hasPermission("furnitureshopplus.admin.purge")) {
                completions.add("purge");
            }
            if (sender.hasPermission("furnitureshopplus.admin.cleanup")) {
                completions.add("cleanup");
            }

            // Filter based on what the user has typed so far
            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            // Second argument for "remove" - show all furniture namespaces in shop
            if (sender.hasPermission("furnitureshopplus.admin.remove")) {
                String input = args[1].toLowerCase();

                // Get all shop items and suggest their namespaces
                return plugin.getConfigLoader().getShopItemsList().stream()
                        .map(com.Teenkung123.furnitureShopPlus.Utils.ShopItem::namespace)
                        .filter(namespace -> namespace.toLowerCase().startsWith(input))
                        .sorted()
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}


