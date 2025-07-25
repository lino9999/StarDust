package com.Lino.starDust.effects;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.utils.ParticleUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
        if (random.nextDouble() > 0.4) {
            spawnSmallMeteors(player, config);
        } else {
            spawnLargeMeteor(player, config);
        }
    }

    private void spawnSmallMeteors(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();
        int meteorCount = 2 + random.nextInt(4);

        for (int i = 0; i < meteorCount; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    double startX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;
                    double startY = playerLoc.getY() + config.getSpawnHeight() + random.nextDouble() * 20;
                    double startZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 2;

                    Vector direction = new Vector(
                            (random.nextDouble() - 0.5) * 0.8,
                            -1 - random.nextDouble() * 0.5,
                            (random.nextDouble() - 0.5) * 0.8
                    ).normalize();

                    createMeteor(player, new Location(player.getWorld(), startX, startY, startZ), direction, config, false);
                }
            }.runTaskLater(plugin, i * 10L);
        }
    }

    private void spawnLargeMeteor(Player player, BiomeConfig config) {
        Location playerLoc = player.getLocation();
        double startX = playerLoc.getX() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 1.5;
        double startY = playerLoc.getY() + config.getSpawnHeight() + 30;
        double startZ = playerLoc.getZ() + (random.nextDouble() - 0.5) * config.getSpawnRadius() * 1.5;

        Vector direction = new Vector(
                (random.nextDouble() - 0.5) * 0.5,
                -1.5,
                (random.nextDouble() - 0.5) * 0.5
        ).normalize();

        createMeteor(player, new Location(player.getWorld(), startX, startY, startZ), direction, config, true);
    }

    private void createMeteor(Player player, Location start, Vector direction, BiomeConfig config, boolean isLarge) {
        new BukkitRunnable() {
            private Location currentLoc = start.clone();
            private int ticks = 0;
            private double speed = config.getFallSpeed() * (isLarge ? 4 : 3);
            private boolean exploded = false;

            @Override
            public void run() {
                if (ticks >= 60 || currentLoc.getY() <= player.getLocation().getY() - 5 || exploded) {
                    if (!exploded && isLarge && currentLoc.getY() <= player.getLocation().getY() + 5) {
                        createExplosion(currentLoc);
                    }
                    this.cancel();
                    return;
                }

                currentLoc.add(direction.clone().multiply(speed));
                speed *= 1.02;

                if (isLarge) {
                    for (int i = 0; i < 3; i++) {
                        double offset = i * 0.3;
                        Location coreLoc = currentLoc.clone().subtract(direction.clone().multiply(offset));
                        player.getWorld().spawnParticle(Particle.FLAME, coreLoc, 2, 0.1, 0.1, 0.1, 0.02);
                        player.getWorld().spawnParticle(Particle.LAVA, coreLoc, 1, 0.2, 0.2, 0.2, 0);
                    }
                }

                int trailLength = isLarge ? 15 : 8;
                for (int i = 0; i < trailLength; i++) {
                    Location trailLoc = currentLoc.clone().subtract(direction.clone().multiply(i * 0.4));

                    Color trailColor = interpolateColor(
                            Color.fromRGB(255, 200, 100),
                            Color.fromRGB(255, 100, 0),
                            (float)i / trailLength
                    );

                    Particle.DustOptions dustOptions = new Particle.DustOptions(trailColor, isLarge ? 2.0f : 1.0f);
                    player.getWorld().spawnParticle(Particle.DUST, trailLoc, 1, 0.1, 0.1, 0.1, 0, dustOptions);

                    if (i % 2 == 0) {
                        player.getWorld().spawnParticle(Particle.SMOKE, trailLoc, 1, 0.2, 0.2, 0.2, 0.01);
                    }
                }

                player.getWorld().spawnParticle(Particle.FLAME, currentLoc, 3, 0.2, 0.2, 0.2, 0.05);
                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, currentLoc, 1, 0.3, 0.3, 0.3, 0.02);

                if (random.nextDouble() < 0.3) {
                    player.getWorld().spawnParticle(Particle.FIREWORK, currentLoc, 2, 0.5, 0.5, 0.5, 0.1);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void createExplosion(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);

        for (int i = 0; i < 50; i++) {
            Vector expVec = new Vector(
                    (random.nextDouble() - 0.5) * 2,
                    random.nextDouble() * 1.5,
                    (random.nextDouble() - 0.5) * 2
            ).normalize().multiply(random.nextDouble() * 2);

            Location expLoc = loc.clone().add(expVec);
            loc.getWorld().spawnParticle(Particle.FLAME, expLoc, 1, 0, 0, 0, 0.1);
            loc.getWorld().spawnParticle(Particle.LAVA, expLoc, 1, 0, 0, 0, 0);
        }

        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);
        loc.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, loc, 10, 1, 1, 1, 0.05);
    }

    private Color interpolateColor(Color from, Color to, float ratio) {
        int r = (int)(from.getRed() + (to.getRed() - from.getRed()) * ratio);
        int g = (int)(from.getGreen() + (to.getGreen() - from.getGreen()) * ratio);
        int b = (int)(from.getBlue() + (to.getBlue() - from.getBlue()) * ratio);
        return Color.fromRGB(r, g, b);
    }

    @Override
    public void cleanup() {

    }
}