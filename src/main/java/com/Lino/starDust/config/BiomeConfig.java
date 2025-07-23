package com.Lino.starDust.config;

public class BiomeConfig {

    private final String particleType;
    private final double fallSpeed;
    private final int particleCount;
    private final String effectType;
    private final double spawnRadius;
    private final double spawnHeight;

    public BiomeConfig(String particleType, double fallSpeed, int particleCount,
                       String effectType, double spawnRadius, double spawnHeight) {
        this.particleType = particleType;
        this.fallSpeed = fallSpeed;
        this.particleCount = particleCount;
        this.effectType = effectType;
        this.spawnRadius = spawnRadius;
        this.spawnHeight = spawnHeight;
    }

    public String getParticleType() {
        return particleType;
    }

    public double getFallSpeed() {
        return fallSpeed;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public String getEffectType() {
        return effectType;
    }

    public double getSpawnRadius() {
        return spawnRadius;
    }

    public double getSpawnHeight() {
        return spawnHeight;
    }
}