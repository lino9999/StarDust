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

public class FloatingEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public FloatingEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        createFloatingOrbs(player, playerLoc, config);
        createAmbientMist(player, playerLoc, config);
        createMagicalStreams(player, playerLoc, config);
    }

    private void createFloatingOrbs(Player player, Location playerLoc, BiomeConfig config) {
        for (int i = 0; i < config.getParticleCount() * 2; i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double baseY = playerLoc.getY() + 1 + random.nextDouble() * 10;
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;

            double floatPhase = random.nextDouble() * Math.PI * 2;
            double horizontalPhase = random.nextDouble() * Math.PI * 2;
            double orbitRadius = 1 + random.nextDouble() * 3;
            double floatAmplitude = 1 + random.nextDouble() * 2;
            double rotationSpeed = 0.02 + random.nextDouble() * 0.04;

            Color orbColor = generateOrbColor();
            double orbSize = 0.5 + random.nextDouble();
            boolean hasAura = random.nextDouble() < 0.4;

            new BukkitRunnable() {
                private int ticks = 0;
                private double currentX = x;
                private double currentZ = z;
                private double brightness = random.nextDouble();
                private boolean brightening = random.nextBoolean();
                private Vector drift = new Vector(
                        (random.nextDouble() - 0.5) * 0.01,
                        0,
                        (random.nextDouble() - 0.5) * 0.01
                );

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() * 2) {
                        this.cancel();
                        return;
                    }

                    double y = baseY + Math.sin(ticks * 0.03 + floatPhase) * floatAmplitude;
                    double xOffset = Math.sin(ticks * rotationSpeed + horizontalPhase) * orbitRadius;
                    double zOffset = Math.cos(ticks * rotationSpeed + horizontalPhase) * orbitRadius;

                    currentX += drift.getX();
                    currentZ += drift.getZ();

                    Location currentLoc = new Location(player.getWorld(), currentX + xOffset, y, currentZ + zOffset);

                    if (brightening) {
                        brightness += 0.02;
                        if (brightness >= 1.0) {
                            brightness = 1.0;
                            brightening = false;
                        }
                    } else {
                        brightness -= 0.01;
                        if (brightness <= 0.3) {
                            brightness = 0.3;
                            brightening = true;
                        }
                    }

                    Color currentColor = dimColor(orbColor, brightness);
                    Particle.DustOptions dustOptions = new Particle.DustOptions(currentColor, (float)(orbSize * brightness));
                    player.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0, 0, dustOptions);

                    if (hasAura) {
                        for (int j = 0; j < 4; j++) {
                            double auraAngle = (Math.PI / 2) * j + ticks * 0.1;
                            double auraRadius = orbSize * 0.5;
                            Location auraLoc = currentLoc.clone().add(
                                    Math.cos(auraAngle) * auraRadius,
                                    0,
                                    Math.sin(auraAngle) * auraRadius
                            );

                            Particle.DustOptions auraDust = new Particle.DustOptions(
                                    dimColor(orbColor, brightness * 0.5),
                                    (float)(orbSize * 0.3)
                            );
                            player.getWorld().spawnParticle(Particle.DUST, auraLoc, 1, 0, 0, 0, 0, auraDust);
                        }
                    }

                    ParticleUtils.spawnParticle(player.getWorld(), currentLoc, config.getParticleType());

                    if (brightness > 0.8 && random.nextDouble() < 0.1) {
                        player.getWorld().spawnParticle(Particle.END_ROD, currentLoc, 0, 0, 0, 0, 0);
                    }

                    if (ticks % 40 == 0) {
                        createEnergyPulse(currentLoc, orbColor);
                    }

                    ticks++;
                }

                private void createEnergyPulse(Location center, Color color) {
                    new BukkitRunnable() {
                        private int pulseTicks = 0;
                        private double pulseRadius = 0;

                        @Override
                        public void run() {
                            if (pulseTicks >= 15) {
                                this.cancel();
                                return;
                            }

                            pulseRadius += 0.2;
                            double opacity = 1.0 - (pulseTicks / 15.0);

                            for (int i = 0; i < 8; i++) {
                                double angle = (Math.PI * 2 / 8) * i;
                                Location pulseLoc = center.clone().add(
                                        Math.cos(angle) * pulseRadius,
                                        0,
                                        Math.sin(angle) * pulseRadius
                                );

                                Particle.DustOptions pulseDust = new Particle.DustOptions(
                                        dimColor(color, opacity),
                                        0.5f
                                );
                                player.getWorld().spawnParticle(Particle.DUST, pulseLoc, 1, 0, 0, 0, 0, pulseDust);
                            }

                            pulseTicks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }
            }.runTaskTimer(plugin, i * 2, 1L);
        }
    }

    private void createAmbientMist(Player player, Location playerLoc, BiomeConfig config) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 200) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    double mistX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius();
                    double mistY = playerLoc.getY() + random.nextDouble() * 5;
                    double mistZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius();

                    Location mistLoc = new Location(player.getWorld(), mistX, mistY, mistZ);
                    player.getWorld().spawnParticle(Particle.ENCHANT, mistLoc, 0, 0.5, 0.2, 0.5, 0.02);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void createMagicalStreams(Player player, Location playerLoc, BiomeConfig config) {
        if (random.nextDouble() < 0.5) {
            Location streamStart = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * config.getSpawnRadius(),
                    5 + random.nextDouble() * 5,
                    (random.nextDouble() - 0.5) * config.getSpawnRadius()
            );

            Location streamEnd = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * config.getSpawnRadius(),
                    5 + random.nextDouble() * 5,
                    (random.nextDouble() - 0.5) * config.getSpawnRadius()
            );

            new BukkitRunnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= 30) {
                        this.cancel();
                        return;
                    }

                    double progress = ticks / 30.0;
                    Location currentPos = streamStart.clone().add(
                            streamEnd.clone().subtract(streamStart).multiply(progress)
                    );

                    for (int i = 0; i < 3; i++) {
                        double offset = i * 0.1;
                        Location particlePos = currentPos.clone().add(
                                (random.nextDouble() - 0.5) * 0.2,
                                Math.sin(ticks * 0.3 + i) * 0.2,
                                (random.nextDouble() - 0.5) * 0.2
                        );

                        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, particlePos, 1, 0, 0, 0, 0);
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private Color generateOrbColor() {
        Color[] colors = {
                Color.fromRGB(150, 100, 255),
                Color.fromRGB(100, 150, 255),
                Color.fromRGB(255, 150, 200),
                Color.fromRGB(150, 255, 150),
                Color.fromRGB(255, 200, 100)
        };
        return colors[random.nextInt(colors.length)];
    }

    private Color dimColor(Color original, double brightness) {
        return Color.fromRGB(
                (int)(original.getRed() * brightness),
                (int)(original.getGreen() * brightness),
                (int)(original.getBlue() * brightness)
        );
    }

    @Override
    public void cleanup() {

    }
}