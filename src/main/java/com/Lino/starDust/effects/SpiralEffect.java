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

public class SpiralEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public SpiralEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        for (int i = 0; i < config.getParticleCount() * 2; i++) {
            double startRadius = (config.getSpawnRadius() * 0.1) + (random.nextDouble() * config.getSpawnRadius() * 0.9);
            double startY = playerLoc.getY() + config.getSpawnHeight() + random.nextDouble() * 10;
            double startAngle = random.nextDouble() * Math.PI * 2;
            boolean reverseSpiral = random.nextBoolean();
            double spiralTightness = 0.5 + random.nextDouble() * 1.5;
            Color spiralColor = generateSpiralColor();

            new BukkitRunnable() {
                private double angle = startAngle;
                private double y = startY;
                private double radius = startRadius;
                private int ticks = 0;
                private double rotationSpeed = 0.05 + random.nextDouble() * 0.15;
                private double verticalSpeed = config.getFallSpeed() * (0.5 + random.nextDouble() * 0.5);
                private double radiusDecay = 0.97 + random.nextDouble() * 0.02;
                private double secondaryAngle = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() * 1.5 || y <= playerLoc.getY() - 5 || radius <= 0.1) {
                        this.cancel();
                        return;
                    }

                    if (reverseSpiral) {
                        angle -= rotationSpeed * spiralTightness;
                    } else {
                        angle += rotationSpeed * spiralTightness;
                    }

                    y -= verticalSpeed;
                    radius *= radiusDecay;
                    secondaryAngle += 0.3;

                    double wobbleX = Math.sin(secondaryAngle) * 0.2;
                    double wobbleZ = Math.cos(secondaryAngle) * 0.2;

                    double x = playerLoc.getX() + Math.cos(angle) * radius + wobbleX;
                    double z = playerLoc.getZ() + Math.sin(angle) * radius + wobbleZ;

                    Location currentLoc = new Location(player.getWorld(), x, y, z);

                    Particle.DustOptions mainDust = new Particle.DustOptions(spiralColor, 1.5f);
                    player.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0, 0, mainDust);

                    for (int j = 1; j <= 3; j++) {
                        double trailRadius = radius * (1 + j * 0.1);
                        double trailAngle = angle - (j * 0.1 * (reverseSpiral ? -1 : 1));
                        double trailX = playerLoc.getX() + Math.cos(trailAngle) * trailRadius;
                        double trailY = y + j * 0.5;
                        double trailZ = playerLoc.getZ() + Math.sin(trailAngle) * trailRadius;

                        Location trailLoc = new Location(player.getWorld(), trailX, trailY, trailZ);
                        float trailSize = 1.0f - (j * 0.2f);
                        Particle.DustOptions trailDust = new Particle.DustOptions(fadeColor(spiralColor, j * 0.3f), trailSize);
                        player.getWorld().spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailDust);
                    }

                    if (ticks % 5 == 0) {
                        player.getWorld().spawnParticle(Particle.END_ROD, currentLoc, 0, 0, 0, 0, 0);
                    }

                    if (radius > config.getSpawnRadius() * 0.3 && random.nextDouble() < 0.1) {
                        player.getWorld().spawnParticle(Particle.ENCHANT, currentLoc, 3, 0.5, 0.5, 0.5, 0);
                    }

                    if (ticks % 20 == 0) {
                        createMiniSpiral(currentLoc, angle, radius * 0.3);
                    }

                    rotationSpeed *= 1.01;
                    ticks++;
                }

                private void createMiniSpiral(Location center, double baseAngle, double miniRadius) {
                    new BukkitRunnable() {
                        private int miniTicks = 0;
                        private double miniAngle = baseAngle;

                        @Override
                        public void run() {
                            if (miniTicks >= 20) {
                                this.cancel();
                                return;
                            }

                            miniAngle += 0.5;
                            double miniX = center.getX() + Math.cos(miniAngle) * miniRadius;
                            double miniY = center.getY() - miniTicks * 0.1;
                            double miniZ = center.getZ() + Math.sin(miniAngle) * miniRadius;

                            Location miniLoc = new Location(center.getWorld(), miniX, miniY, miniZ);
                            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, miniLoc, 1, 0, 0, 0, 0);

                            miniTicks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }
            }.runTaskTimer(plugin, i * 2, 1L);
        }
    }

    private Color generateSpiralColor() {
        Color[] colors = {
                Color.fromRGB(100, 200, 255),
                Color.fromRGB(255, 100, 200),
                Color.fromRGB(100, 255, 150),
                Color.fromRGB(255, 200, 100),
                Color.fromRGB(200, 100, 255)
        };
        return colors[random.nextInt(colors.length)];
    }

    private Color fadeColor(Color original, float fadeAmount) {
        int r = (int)(original.getRed() * (1 - fadeAmount));
        int g = (int)(original.getGreen() * (1 - fadeAmount));
        int b = (int)(original.getBlue() * (1 - fadeAmount));
        return Color.fromRGB(Math.max(50, r), Math.max(50, g), Math.max(50, b));
    }

    @Override
    public void cleanup() {

    }
}