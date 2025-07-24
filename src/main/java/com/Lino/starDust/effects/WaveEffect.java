package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class WaveEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public WaveEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        for (int i = 0; i < config.getParticleCount(); i++) {
            double startX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double startY = playerLoc.getY() + random.nextDouble() * config.getSpawnHeight();
            double startZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double wavePhase = random.nextDouble() * Math.PI * 2;

            new BukkitRunnable() {
                private double x = startX;
                private double y = startY;
                private double z = startZ;
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() || y <= playerLoc.getY() - 5) {
                        this.cancel();
                        return;
                    }

                    y -= config.getFallSpeed();
                    double waveOffset = Math.sin((ticks * 0.1) + wavePhase) * 2;

                    Location currentLoc = new Location(player.getWorld(), x + waveOffset, y, z);

                    ParticleUtils.spawnParticle(player.getWorld(), currentLoc, config.getParticleType());

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @Override
    public void cleanup() {

    }
}