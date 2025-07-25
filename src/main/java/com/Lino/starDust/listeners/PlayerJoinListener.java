package com.Lino.starDust.listeners;

import com.Lino.starDust.StarDust;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final StarDust plugin;
    private final Set<UUID> welcomeShown = new HashSet<>();

    public PlayerJoinListener(StarDust plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                boolean firstTime = !player.hasPlayedBefore() || !welcomeShown.contains(player.getUniqueId());

                if (firstTime && isNightTime(player.getWorld())) {
                    showWelcomeEffect(player);
                    welcomeShown.add(player.getUniqueId());
                }

                if (plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
                    if (player.hasPermission("stardust.autojoin")) {
                        plugin.getPlayerManager().addPlayer(player);

                        if (plugin.getPlayerManager().isPlayerActive(player)) {
                            if (!firstTime) {
                                sendWelcomeBackMessage(player);
                            }
                        } else {
                            int position = plugin.getPlayerManager().getQueuePosition(player);
                            if (position > 0) {
                                sendQueueNotification(player, position);
                            }
                        }
                    } else {
                        sendInviteMessage(player);
                    }
                }
            }
        }.runTaskLater(plugin, 40L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().removePlayer(player);

        if (plugin.getPlayerManager().getActivePlayerCount() == 0) {
            plugin.getLogger().info("All players left, StarDust is now idle");
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                boolean fromEnabled = plugin.getConfigManager().isWorldEnabled(event.getFrom().getName());
                boolean toEnabled = plugin.getConfigManager().isWorldEnabled(player.getWorld().getName());

                if (!fromEnabled && toEnabled) {
                    player.sendMessage("");
                    player.sendMessage("§9§l* §b§lSTARDUST AVAILABLE §9§l*");
                    player.sendMessage("§7StarDust effects are available in this world!");
                    player.sendMessage("§7Use §f/stardust toggle §7to enable");
                    player.sendMessage("");

                    playWorldChangeEffect(player);
                } else if (fromEnabled && !toEnabled) {
                    player.sendMessage("");
                    player.sendMessage("§9§l* §c§lSTARDUST UNAVAILABLE §9§l*");
                    player.sendMessage("§7StarDust effects are not available in this world");
                    player.sendMessage("");
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPlayerManager().isPlayerActive(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;

                    player.sendMessage("§9StarDust >> §fYour particles have been restored!");
                    playRespawnEffect(player);
                }
            }.runTaskLater(plugin, 10L);
        }
    }

    private void showWelcomeEffect(Player player) {
        Location loc = player.getLocation();

        new BukkitRunnable() {
            private int step = 0;

            @Override
            public void run() {
                if (step >= 20 || !player.isOnline()) {
                    this.cancel();
                    if (player.isOnline()) {
                        sendFirstTimeMessage(player);
                    }
                    return;
                }

                double radius = 2.0 - (step * 0.1);
                for (int i = 0; i < 360; i += 30) {
                    double angle = Math.toRadians(i);
                    double x = loc.getX() + radius * Math.cos(angle);
                    double z = loc.getZ() + radius * Math.sin(angle);
                    double y = loc.getY() + 2 + (step * 0.1);

                    Location particleLoc = new Location(loc.getWorld(), x, y, z);
                    player.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }

                if (step % 5 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.5f + (step * 0.1f));
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void playWorldChangeEffect(Player player) {
        Location loc = player.getLocation();

        for (int i = 0; i < 20; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                player.getWorld().spawnParticle(
                        Particle.PORTAL,
                        loc.clone().add(0, 1, 0),
                        10,
                        0.5, 0.5, 0.5,
                        0.1
                );
            }, i * 2L);
        }

        player.playSound(loc, Sound.BLOCK_PORTAL_TRAVEL, 0.3f, 2.0f);
    }

    private void playRespawnEffect(Player player) {
        Location loc = player.getLocation();

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 10 || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                player.getWorld().spawnParticle(
                        Particle.TOTEM_OF_UNDYING,
                        loc.clone().add(0, 1, 0),
                        20,
                        0.5, 0.5, 0.5,
                        0.1
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        player.playSound(loc, Sound.ITEM_TOTEM_USE, 0.5f, 1.5f);
    }

    private void sendFirstTimeMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§9§l* §b§lWELCOME TO STARDUST §9§l*");
        player.sendMessage("");
        player.sendMessage("§7Experience magical particle effects");
        player.sendMessage("§7that light up the night sky!");
        player.sendMessage("");
        player.sendMessage("§7Each biome has unique effects");
        player.sendMessage("§7that activate during nighttime");
        player.sendMessage("");
        player.sendMessage("§bUse §f/stardust toggle §bto begin!");
        player.sendMessage("");
    }

    private void sendWelcomeBackMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§9§l* §a§lWELCOME BACK §9§l*");
        player.sendMessage("§7StarDust particles activated!");
        player.sendMessage("");
    }

    private void sendQueueNotification(Player player, int position) {
        player.sendMessage("");
        player.sendMessage("§9§l* §e§lQUEUED §9§l*");
        player.sendMessage("§7Position: §e#" + position);
        player.sendMessage("");
    }

    private void sendInviteMessage(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                if (isNightTime(player.getWorld())) {
                    player.sendMessage("");
                    player.sendMessage("§9§l* §b§lSTARDUST NIGHT §9§l*");
                    player.sendMessage("§7The night sky awaits your magic!");
                    player.sendMessage("§7Use §f/stardust toggle §7to join");
                    player.sendMessage("");
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    private boolean isNightTime(org.bukkit.World world) {
        if (world.getEnvironment() != org.bukkit.World.Environment.NORMAL) return true;
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }
}