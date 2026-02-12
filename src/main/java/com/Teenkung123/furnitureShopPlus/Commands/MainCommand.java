package com.Teenkung123.furnitureShopPlus.Commands;

import com.Teenkung123.furnitureShopPlus.FurnitureShopPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "reload" -> {
                    if (!player.hasPermission("furnitureshopplus.reload")) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoPermission")));
                        return true;
                    }
                    handleReload(player);
                }
                case "missing" -> {
                    if (!player.hasPermission("furnitureshopplus.admin.missing")) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoPermission")));
                        return true;
                    }
                    handleMissing(player);
                }
                case "remove" -> {
                    if (!player.hasPermission("furnitureshopplus.admin.remove")) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoPermission")));
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage("§cUsage: /fsp remove <namespace>");
                        return true;
                    }
                    handleRemove(player, args[1]);
                }
                case "purge" -> {
                    if (!player.hasPermission("furnitureshopplus.admin.purge")) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoPermission")));
                        return true;
                    }
                    handlePurge(player);
                }
                case "cleanup" -> {
                    if (!player.hasPermission("furnitureshopplus.admin.cleanup")) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoPermission")));
                        return true;
                    }
                    handleCleanup(player);
                }
                default -> plugin.getShopGUI().openGUI(player, 0);
            }
        } else {
            // Console commands
            if (args.length == 0) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }

            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("reload")) {
                handleReload(sender);
            } else if (subCommand.equals("missing")) {
                handleMissing(sender);
            } else if (subCommand.equals("remove") && args.length >= 2) {
                handleRemove(sender, args[1]);
            } else if (subCommand.equals("purge")) {
                handlePurge(sender);
            } else if (subCommand.equals("cleanup")) {
                handleCleanup(sender);
            } else {
                sender.sendMessage("§cUsage: /fsp <reload|missing|remove|purge|cleanup>");
            }
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        long ms = System.currentTimeMillis();
        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("Reloading")));
        plugin.reload();
        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageLoader().getMessage("Reloaded"),
                Placeholder.unparsed("time", String.valueOf(System.currentTimeMillis() - ms))
        ));
    }

    private void handleMissing(CommandSender sender) {
        List<String> missing = plugin.getConfigLoader().getMissingFurniture();

        if (missing.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoMissingFurniture")));
            return;
        }

        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageLoader().getMessage("MissingFurnitureHeader"),
                Placeholder.unparsed("count", String.valueOf(missing.size()))
        ));

        for (String namespace : missing) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageLoader().getMessage("MissingFurnitureEntry"),
                    Placeholder.unparsed("namespace", namespace)
            ));
        }
    }

    private void handleRemove(CommandSender sender, String namespace) {
        if (plugin.getConfigLoader().removeFurnitureFromShop(namespace)) {
            try {
                plugin.getConfigLoader().saveShopConfig();
                sender.sendMessage(MiniMessage.miniMessage().deserialize(
                        plugin.getMessageLoader().getMessage("FurnitureRemoved"),
                        Placeholder.unparsed("namespace", namespace)
                ));
            } catch (Exception e) {
                sender.sendMessage("§cFailed to save shop.yml: " + e.getMessage());
                plugin.getLogger().severe("Failed to save shop.yml: " + e.getMessage());
            }
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageLoader().getMessage("FurnitureNotInShop"),
                    Placeholder.unparsed("namespace", namespace)
            ));
        }
    }

    private void handlePurge(CommandSender sender) {
        List<String> missing = plugin.getConfigLoader().getMissingFurniture();

        if (missing.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("NoMissingFurniture")));
            return;
        }

        int count = missing.size();

        try {
            // Create backup
            String backupFileName = plugin.getConfigLoader().createShopBackup();
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageLoader().getMessage("BackupCreated"),
                    Placeholder.unparsed("filename", "backup/" + backupFileName)
            ));

            // Remove all missing items
            for (String namespace : missing) {
                plugin.getConfigLoader().removeFurnitureFromShop(namespace);
            }

            // Save changes
            plugin.getConfigLoader().saveShopConfig();

            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageLoader().getMessage("PurgedCount"),
                    Placeholder.unparsed("count", String.valueOf(count))
            ));

        } catch (Exception e) {
            sender.sendMessage("§cFailed to purge furniture: " + e.getMessage());
            plugin.getLogger().severe("Failed to purge furniture: " + e.getMessage());
        }
    }

    private void handleCleanup(CommandSender sender) {
        sender.sendMessage("§eForcing display area cleanup...");
        plugin.getPreviewTimer().forceCleanupDisplayArea().thenRun(() -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageLoader().getMessage("CleanupComplete")));
        }).exceptionally(throwable -> {
            sender.sendMessage("§cCleanup failed: " + throwable.getMessage());
            return null;
        });
    }

}
