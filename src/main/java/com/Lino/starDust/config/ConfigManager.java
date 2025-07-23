package com.Lino.starDust.config;

import com.Lino.starDust.StarDust;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final StarDust plugin;
    private FileConfiguration config;
    private final Map<String, BiomeConfig> biomeConfigs = new HashMap<>();

    public ConfigManager(StarDust plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadBiomeConfigs();
    }

    private void loadBiomeConfigs() {
        biomeConfigs.clear();

        if (config.getConfigurationSection("biomes") == null) return;

        for (String biome : config.getConfigurationSection("biomes").getKeys(false)) {
            String path = "biomes." + biome + ".";
            BiomeConfig biomeConfig = new BiomeConfig(
                    config.getString(path + "particle-type", "END_ROD"),
                    config.getDouble(path + "fall-speed", 0.1),
                    config.getInt(path + "particle-count", 3),
                    config.getString(path + "effect-type", "FALLING"),
                    config.getDouble(path + "spawn-radius", 50.0),
                    config.getDouble(path + "spawn-height", 30.0)
            );
            biomeConfigs.put(biome.toUpperCase(), biomeConfig);
        }
    }

    public BiomeConfig getBiomeConfig(String biome) {
        return biomeConfigs.getOrDefault(biome.toUpperCase(), getDefaultBiomeConfig());
    }

    private BiomeConfig getDefaultBiomeConfig() {
        return new BiomeConfig(
                config.getString("default.particle-type", "END_ROD"),
                config.getDouble("default.fall-speed", 0.1),
                config.getInt("default.particle-count", 3),
                config.getString("default.effect-type", "FALLING"),
                config.getDouble("default.spawn-radius", 50.0),
                config.getDouble("default.spawn-height", 30.0)
        );
    }

    public int getMaxPlayers() {
        return config.getInt("max-players", 10);
    }

    public long getSpawnInterval() {
        return config.getLong("spawn-interval", 5L);
    }

    public int getParticleLifetime() {
        return config.getInt("particle-lifetime", 200);
    }

    public boolean isWorldEnabled(String worldName) {
        return config.getStringList("enabled-worlds").contains(worldName);
    }
}