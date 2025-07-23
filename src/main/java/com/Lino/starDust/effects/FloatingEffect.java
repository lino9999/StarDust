package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

        for (int i = 0; i < config.getParticleCount(); i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double baseY = playerLoc.getY() + 2 + random.nextDouble() * 8;
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double floatPhase = random.nextDouble() * Math.PI * 2;
            double horizontalPhase = random.nextDouble() * Math.PI * 2;

            new BukkitRunnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime()) {
                        this.cancel();
                        return;
                    }

                    double y = baseY + Math.sin(ticks * 0.05 + floatPhase) * 1.5;
                    double xOffset = Math.sin(ticks * 0.03 + horizontalPhase) * 2;
                    double zOffset = Math.cos(ticks * 0.03 + horizontalPhase) * 2;

                    Location currentLoc = new Location(player.getWorld(), x + xOffset, y, z + zOffset);

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