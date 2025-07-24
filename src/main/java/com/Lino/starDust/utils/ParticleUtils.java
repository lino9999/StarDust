package com.Lino.starDust.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;

public class ParticleUtils {

    public static void spawnParticle(World world, Location location, String particleType) {
        try {
            Particle particle = Particle.valueOf(particleType);

            if (particle == Particle.DUST || particle == Particle.DUST_COLOR_TRANSITION) {
                DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 255, 255), 1.0f);
                world.spawnParticle(particle, location, 1, 0, 0, 0, 0, dustOptions);
            } else if (particle == Particle.ITEM) {
                world.spawnParticle(particle, location, 0, 0, 0, 0, 0);
            } else if (particle == Particle.BLOCK || particle == Particle.FALLING_DUST) {
                BlockData blockData = Material.STONE.createBlockData();
                world.spawnParticle(particle, location, 0, 0, 0, 0, 0, blockData);
            } else if (particle == Particle.BLOCK_MARKER) {
                BlockData blockData = Material.BARRIER.createBlockData();
                world.spawnParticle(particle, location, 0, 0, 0, 0, 0, blockData);
            } else {
                world.spawnParticle(particle, location, 1, 0, 0, 0, 0);
            }

            if (shouldEmitLight(particleType)) {
                emitLight(world, location);
            }

        } catch (Exception e) {
            world.spawnParticle(Particle.END_ROD, location, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnParticleWithOffset(World world, Location location, String particleType,
                                               double offsetX, double offsetY, double offsetZ) {
        try {
            Particle particle = Particle.valueOf(particleType);

            if (particle == Particle.DUST || particle == Particle.DUST_COLOR_TRANSITION) {
                DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 255, 200), 1.5f);
                world.spawnParticle(particle, location, 1, offsetX, offsetY, offsetZ, 0, dustOptions);
            } else if (particle == Particle.ITEM) {
                world.spawnParticle(particle, location, 0, offsetX, offsetY, offsetZ, 0);
            } else if (particle == Particle.BLOCK || particle == Particle.FALLING_DUST) {
                BlockData blockData = Material.STONE.createBlockData();
                world.spawnParticle(particle, location, 0, offsetX, offsetY, offsetZ, 0, blockData);
            } else if (particle == Particle.BLOCK_MARKER) {
                BlockData blockData = Material.BARRIER.createBlockData();
                world.spawnParticle(particle, location, 0, offsetX, offsetY, offsetZ, 0, blockData);
            } else {
                world.spawnParticle(particle, location, 1, offsetX, offsetY, offsetZ, 0);
            }

            if (shouldEmitLight(particleType)) {
                emitLight(world, location);
            }

        } catch (Exception e) {
            world.spawnParticle(Particle.END_ROD, location, 1, offsetX, offsetY, offsetZ, 0);
        }
    }

    private static boolean shouldEmitLight(String particleType) {
        return particleType.equals("END_ROD") ||
                particleType.equals("FLAME") ||
                particleType.equals("SOUL_FIRE_FLAME") ||
                particleType.equals("GLOW") ||
                particleType.equals("DUST") ||
                particleType.equals("FIREWORK") ||
                particleType.equals("SMALL_FLAME") ||
                particleType.equals("GLOW_SQUID_INK") ||
                particleType.equals("ELECTRIC_SPARK") ||
                particleType.equals("SCRAPE") ||
                particleType.equals("WAX_ON") ||
                particleType.equals("WAX_OFF");
    }

    private static void emitLight(World world, Location location) {
        world.spawnParticle(Particle.END_ROD, location, 0, 0, 0, 0, 0);
    }
}