package com.Lino.starDust.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ParticleUtils {

    private static final Random random = new Random();

    public static void spawnParticle(World world, Location location, String particleType) {
        try {
            Particle particle = Particle.valueOf(particleType);

            if (particle == Particle.DUST || particle == Particle.DUST_COLOR_TRANSITION) {
                Color color = generateDynamicColor();
                DustOptions dustOptions = new DustOptions(color, 1.0f + random.nextFloat() * 0.5f);
                world.spawnParticle(particle, location, 1, 0, 0, 0, 0, dustOptions);
            } else if (particle == Particle.ITEM) {
                Material[] materials = {Material.DIAMOND, Material.EMERALD, Material.GOLD_NUGGET, Material.GLOWSTONE_DUST};
                ItemStack item = new ItemStack(materials[random.nextInt(materials.length)]);
                world.spawnParticle(particle, location, 0, 0, 0, 0, 0, item);
            } else if (particle == Particle.BLOCK || particle == Particle.FALLING_DUST) {
                Material[] blocks = {Material.GLOWSTONE, Material.SEA_LANTERN, Material.END_STONE, Material.QUARTZ_BLOCK};
                BlockData blockData = blocks[random.nextInt(blocks.length)].createBlockData();
                world.spawnParticle(particle, location, 0, 0, 0, 0, 0, blockData);
            } else if (particle == Particle.BLOCK_MARKER) {
                BlockData blockData = Material.LIGHT.createBlockData();
                world.spawnParticle(particle, location, 0, 0, 0, 0, 0, blockData);
            } else {
                world.spawnParticle(particle, location, 1, 0, 0, 0, 0);
            }

            if (shouldEmitLight(particleType)) {
                emitLight(world, location);
            }

            if (shouldAddSparkles(particleType) && random.nextDouble() < 0.3) {
                addSparkleEffect(world, location);
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
                Color color = generateDynamicColor();
                DustOptions dustOptions = new DustOptions(color, 1.5f + random.nextFloat());
                world.spawnParticle(particle, location, 1, offsetX, offsetY, offsetZ, 0, dustOptions);
            } else if (particle == Particle.ITEM) {
                Material[] materials = {Material.PRISMARINE_CRYSTALS, Material.NETHER_STAR, Material.ECHO_SHARD};
                ItemStack item = new ItemStack(materials[random.nextInt(materials.length)]);
                world.spawnParticle(particle, location, 0, offsetX, offsetY, offsetZ, 0, item);
            } else if (particle == Particle.BLOCK || particle == Particle.FALLING_DUST) {
                Material[] blocks = {Material.BEACON, Material.CRYING_OBSIDIAN, Material.AMETHYST_BLOCK};
                BlockData blockData = blocks[random.nextInt(blocks.length)].createBlockData();
                world.spawnParticle(particle, location, 0, offsetX, offsetY, offsetZ, 0, blockData);
            } else if (particle == Particle.BLOCK_MARKER) {
                BlockData blockData = Material.LIGHT.createBlockData();
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

    public static void spawnColorTransition(World world, Location location, Color fromColor, Color toColor, float size) {
        DustTransition dustTransition = new DustTransition(fromColor, toColor, size);
        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 1, 0, 0, 0, 0, dustTransition);
    }

    public static void spawnRainbowParticle(World world, Location location, float size) {
        double time = System.currentTimeMillis() / 1000.0;
        int r = (int)(Math.sin(time) * 127 + 128);
        int g = (int)(Math.sin(time + 2.094) * 127 + 128);
        int b = (int)(Math.sin(time + 4.189) * 127 + 128);

        Color rainbowColor = Color.fromRGB(r, g, b);
        DustOptions dustOptions = new DustOptions(rainbowColor, size);
        world.spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0, dustOptions);
    }

    private static Color generateDynamicColor() {
        double time = System.currentTimeMillis() / 5000.0;
        int baseR = 200 + (int)(Math.sin(time) * 55);
        int baseG = 200 + (int)(Math.sin(time + 2) * 55);
        int baseB = 200 + (int)(Math.sin(time + 4) * 55);

        return Color.fromRGB(
                Math.min(255, Math.max(100, baseR + random.nextInt(50) - 25)),
                Math.min(255, Math.max(100, baseG + random.nextInt(50) - 25)),
                Math.min(255, Math.max(100, baseB + random.nextInt(50) - 25))
        );
    }

    private static void addSparkleEffect(World world, Location location) {
        for (int i = 0; i < 3; i++) {
            Location sparkleLocation = location.clone().add(
                    (random.nextDouble() - 0.5) * 0.5,
                    random.nextDouble() * 0.5,
                    (random.nextDouble() - 0.5) * 0.5
            );
            world.spawnParticle(Particle.FIREWORK, sparkleLocation, 0, 0, 0, 0, 0);
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
                particleType.equals("WAX_OFF") ||
                particleType.equals("LAVA") ||
                particleType.equals("NAUTILUS") ||
                particleType.equals("DRAGON_BREATH");
    }

    private static boolean shouldAddSparkles(String particleType) {
        return particleType.equals("END_ROD") ||
                particleType.equals("DUST") ||
                particleType.equals("HAPPY_VILLAGER") ||
                particleType.equals("ENCHANT") ||
                particleType.equals("CRIT_MAGIC");
    }

    private static void emitLight(World world, Location location) {
        if (random.nextDouble() < 0.7) {
            world.spawnParticle(Particle.END_ROD, location, 0, 0, 0, 0, 0);
        }

        if (random.nextDouble() < 0.3) {
            world.spawnParticle(Particle.GLOW, location, 1, 0.2, 0.2, 0.2, 0);
        }
    }

    public static void createParticleRing(World world, Location center, double radius, Particle particle, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(world, x, center.getY(), z);
            world.spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
        }
    }

    public static void createParticleSphere(World world, Location center, double radius, Particle particle, int density) {
        for (double phi = 0; phi <= Math.PI; phi += Math.PI / density) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / density) {
                double x = radius * Math.sin(phi) * Math.cos(theta);
                double y = radius * Math.cos(phi);
                double z = radius * Math.sin(phi) * Math.sin(theta);

                Location particleLocation = center.clone().add(x, y, z);
                world.spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
            }
        }
    }
}