package com.Lino.starDust.commands;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StarDustCommand implements CommandExecutor, TabCompleter {

    private final StarDust plugin;

    public StarDustCommand(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, 1);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "status":
                handleStatus(sender);
                break;

            case "toggle":
                handleToggle(sender);
                break;

            case "stats":
                handleStats(sender, args);
                break;

            case "info":
                handleInfo(sender);
                break;

            case "biome":
                handleBiomeInfo(sender);
                break;

            case "list":
                handleList(sender);
                break;

            case "admin":
                if (args.length > 1) {
                    handleAdmin(sender, args);
                } else {
                    sendAdminHelp(sender);
                }
                break;

            case "help":
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {}
                }
                sendHelp(sender, page);
                break;

            default:
                sender.sendMessage("§cUnknown command. Use §f/stardust help");
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("stardust.reload")) {
            sender.sendMessage("§cYou don't have permission to reload StarDust!");
            return;
        }

        long startTime = System.currentTimeMillis();
        plugin.getConfigManager().loadConfig();
        long endTime = System.currentTimeMillis();

        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §a§lCONFIG RELOADED §9§l⋆");
        sender.sendMessage("§7Reload time: §f" + (endTime - startTime) + "ms");
        sender.sendMessage("");

        Bukkit.getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2.0f)
        );
    }

    private void handleStatus(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can check their status!");
            return;
        }

        Player player = (Player) sender;
        boolean isActive = plugin.getPlayerManager().isPlayerActive(player);
        int queuePos = plugin.getPlayerManager().getQueuePosition(player);
        PlayerManager.PlayerStats stats = plugin.getPlayerManager().getPlayerStats(player);

        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §b§lYOUR STARDUST STATUS §9§l⋆");
        sender.sendMessage("");

        if (isActive) {
            sender.sendMessage("§7Status: §a§lACTIVE");
            sender.sendMessage("§7Current biome: §f" + player.getLocation().getBlock().getBiome().toString());
            BiomeConfig config = plugin.getConfigManager().getBiomeConfig(player.getLocation().getBlock().getBiome().toString());
            sender.sendMessage("§7Effect type: §f" + config.getEffectType());
            sender.sendMessage("§7Particle type: §f" + config.getParticleType());
        } else if (queuePos > 0) {
            sender.sendMessage("§7Status: §e§lQUEUED");
            sender.sendMessage("§7Queue position: §e#" + queuePos);
            sender.sendMessage("§7Players ahead: §e" + (queuePos - 1));
        } else {
            sender.sendMessage("§7Status: §c§lINACTIVE");
            sender.sendMessage("§7Use §f/stardust toggle §7to join");
        }

        if (stats != null && stats.getTotalActiveTime() > 0) {
            sender.sendMessage("");
            sender.sendMessage("§7Total time active: §f" + formatTime(stats.getTotalActiveTime()));
            sender.sendMessage("§7Sessions count: §f" + stats.getSessionsCount());
        }

        sender.sendMessage("");
    }

    private void handleToggle(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can toggle particles!");
            return;
        }

        Player player = (Player) sender;
        if (plugin.getPlayerManager().isPlayerActive(player)) {
            plugin.getPlayerManager().removePlayer(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        } else {
            plugin.getPlayerManager().addPlayer(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
        }
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cSpecify a player name!");
                return;
            }
            showPlayerStats(sender, (Player) sender);
        } else {
            if (!sender.hasPermission("stardust.stats.others")) {
                sender.sendMessage("§cYou can only view your own stats!");
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
            showPlayerStats(sender, target);
        }
    }

    private void showPlayerStats(CommandSender sender, Player target) {
        PlayerManager.PlayerStats stats = plugin.getPlayerManager().getPlayerStats(target);

        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §b§lSTARDUST STATS §9§l⋆");
        sender.sendMessage("§7Player: §f" + target.getName());
        sender.sendMessage("");

        if (stats != null) {
            sender.sendMessage("§7Total active time: §f" + formatTime(stats.getTotalActiveTime()));
            sender.sendMessage("§7Sessions played: §f" + stats.getSessionsCount());
            sender.sendMessage("§7Last session: §f" + formatTime(stats.getLastSessionTime()));
            sender.sendMessage("§7Average session: §f" + formatTime(
                    stats.getSessionsCount() > 0 ? stats.getTotalActiveTime() / stats.getSessionsCount() : 0
            ));
        } else {
            sender.sendMessage("§7No statistics available for this player");
        }

        sender.sendMessage("");
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §b§lSTARDUST INFO §9§l⋆");
        sender.sendMessage("");
        sender.sendMessage("§7Version: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Author: §f" + plugin.getDescription().getAuthors().get(0));
        sender.sendMessage("§7Active players: §f" + plugin.getPlayerManager().getActivePlayerCount() +
                "/" + plugin.getConfigManager().getMaxPlayers());
        sender.sendMessage("§7Queued players: §f" + plugin.getPlayerManager().getQueuedPlayerCount());
        sender.sendMessage("§7Enabled worlds: §f" + String.join(", ",
                plugin.getConfig().getStringList("enabled-worlds")));
        sender.sendMessage("§7Spawn interval: §f" + plugin.getConfigManager().getSpawnInterval() + " ticks");
        sender.sendMessage("");
    }

    private void handleBiomeInfo(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can check biome info!");
            return;
        }

        Player player = (Player) sender;
        String biomeName = player.getLocation().getBlock().getBiome().toString();
        BiomeConfig config = plugin.getConfigManager().getBiomeConfig(biomeName);

        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §b§lBIOME CONFIGURATION §9§l⋆");
        sender.sendMessage("");
        sender.sendMessage("§7Current biome: §f" + biomeName);
        sender.sendMessage("§7Effect type: §f" + config.getEffectType());
        sender.sendMessage("§7Particle: §f" + config.getParticleType());
        sender.sendMessage("§7Fall speed: §f" + config.getFallSpeed());
        sender.sendMessage("§7Count: §f" + config.getParticleCount());
        sender.sendMessage("§7Radius: §f" + config.getSpawnRadius() + " blocks");
        sender.sendMessage("§7Height: §f" + config.getSpawnHeight() + " blocks");
        sender.sendMessage("");
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("stardust.list")) {
            sender.sendMessage("§cYou don't have permission to list players!");
            return;
        }

        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §b§lACTIVE PLAYERS §9§l⋆");
        sender.sendMessage("");

        List<Player> activePlayers = new ArrayList<>(plugin.getPlayerManager().getActiveParticlePlayers());
        if (activePlayers.isEmpty()) {
            sender.sendMessage("§7No players currently have particles active");
        } else {
            for (int i = 0; i < activePlayers.size(); i++) {
                Player p = activePlayers.get(i);
                sender.sendMessage("§b" + (i + 1) + ". §f" + p.getName() +
                        " §7(" + p.getLocation().getBlock().getBiome().toString() + ")");
            }
        }

        sender.sendMessage("");
        sender.sendMessage("§7Total: §f" + activePlayers.size() + "/" +
                plugin.getConfigManager().getMaxPlayers());
        sender.sendMessage("");
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("stardust.admin")) {
            sender.sendMessage("§cYou don't have permission for admin commands!");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /stardust admin add <player>");
                    return;
                }
                Player targetAdd = Bukkit.getPlayer(args[2]);
                if (targetAdd == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return;
                }
                plugin.getPlayerManager().addPlayer(targetAdd);
                sender.sendMessage("§aAdded " + targetAdd.getName() + " to StarDust");
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /stardust admin remove <player>");
                    return;
                }
                Player targetRemove = Bukkit.getPlayer(args[2]);
                if (targetRemove == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return;
                }
                plugin.getPlayerManager().removePlayer(targetRemove);
                sender.sendMessage("§aRemoved " + targetRemove.getName() + " from StarDust");
                break;

            case "clear":
                for (Player p : plugin.getPlayerManager().getActiveParticlePlayers()) {
                    plugin.getPlayerManager().removePlayer(p);
                }
                sender.sendMessage("§aCleared all active players");
                break;

            default:
                sendAdminHelp(sender);
        }
    }

    private void sendHelp(CommandSender sender, int page) {
        int totalPages = sender.hasPermission("stardust.admin") ? 2 : 1;
        page = Math.max(1, Math.min(page, totalPages));

        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §b§lSTARDUST HELP §7(Page " + page + "/" + totalPages + ") §9§l⋆");
        sender.sendMessage("");

        if (page == 1) {
            sender.sendMessage("§b/stardust §7- Show this help");
            sender.sendMessage("§b/stardust status §7- Check your particle status");
            sender.sendMessage("§b/stardust toggle §7- Toggle particles on/off");
            sender.sendMessage("§b/stardust stats §7- View your statistics");
            sender.sendMessage("§b/stardust info §7- Plugin information");
            sender.sendMessage("§b/stardust biome §7- Current biome settings");

            if (sender.hasPermission("stardust.list")) {
                sender.sendMessage("§b/stardust list §7- List active players");
            }
            if (sender.hasPermission("stardust.reload")) {
                sender.sendMessage("§b/stardust reload §7- Reload configuration");
            }
        } else if (page == 2 && sender.hasPermission("stardust.admin")) {
            sender.sendMessage("§c/stardust admin add <player> §7- Force add player");
            sender.sendMessage("§c/stardust admin remove <player> §7- Force remove player");
            sender.sendMessage("§c/stardust admin clear §7- Remove all players");
        }

        sender.sendMessage("");
        if (totalPages > 1) {
            sender.sendMessage("§7Use §f/stardust help " + (page == 1 ? "2" : "1") + " §7for " +
                    (page == 1 ? "admin" : "basic") + " commands");
        }
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§9§l⋆ §c§lADMIN COMMANDS §9§l⋆");
        sender.sendMessage("");
        sender.sendMessage("§c/stardust admin add <player> §7- Force add player");
        sender.sendMessage("§c/stardust admin remove <player> §7- Force remove player");
        sender.sendMessage("§c/stardust admin clear §7- Remove all players");
        sender.sendMessage("");
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "status", "toggle", "stats", "info", "biome", "list"));
            if (sender.hasPermission("stardust.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("stardust.admin")) {
                completions.add("admin");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("stardust.admin")) {
                completions.addAll(Arrays.asList("add", "remove", "clear"));
            } else if (args[0].equalsIgnoreCase("stats") && sender.hasPermission("stardust.stats.others")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("admin") &&
                    (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) &&
                    sender.hasPermission("stardust.admin")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}