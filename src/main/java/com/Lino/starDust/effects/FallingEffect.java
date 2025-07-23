package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

        for (int i = 0; i < config.getParticleCount(); i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double y = playerLoc.getY() + random.nextDouble() * config.getSpawnHeight();
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;

            new BukkitRunnable() {
                private double currentY = y;
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() || currentY <= playerLoc.getY() - 5) {
                        this.cancel();
                        return;
                    }

                    currentY -= config.getFallSpeed();
                    Location currentLoc = new Location(player.getWorld(), x, currentY, z);

                    player.getWorld().spawnParticle(
                            Particle.valueOf(config.getParticleType()),
                            currentLoc,
                            1,
                            0, 0, 0,
                            0
                    );

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @Override
    public void cleanup() {

    }
}