package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class SnowEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();
    private double windStrength = 0;
    private double windDirection = 0;

    public SnowEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        updateWind();

        for (int layer = 0; layer < 3; layer++) {
            final int layerIndex = layer;
            int particleCount = config.getParticleCount() * (3 - layer);

            for (int i = 0; i < particleCount; i++) {
                double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2.5;
                double y = playerLoc.getY() + config.getSpawnHeight() + random.nextDouble() * 10;
                double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2.5;

                double swayPhase = random.nextDouble() * Math.PI * 2;
                double snowflakeSize = 0.5 + random.nextDouble() * 1.0;
                double fallSpeedModifier = 0.5 + random.nextDouble() * 0.5;
                boolean isLargeFlake = random.nextDouble() < 0.1;

                new BukkitRunnable() {
                    private double currentX = x;
                    private double currentY = y;
                    private double currentZ = z;
                    private int ticks = 0;
                    private double rotation = 0;
                    private double rotationSpeed = (random.nextDouble() - 0.5) * 0.1;
                    private Vector velocity = new Vector(0, 0, 0);

                    @Override
                    public void run() {
                        if (ticks >= plugin.getConfigManager().getParticleLifetime() * 1.5 || currentY <= playerLoc.getY() - 5) {
                            this.cancel();
                            return;
                        }

                        double layerWindEffect = windStrength * (0.3 + layerIndex * 0.3);
                        velocity.setX(Math.cos(windDirection) * layerWindEffect);
                        velocity.setZ(Math.sin(windDirection) * layerWindEffect);

                        double swayAmount = Math.sin(ticks * 0.05 + swayPhase) * 0.15 * (1 + layerIndex * 0.5);
                        currentX += velocity.getX() + swayAmount;
                        currentZ += velocity.getZ() + Math.cos(ticks * 0.05 + swayPhase) * 0.1;

                        double fallSpeed = config.getFallSpeed() * fallSpeedModifier * (0.3 + layerIndex * 0.2);
                        currentY -= fallSpeed;

                        rotation += rotationSpeed;

                        Location currentLoc = new Location(player.getWorld(), currentX, currentY, currentZ);

                        if (isLargeFlake) {
                            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.WHITE, (float)(snowflakeSize * 2));
                            player.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0, 0, dustOptions);

                            for (int j = 0; j < 6; j++) {
                                double angle = (Math.PI / 3) * j + rotation;
                                double armLength = 0.3;
                                Location armLoc = currentLoc.clone().add(
                                        Math.cos(angle) * armLength,
                                        0,
                                        Math.sin(angle) * armLength
                                );
                                player.getWorld().spawnParticle(Particle.SNOWFLAKE, armLoc, 1, 0, 0, 0, 0);
                            }
                        } else {
                            player.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 1, 0.05, 0.05, 0.05, 0);

                            if (layerIndex == 0 && random.nextDouble() < 0.3) {
                                player.getWorld().spawnParticle(Particle.WHITE_ASH, currentLoc, 1, 0.1, 0.1, 0.1, 0);
                            }
                        }

                        if (random.nextDouble() < 0.02) {
                            player.getWorld().spawnParticle(Particle.FIREWORK, currentLoc, 1, 0, 0, 0, 0);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, i / 3 + layerIndex * 10, 1L);
            }
        }

        spawnSnowFog(player, playerLoc, config);
    }

    private void updateWind() {
        windDirection += (random.nextDouble() - 0.5) * 0.1;
        windStrength = Math.max(0, Math.min(0.3, windStrength + (random.nextDouble() - 0.5) * 0.05));
    }

    private void spawnSnowFog(Player player, Location playerLoc, BiomeConfig config) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < 5; i++) {
                    double fogX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius();
                    double fogY = playerLoc.getY() + random.nextDouble() * 3;
                    double fogZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius();

                    Location fogLoc = new Location(player.getWorld(), fogX, fogY, fogZ);
                    player.getWorld().spawnParticle(Particle.CLOUD, fogLoc, 0, 0.5, 0.1, 0.5, 0.01);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @Override
    public void cleanup() {

    }
}