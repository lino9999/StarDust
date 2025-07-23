package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
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
        double baseY = playerLoc.getY() + config.getSpawnHeight() * 0.7;

        for (int wave = 0; wave < 3; wave++) {
            final int waveIndex = wave;

            new BukkitRunnable() {
                private double phase = random.nextDouble() * Math.PI * 2;
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime()) {
                        this.cancel();
                        return;
                    }

                    for (int i = 0; i < 10; i++) {
                        double progress = i / 10.0;
                        double x = playerLoc.getX() + (progress - 0.5) * config.getSpawnRadius() * 2;
                        double z = playerLoc.getZ() + (waveIndex - 1) * 10;
                        double y = baseY + Math.sin(phase + progress * Math.PI * 2) * 5;

                        Location particleLoc = new Location(player.getWorld(), x, y, z);

                        player.getWorld().spawnParticle(
                                Particle.valueOf(config.getParticleType()),
                                particleLoc,
                                1,
                                0.2, 0.2, 0.2,
                                0
                        );
                    }

                    phase += 0.1;
                    ticks++;
                }
            }.runTaskTimer(plugin, waveIndex * 10L, 2L);
        }
    }

    @Override
    public void cleanup() {

    }
}