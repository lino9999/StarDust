package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class AuroraEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public AuroraEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();
        double baseY = playerLoc.getY() + config.getSpawnHeight() * 0.8;

        for (int wave = 0; wave < 5; wave++) {
            final int waveIndex = wave;
            Color[] colors = {
                    Color.fromRGB(0, 255, 100),
                    Color.fromRGB(0, 200, 255),
                    Color.fromRGB(150, 50, 255),
                    Color.fromRGB(255, 0, 150),
                    Color.fromRGB(100, 255, 200)
            };
            final Color waveColor = colors[wave % colors.length];

            new BukkitRunnable() {
                private double phase = random.nextDouble() * Math.PI * 2;
                private int ticks = 0;
                private double waveAmplitude = 5 + random.nextDouble() * 5;
                private double frequency = 0.5 + random.nextDouble() * 0.5;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() * 2) {
                        this.cancel();
                        return;
                    }

                    for (int i = 0; i < 30; i++) {
                        double progress = i / 30.0;
                        double x = playerLoc.getX() + (progress - 0.5) * config.getSpawnRadius() * 2.5;
                        double z = playerLoc.getZ() + (waveIndex - 2) * 8;

                        double y1 = baseY + Math.sin(phase + progress * Math.PI * 2 * frequency) * waveAmplitude;
                        double y2 = y1 + Math.sin(phase * 2 + progress * Math.PI * 3) * 3;

                        double shimmer = Math.sin(ticks * 0.3 + i * 0.5) * 0.5 + 0.5;

                        Location particleLoc = new Location(player.getWorld(), x, y1, z);
                        Location particleLoc2 = new Location(player.getWorld(), x, y2, z);

                        Particle.DustOptions dustOptions = new Particle.DustOptions(waveColor, (float)(1.5 + shimmer));
                        player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.5, 0.1, 0, dustOptions);

                        if (i % 2 == 0) {
                            player.getWorld().spawnParticle(Particle.DUST, particleLoc2, 1, 0.2, 0.3, 0.2, 0, dustOptions);
                        }

                        if (random.nextDouble() < 0.05) {
                            player.getWorld().spawnParticle(Particle.FIREWORK, particleLoc, 1, 0.5, 0.5, 0.5, 0.02);
                        }
                    }

                    for (int j = 0; j < 5; j++) {
                        double starX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
                        double starY = baseY + (random.nextDouble() - 0.5) * 20;
                        double starZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * 30;
                        Location starLoc = new Location(player.getWorld(), starX, starY, starZ);

                        player.getWorld().spawnParticle(Particle.END_ROD, starLoc, 0, 0, 0, 0, 0);
                    }

                    phase += 0.08;
                    waveAmplitude = 5 + Math.sin(ticks * 0.02) * 3;
                    ticks++;
                }
            }.runTaskTimer(plugin, waveIndex * 5L, 1L);
        }
    }

    @Override
    public void cleanup() {

    }
}