package com.Lino.starDust;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class StarDust extends JavaPlugin {

    private BukkitTask particleTask;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        startParticleTask();
        getLogger().info("StarDust has been enabled!");
    }

    @Override
    public void onDisable() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        getLogger().info("StarDust has been disabled!");
    }

    private void startParticleTask() {
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() != World.Environment.NORMAL) continue;

                    long time = world.getTime();
                    if (time < 13000 || time > 23000) continue;

                    for (Player player : world.getPlayers()) {
                        spawnParticlesAroundPlayer(player);
                    }
                }
            }
        }.runTaskTimer(this, 0L, getConfig().getLong("spawn-interval", 5L));
    }

    private void spawnParticlesAroundPlayer(Player player) {
        Location playerLoc = player.getLocation();
        int particleCount = getConfig().getInt("particles-per-spawn", 3);
        double radius = getConfig().getDouble("spawn-radius", 50.0);
        double maxHeight = getConfig().getDouble("spawn-height", 30.0);

        for (int i = 0; i < particleCount; i++) {
            double x = playerLoc.getX() + (random.nextDouble() - 0.5) * radius * 2;
            double y = playerLoc.getY() + random.nextDouble() * maxHeight;
            double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * radius * 2;

            Location particleLoc = new Location(player.getWorld(), x, y, z);

            new BukkitRunnable() {
                private double currentY = y;
                private final double fallSpeed = getConfig().getDouble("fall-speed", 0.1);
                private final int lifetime = getConfig().getInt("particle-lifetime", 200);
                private int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= lifetime || currentY <= playerLoc.getY() - 5) {
                        this.cancel();
                        return;
                    }

                    currentY -= fallSpeed;
                    Location currentLoc = new Location(player.getWorld(), x, currentY, z);

                    player.getWorld().spawnParticle(
                            Particle.valueOf(getConfig().getString("particle-type", "END_ROD")),
                            currentLoc,
                            1,
                            0, 0, 0,
                            0
                    );

                    ticks++;
                }
            }.runTaskTimer(this, 0L, 1L);
        }
    }
}