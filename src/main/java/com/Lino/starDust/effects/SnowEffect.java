package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SnowEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public SnowEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();

        for (int i = 0; i < config.getParticleCount() * 2; i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double y = playerLoc.getY() + random.nextDouble() * config.getSpawnHeight();
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double swayPhase = random.nextDouble() * Math.PI * 2;

            new BukkitRunnable() {
                private double currentX = x;
                private double currentY = y;
                private double currentZ = z;
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime() || currentY <= playerLoc.getY() - 5) {
                        this.cancel();
                        return;
                    }

                    currentY -= config.getFallSpeed() * 0.3;
                    currentX += Math.sin(ticks * 0.05 + swayPhase) * 0.1;
                    currentZ += Math.cos(ticks * 0.05 + swayPhase) * 0.1;

                    Location currentLoc = new Location(player.getWorld(), currentX, currentY, currentZ);

                    ParticleUtils.spawnParticleWithOffset(player.getWorld(), currentLoc, config.getParticleType(), 0.1, 0, 0.1);

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @Override
    public void cleanup() {

    }
}