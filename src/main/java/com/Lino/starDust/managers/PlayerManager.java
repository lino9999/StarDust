package com.Lino.starDust.managers;

import com.Lino.starDust.StarDust;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerManager {

    private final StarDust plugin;
    private final Set<UUID> activeParticlePlayers = new HashSet<>();
    private final Queue<UUID> playerQueue = new LinkedList<>();

    public PlayerManager(StarDust plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (activeParticlePlayers.contains(uuid) || playerQueue.contains(uuid)) {
            return;
        }

        if (activeParticlePlayers.size() < plugin.getConfigManager().getMaxPlayers()) {
            activeParticlePlayers.add(uuid);
        } else {
            playerQueue.offer(uuid);
        }
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        activeParticlePlayers.remove(uuid);
        playerQueue.remove(uuid);

        if (!playerQueue.isEmpty() && activeParticlePlayers.size() < plugin.getConfigManager().getMaxPlayers()) {
            UUID next = playerQueue.poll();
            if (next != null && plugin.getServer().getPlayer(next) != null) {
                activeParticlePlayers.add(next);
            }
        }
    }

    public Set<Player> getActiveParticlePlayers() {
        Set<Player> players = new HashSet<>();
        Iterator<UUID> iterator = activeParticlePlayers.iterator();

        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Player player = plugin.getServer().getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                iterator.remove();
                promoteFromQueue();
            } else {
                players.add(player);
            }
        }

        return players;
    }

    private void promoteFromQueue() {
        while (!playerQueue.isEmpty() && activeParticlePlayers.size() < plugin.getConfigManager().getMaxPlayers()) {
            UUID next = playerQueue.poll();
            if (next != null) {
                Player player = plugin.getServer().getPlayer(next);
                if (player != null && player.isOnline()) {
                    activeParticlePlayers.add(next);
                    break;
                }
            }
        }
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
}