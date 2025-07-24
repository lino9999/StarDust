package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SpiralEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public SpiralEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        for (int i = 0; i < config.getParticleCount(); i++) {
            double startRadius = (config.getSpawnRadius() * 0.3) + (random.nextDouble() * config.getSpawnRadius() * 0.7);
            double startY = playerLoc.getY() + random.nextDouble() * config.getSpawnHeight();
            double startAngle = random.nextDouble() * Math.PI * 2;

            new BukkitRunnable() {
                private double angle = startAngle;
                private double y = startY;
                private double radius = startRadius;
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() || y <= playerLoc.getY() - 5) {
                        this.cancel();
                        return;
                    }

                    angle += 0.1;
                    y -= config.getFallSpeed();
                    radius *= 0.98;

                    double x = playerLoc.getX() + Math.cos(angle) * radius;
                    double z = playerLoc.getZ() + Math.sin(angle) * radius;

                    Location currentLoc = new Location(player.getWorld(), x, y, z);

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