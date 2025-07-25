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

public class FireflyEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public FireflyEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        for (int i = 0; i < config.getParticleCount() * 3; i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double y = playerLoc.getY() + 2 + random.nextDouble() * 15;
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;

            Vector velocity = new Vector(
                    (random.nextDouble() - 0.5) * 0.15,
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.15
            );

            double fireflySize = 0.8 + random.nextDouble() * 0.7;
            int glowPattern = random.nextInt(3);
            Color fireflyColor = generateFireflyColor();

            new BukkitRunnable() {
                private Location currentLoc = new Location(player.getWorld(), x, y, z);
                private Vector currentVelocity = velocity.clone();
                private int ticks = 0;
                private double glowIntensity = random.nextDouble();
                private boolean glowing = true;
                private double wanderAngle = random.nextDouble() * Math.PI * 2;
                private Location targetLocation = generateRandomTarget(playerLoc, config.getSpawnRadius());
                private int targetUpdateCounter = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() * 2) {
                        this.cancel();
                        return;
                    }

                    targetUpdateCounter++;
                    if (targetUpdateCounter >= 60 + random.nextInt(60)) {
                        targetLocation = generateRandomTarget(playerLoc, config.getSpawnRadius());
                        targetUpdateCounter = 0;
                    }

                    Vector toTarget = targetLocation.toVector().subtract(currentLoc.toVector()).normalize().multiply(0.03);
                    currentVelocity.add(toTarget);

                    wanderAngle += (random.nextDouble() - 0.5) * 0.3;
                    double wanderForce = 0.02;
                    currentVelocity.add(new Vector(
                            Math.cos(wanderAngle) * wanderForce,
                            (random.nextDouble() - 0.5) * 0.015,
                            Math.sin(wanderAngle) * wanderForce
                    ));

                    currentVelocity.multiply(0.92);
                    currentLoc.add(currentVelocity);

                    updateGlow(glowPattern);

                    if (glowIntensity > 0.3) {
                        Particle.DustOptions dustOptions = new Particle.DustOptions(fireflyColor, (float)(fireflySize * glowIntensity));
                        player.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0, 0, dustOptions);

                        if (glowIntensity > 0.8) {
                            player.getWorld().spawnParticle(Particle.END_ROD, currentLoc, 0, 0, 0, 0, 0);

                            for (int j = 0; j < 3; j++) {
                                double angle = (Math.PI * 2 / 3) * j + ticks * 0.1;
                                double orbitRadius = 0.3;
                                Location orbitLoc = currentLoc.clone().add(
                                        Math.cos(angle) * orbitRadius,
                                        0,
                                        Math.sin(angle) * orbitRadius
                                );
                                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, orbitLoc, 0, 0, 0, 0, 0);
                            }
                        }
                    }

                    ticks++;
                }

                private void updateGlow(int pattern) {
                    switch (pattern) {
                        case 0:
                            if (glowing) {
                                glowIntensity += 0.04;
                                if (glowIntensity >= 1.0) {
                                    glowIntensity = 1.0;
                                    glowing = false;
                                }
                            } else {
                                glowIntensity -= 0.02;
                                if (glowIntensity <= 0.0) {
                                    glowIntensity = 0.0;
                                    glowing = true;
                                }
                            }
                            break;
                        case 1:
                            glowIntensity = 0.5 + Math.sin(ticks * 0.1) * 0.5;
                            break;
                        case 2:
                            if (ticks % 30 < 10) {
                                glowIntensity = Math.min(1.0, glowIntensity + 0.1);
                            } else {
                                glowIntensity = Math.max(0.0, glowIntensity - 0.05);
                            }
                            break;
                    }
                }

                private Location generateRandomTarget(Location center, double radius) {
                    double targetX = center.getX() + (random.nextDouble() - 0.5) * radius * 2;
                    double targetY = center.getY() + 2 + random.nextDouble() * 15;
                    double targetZ = center.getZ() + (random.nextDouble() - 0.5) * radius * 2;
                    return new Location(center.getWorld(), targetX, targetY, targetZ);
                }

                private Color generateFireflyColor() {
                    int colorType = random.nextInt(4);
                    switch (colorType) {
                        case 0: return Color.fromRGB(255, 255, 150);
                        case 1: return Color.fromRGB(150, 255, 150);
                        case 2: return Color.fromRGB(255, 200, 100);
                        default: return Color.fromRGB(200, 255, 255);
                    }
                }
            }.runTaskTimer(plugin, random.nextInt(40), 1L);
        }
    }

    private Color generateFireflyColor() {
        int colorType = random.nextInt(4);
        switch (colorType) {
            case 0: return Color.fromRGB(255, 255, 150);
            case 1: return Color.fromRGB(150, 255, 150);
            case 2: return Color.fromRGB(255, 200, 100);
            default: return Color.fromRGB(200, 255, 255);
        }
    }

    @Override
    public void cleanup() {

    }
}