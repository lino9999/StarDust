package com.Lino.starDust.commands;

import com.Lino.starDust.StarDust;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StarDustCommand implements CommandExecutor {

    private final StarDust plugin;

    public StarDustCommand(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("stardust.reload")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                sender.sendMessage("§9StarDust §7» §fConfiguration reloaded!");
                break;

            case "status":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command!");
                    return true;
                }
                Player player = (Player) sender;
                if (plugin.getPlayerManager().isPlayerActive(player)) {
                    sender.sendMessage("§9StarDust §7» §fYou are currently seeing particles!");
                } else {
                    int position = plugin.getPlayerManager().getQueuePosition(player);
                    if (position > 0) {
                        sender.sendMessage("§9StarDust §7» §fQueue position: §b" + position);
                    } else {
                        sender.sendMessage("§9StarDust §7» §fYou are not in the particle system!");
                    }
                }
                break;

            case "toggle":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command!");
                    return true;
                }
                Player togglePlayer = (Player) sender;
                if (plugin.getPlayerManager().isPlayerActive(togglePlayer)) {
                    plugin.getPlayerManager().removePlayer(togglePlayer);
                    sender.sendMessage("§9StarDust §7» §fParticles disabled!");
                } else {
                    plugin.getPlayerManager().addPlayer(togglePlayer);
                    sender.sendMessage("§9StarDust §7» §fParticles enabled!");
                }
                break;

            default:
                sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§9§lStarDust Commands:");
        sender.sendMessage("§f/stardust status §7- Check your particle status");
        sender.sendMessage("§f/stardust toggle §7- Toggle particles on/off");
        if (sender.hasPermission("stardust.reload")) {
            sender.sendMessage("§f/stardust reload §7- Reload configuration");
        }
    }
}