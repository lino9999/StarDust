package com.Lino.starDust.listeners;

import com.Lino.starDust.StarDust;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final StarDust plugin;

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

                if (plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
                    if (player.hasPermission("stardust.autojoin")) {
                        plugin.getPlayerManager().addPlayer(player);
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

        boolean fromEnabled = plugin.getConfigManager().isWorldEnabled(event.getFrom().getName());
        boolean toEnabled = plugin.getConfigManager().isWorldEnabled(player.getWorld().getName());

        if (fromEnabled && !toEnabled && plugin.getPlayerManager().isPlayerActive(player)) {
            plugin.getPlayerManager().removePlayer(player);
        }
    }
}