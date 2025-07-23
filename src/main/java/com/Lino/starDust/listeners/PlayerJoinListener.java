package com.Lino.starDust.listeners;

import com.Lino.starDust.StarDust;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final StarDust plugin;

    public PlayerJoinListener(StarDust plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().addPlayer(player);

        if (plugin.getPlayerManager().isPlayerActive(player)) {
            player.sendMessage("§9StarDust §7» §fNighttime particles enabled!");
        } else {
            int position = plugin.getPlayerManager().getQueuePosition(player);
            if (position > 0) {
                player.sendMessage("§9StarDust §7» §fYou are in queue position: §b" + position);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().removePlayer(event.getPlayer());
    }
}