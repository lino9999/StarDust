package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class MeteorEffect implements ParticleEffect {

    private final StarDust plugin;
    private final Random random = new Random();

    public MeteorEffect(StarDust plugin) {
        this.plugin = plugin;
    }

    @Override
    public void spawnEffect(Player player, BiomeConfig config) {
        if (random.nextDouble() > 0.3) return;

        Location playerLoc = player.getLocation();
        double startX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
        double startY = playerLoc.getY() + config.getSpawnHeight();
        double startZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;

        Vector direction = new Vector(
                (random.nextDouble() - 0.5) * 0.5,
                -1,
                (random.nextDouble() - 0.5) * 0.5
        ).normalize();

        new BukkitRunnable() {
            private Location currentLoc = new Location(player.getWorld(), startX, startY, startZ);
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40 || currentLoc.getY() <= playerLoc.getY() - 5) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < 5; i++) {
                    Location trailLoc = currentLoc.clone().subtract(direction.clone().multiply(i * 0.5));
                    ParticleUtils.spawnParticle(player.getWorld(), trailLoc, config.getParticleType());
                }

                currentLoc.add(direction.clone().multiply(config.getFallSpeed() * 3));
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void cleanup() {

    }
}