package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
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

        for (int i = 0; i < config.getParticleCount(); i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
            double y = playerLoc.getY() + 5 + random.nextDouble() * 10;
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;

            Vector velocity = new Vector(
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.05,
                    (random.nextDouble() - 0.5) * 0.1
            );

            new BukkitRunnable() {
                private Location currentLoc = new Location(player.getWorld(), x, y, z);
                private Vector currentVelocity = velocity.clone();
                private int ticks = 0;
                private int glowTicks = 0;

                @Override
                public void run() {
                    if (ticks >= plugin.getConfigManager().getParticleLifetime()) {
                        this.cancel();
                        return;
                    }

                    currentVelocity.add(new Vector(
                            (random.nextDouble() - 0.5) * 0.02,
                            (random.nextDouble() - 0.5) * 0.01,
                            (random.nextDouble() - 0.5) * 0.02
                    ));
                    currentVelocity.multiply(0.95);

                    currentLoc.add(currentVelocity);

                    glowTicks++;
                    if (glowTicks > 20 && glowTicks < 40) {
                        player.getWorld().spawnParticle(
                                Particle.valueOf(config.getParticleType()),
                                currentLoc,
                                1,
                                0, 0, 0,
                                0
                        );
                    }
                    if (glowTicks >= 60) glowTicks = 0;

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @Override
    public void cleanup() {

    }
}