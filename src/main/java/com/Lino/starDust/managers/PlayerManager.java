package com.Lino.starDust.managers;

import com.Lino.starDust.StarDust;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerManager {

    private final StarDust plugin;
    private final Set<UUID> activeParticlePlayers = ConcurrentHashMap.newKeySet();
    private final Queue<UUID> playerQueue = new ConcurrentLinkedQueue<>();
    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerStats> playerStats = new ConcurrentHashMap<>();
    private final AtomicInteger totalParticlesSpawned = new AtomicInteger(0);

    public PlayerManager(StarDust plugin) {
        this.plugin = plugin;
        startQueueManager();
        startStatsTracker();
    }

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (activeParticlePlayers.contains(uuid) || playerQueue.contains(uuid)) {
            return;
        }

        joinTimes.put(uuid, System.currentTimeMillis());
        playerStats.putIfAbsent(uuid, new PlayerStats());

        if (activeParticlePlayers.size() < plugin.getConfigManager().getMaxPlayers()) {
            activeParticlePlayers.add(uuid);
            sendActivationMessage(player);
            playActivationEffect(player);
        } else {
            playerQueue.offer(uuid);
            sendQueueMessage(player);
        }
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean wasActive = activeParticlePlayers.remove(uuid);
        boolean wasQueued = playerQueue.remove(uuid);

        if (wasActive) {
            PlayerStats stats = playerStats.get(uuid);
            if (stats != null) {
                long sessionTime = System.currentTimeMillis() - joinTimes.getOrDefault(uuid, System.currentTimeMillis());
                stats.addSessionTime(sessionTime);
            }
            sendDeactivationMessage(player);
            promoteNextInQueue();
        } else if (wasQueued) {
            player.sendMessage("§9StarDust >> §fYou have been removed from the queue");
        }

        joinTimes.remove(uuid);
    }

    private void promoteNextInQueue() {
        while (!playerQueue.isEmpty() && activeParticlePlayers.size() < plugin.getConfigManager().getMaxPlayers()) {
            UUID next = playerQueue.poll();
            if (next != null) {
                Player player = plugin.getServer().getPlayer(next);
                if (player != null && player.isOnline()) {
                    activeParticlePlayers.add(next);
                    sendPromotionMessage(player);
                    playActivationEffect(player);
                    notifyQueueUpdate();
                    break;
                }
            }
        }
    }

    private void startQueueManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<UUID> iterator = activeParticlePlayers.iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    Player player = plugin.getServer().getPlayer(uuid);

                    if (player == null || !player.isOnline()) {
                        iterator.remove();
                        promoteNextInQueue();
                    }
                }

                cleanupOfflinePlayers();
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    private void startStatsTracker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : activeParticlePlayers) {
                    PlayerStats stats = playerStats.get(uuid);
                    if (stats != null) {
                        stats.incrementActiveTime();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void cleanupOfflinePlayers() {
        playerQueue.removeIf(uuid -> {
            Player player = plugin.getServer().getPlayer(uuid);
            return player == null || !player.isOnline();
        });

        joinTimes.entrySet().removeIf(entry -> {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            return player == null || !player.isOnline();
        });
    }

    private void notifyQueueUpdate() {
        int position = 1;
        for (UUID uuid : playerQueue) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage("§9StarDust >> §fYour queue position updated: §b#" + position);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
            }
            position++;
        }
    }

    private void sendActivationMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§9§l* §b§lSTARDUST ACTIVATED §9§l*");
        player.sendMessage("§7Magical particles now surround you!");
        player.sendMessage("§7Type §f/stardust §7for more options");
        player.sendMessage("");
    }

    private void sendDeactivationMessage(Player player) {
        PlayerStats stats = playerStats.get(player.getUniqueId());
        if (stats != null) {
            player.sendMessage("");
            player.sendMessage("§9§l* §c§lSTARDUST DEACTIVATED §9§l*");
            player.sendMessage("§7Session time: §f" + formatTime(stats.getLastSessionTime()));
            player.sendMessage("");
        }
    }

    private void sendQueueMessage(Player player) {
        int position = getQueuePosition(player);
        player.sendMessage("");
        player.sendMessage("§9§l* §e§lQUEUED FOR STARDUST §9§l*");
        player.sendMessage("§7Queue position: §e#" + position);
        player.sendMessage("§7Estimated wait: §e" + estimateWaitTime(position));
        player.sendMessage("");
    }

    private void sendPromotionMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§9§l* §a§lPROMOTED FROM QUEUE §9§l*");
        player.sendMessage("§7You are now experiencing StarDust!");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    private void playActivationEffect(Player player) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 10 || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                player.getWorld().spawnParticle(
                        org.bukkit.Particle.END_ROD,
                        player.getLocation().add(0, 2, 0),
                        10,
                        0.5, 0.5, 0.5,
                        0.1
                );

                player.getWorld().spawnParticle(
                        org.bukkit.Particle.FIREWORK,
                        player.getLocation().add(0, 1, 0),
                        5,
                        0.3, 0.3, 0.3,
                        0.05
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private String estimateWaitTime(int position) {
        int estimatedSeconds = position * 30;
        return formatTime(estimatedSeconds * 1000L);
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

    public Set<Player> getActiveParticlePlayers() {
        Set<Player> players = new HashSet<>();
        for (UUID uuid : activeParticlePlayers) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    public boolean isPlayerActive(Player player) {
        return activeParticlePlayers.contains(player.getUniqueId());
    }

    public int getQueuePosition(Player player) {
        UUID uuid = player.getUniqueId();
        if (activeParticlePlayers.contains(uuid)) return 0;

        int position = 1;
        for (UUID queuedUuid : playerQueue) {
            if (queuedUuid.equals(uuid)) return position;
            position++;
        }
        return -1;
    }

    public PlayerStats getPlayerStats(Player player) {
        return playerStats.get(player.getUniqueId());
    }

    public int getActivePlayerCount() {
        return activeParticlePlayers.size();
    }

    public int getQueuedPlayerCount() {
        return playerQueue.size();
    }

    public void incrementTotalParticles(int amount) {
        totalParticlesSpawned.addAndGet(amount);
    }

    public int getTotalParticlesSpawned() {
        return totalParticlesSpawned.get();
    }

    public static class PlayerStats {
        private long totalActiveTime = 0;
        private long lastSessionTime = 0;
        private int sessionsCount = 0;

        public void incrementActiveTime() {
            totalActiveTime += 1000;
        }

        public void addSessionTime(long time) {
            lastSessionTime = time;
            sessionsCount++;
        }

        public long getTotalActiveTime() {
            return totalActiveTime;
        }

        public long getLastSessionTime() {
            return lastSessionTime;
        }

        public int getSessionsCount() {
            return sessionsCount;
        }
    }
}