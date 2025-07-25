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

public class WaveEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public WaveEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        createMainWave(player, playerLoc, config);
        createSecondaryWaves(player, playerLoc, config);
        createWaveSplash(player, playerLoc, config);
    }

    private void createMainWave(Player player, Location playerLoc, BiomeConfig config) {
        for (int i = 0; i < config.getParticleCount(); i++) {
            double startX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double startY = playerLoc.getY() + config.getSpawnHeight() + random.nextDouble() * 5;
            double startZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double wavePhase = random.nextDouble() * Math.PI * 2;
            double waveFrequency = 0.5 + random.nextDouble() * 1.0;
            double waveAmplitude = 2 + random.nextDouble() * 3;
            boolean isCrestedWave = random.nextDouble() < 0.3;

            new BukkitRunnable() {
                private double x = startX;
                private double y = startY;
                private double z = startZ;
                private int ticks = 0;
                private double currentPhase = wavePhase;
                private double turbulence = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() || y <= playerLoc.getY() - 5) {
                        this.cancel();
                        return;
                    }

                    y -= config.getFallSpeed();
                    currentPhase += 0.1 * waveFrequency;
                    turbulence = Math.sin(ticks * 0.2) * 0.5;

                    double primaryWave = Math.sin(currentPhase) * waveAmplitude;
                    double secondaryWave = Math.sin(currentPhase * 2.5) * (waveAmplitude * 0.3);
                    double waveOffset = primaryWave + secondaryWave + turbulence;

                    Location currentLoc = new Location(player.getWorld(), x + waveOffset, y, z);

                    if (isCrestedWave && Math.abs(Math.sin(currentPhase)) > 0.8) {
                        for (int j = 0; j < 3; j++) {
                            Location foamLoc = currentLoc.clone().add(
                                    (random.nextDouble() - 0.5) * 0.5,
                                    random.nextDouble() * 0.3,
                                    (random.nextDouble() - 0.5) * 0.5
                            );
                            player.getWorld().spawnParticle(Particle.CLOUD, foamLoc, 0, 0, 0, 0, 0);
                        }
                        player.getWorld().spawnParticle(Particle.SPLASH, currentLoc, 2, 0.2, 0.1, 0.2, 0);
                    }

                    Color waveColor = interpolateWaveColor(Math.abs(Math.sin(currentPhase)));
                    Particle.DustOptions dustOptions = new Particle.DustOptions(waveColor, 1.5f);
                    player.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0, 0, dustOptions);

                    player.getWorld().spawnParticle(Particle.DRIPPING_WATER, currentLoc, 1, 0.1, 0.1, 0.1, 0);

                    for (int k = 1; k <= 3; k++) {
                        double trailOffset = waveOffset - (k * waveAmplitude * 0.2);
                        Location trailLoc = new Location(player.getWorld(), x + trailOffset, y + k * 0.3, z);
                        float alpha = 1.0f - (k * 0.3f);
                        Color trailColor = Color.fromRGB(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue());
                        Particle.DustOptions trailDust = new Particle.DustOptions(trailColor, 1.0f);
                        player.getWorld().spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailDust);
                    }

                    if (random.nextDouble() < 0.05) {
                        player.getWorld().spawnParticle(Particle.DOLPHIN, currentLoc, 1, 0, 0, 0, 0);
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, i * 3, 1L);
        }
    }

    private void createSecondaryWaves(Player player, Location playerLoc, BiomeConfig config) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < 360; i += 30) {
                    double angle = Math.toRadians(i);
                    double radius = 10 + Math.sin(ticks * 0.1) * 5;

                    double x = playerLoc.getX() + Math.cos(angle) * radius;
                    double y = playerLoc.getY() + 1 + Math.sin(ticks * 0.15 + angle) * 2;
                    double z = playerLoc.getZ() + Math.sin(angle) * radius;

                    Location rippleLoc = new Location(player.getWorld(), x, y, z);
                    player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, rippleLoc, 1, 0, 0, 0, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void createWaveSplash(Player player, Location playerLoc, BiomeConfig config) {
        if (random.nextDouble() < 0.3) {
            double splashX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius();
            double splashY = playerLoc.getY() + 2;
            double splashZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius();
            Location splashLoc = new Location(player.getWorld(), splashX, splashY, splashZ);

            new BukkitRunnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= 20) {
                        this.cancel();
                        return;
                    }

                    for (int i = 0; i < 10; i++) {
                        double angle = (Math.PI * 2 / 10) * i;
                        double distance = ticks * 0.3;
                        double dropHeight = 2 - (ticks * 0.1);

                        Location dropLoc = splashLoc.clone().add(
                                Math.cos(angle) * distance,
                                dropHeight,
                                Math.sin(angle) * distance
                        );

                        player.getWorld().spawnParticle(Particle.DRIPPING_WATER, dropLoc, 1, 0, -0.1, 0, 0);

                        if (ticks < 5) {
                            player.getWorld().spawnParticle(Particle.SPLASH, dropLoc, 1, 0, 0, 0, 0);
                        }
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private Color interpolateWaveColor(double intensity) {
        Color deepWater = Color.fromRGB(0, 100, 200);
        Color lightWater = Color.fromRGB(100, 200, 255);

        int r = (int)(deepWater.getRed() + (lightWater.getRed() - deepWater.getRed()) * intensity);
        int g = (int)(deepWater.getGreen() + (lightWater.getGreen() - deepWater.getGreen()) * intensity);
        int b = (int)(deepWater.getBlue() + (lightWater.getBlue() - deepWater.getBlue()) * intensity);

        return Color.fromRGB(r, g, b);
    }

    @Override
    public void cleanup() {

    }
}