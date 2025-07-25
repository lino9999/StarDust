package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class FallingEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public FallingEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        for (int i = 0; i < config.getParticleCount() * 2; i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double y = playerLoc.getY() + config.getSpawnHeight() + random.nextDouble() * 10;
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;

            double initialVelocityX = (random.nextDouble() - 0.5) * 0.02;
            double initialVelocityZ = (random.nextDouble() - 0.5) * 0.02;
            double rotationSpeed = random.nextDouble() * 0.15 + 0.05;
            double particleSize = random.nextDouble() * 0.5 + 0.5;
            int particleLifetime = plugin.getConfigManager().getParticleLifetime() + random.nextInt(100);

            new BukkitRunnable() {
                private double currentY = y;
                private double currentX = x;
                private double currentZ = z;
                private double velocityY = 0;
                private double rotation = 0;
                private int ticks = 0;
                private double brightness = 0;
                private boolean brightening = true;

                @Override
                public void run() {
                    if (ticks >= particleLifetime || currentY <= playerLoc.getY() - 5) {
                        this.cancel();
                        return;
                    }

                    velocityY += config.getFallSpeed() * 0.02;
                    currentY -= velocityY;

                    currentX += initialVelocityX * Math.cos(rotation);
                    currentZ += initialVelocityZ * Math.sin(rotation);
                    rotation += rotationSpeed;

                    if (brightening) {
                        brightness += 0.05;
                        if (brightness >= 1.0) {
                            brightness = 1.0;
                            brightening = false;
                        }
                    } else {
                        brightness -= 0.03;
                        if (brightness <= 0.3) {
                            brightness = 0.3;
                            brightening = true;
                        }
                    }

                    Location currentLoc = new Location(player.getWorld(), currentX, currentY, currentZ);

                    ParticleUtils.spawnParticle(player.getWorld(), currentLoc, config.getParticleType());

                    if (ticks % 3 == 0) {
                        player.getWorld().spawnParticle(Particle.END_ROD, currentLoc, 1, 0.1, 0.1, 0.1, 0.01);
                    }

                    if (random.nextDouble() < 0.1) {
                        player.getWorld().spawnParticle(Particle.ENCHANT, currentLoc, 3, 0.3, 0.3, 0.3, 0);
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, random.nextInt(20), 1L);
        }
    }

    @Override
    public void cleanup() {

    }
}