package com.Lino.starDust.config;

import com.Lino.starDust.StarDust;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ConfigManager {

    private final StarDust plugin;
    private FileConfiguration config;
    private final Map<String, BiomeConfig> biomeConfigs = new HashMap<>();
    private final Set<String> validParticles = new HashSet<>();
    private final Set<String> validEffects = new HashSet<>();

    public ConfigManager(StarDust plugin) {
        this.plugin = plugin;
        initializeValidTypes();
    }

    private void initializeValidTypes() {
        validParticles.addAll(Arrays.asList(
                "END_ROD", "SNOWFLAKE", "FLAME", "SOUL_FIRE_FLAME", "HAPPY_VILLAGER",
                "HEART", "FIREWORK", "GLOW", "DUST", "CLOUD", "DRIPPING_WATER", "DOLPHIN",
                "NAUTILUS", "CRIMSON_SPORE", "WARPED_SPORE", "CHERRY_LEAVES", "SMALL_FLAME",
                "LAVA", "DRAGON_BREATH", "PORTAL", "SPLASH", "CAMPFIRE_COSY_SMOKE",
                "GLOW_SQUID_INK", "ELECTRIC_SPARK", "SCRAPE", "WAX_ON", "WAX_OFF"
        ));

        validEffects.addAll(Arrays.asList(
                "FALLING", "SPIRAL", "WAVE", "FIREFLY", "SNOW", "METEOR", "AURORA", "FLOATING"
        ));
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        validateConfig();
        loadBiomeConfigs();

        plugin.getLogger().info("Loaded " + biomeConfigs.size() + " biome configurations");
    }

    private void validateConfig() {
        boolean hasErrors = false;

        if (config.getInt("max-players") <= 0) {
            plugin.getLogger().warning("Invalid max-players value, using default: 20");
            config.set("max-players", 20);
            hasErrors = true;
        }

        if (config.getLong("spawn-interval") <= 0) {
            plugin.getLogger().warning("Invalid spawn-interval value, using default: 3");
            config.set("spawn-interval", 3L);
            hasErrors = true;
        }

        if (config.getInt("particle-lifetime") <= 0) {
            plugin.getLogger().warning("Invalid particle-lifetime value, using default: 300");
            config.set("particle-lifetime", 300);
            hasErrors = true;
        }

        List<String> enabledWorlds = config.getStringList("enabled-worlds");
        if (enabledWorlds.isEmpty()) {
            plugin.getLogger().warning("No enabled worlds found, adding default world");
            enabledWorlds.add("world");
            config.set("enabled-worlds", enabledWorlds);
            hasErrors = true;
        }

        validateDefaultConfig();

        if (hasErrors) {
            plugin.saveConfig();
        }
    }

    private void validateDefaultConfig() {
        String defaultPath = "default.";

        if (!config.contains(defaultPath + "particle-type")) {
            config.set(defaultPath + "particle-type", "END_ROD");
        }
        if (!config.contains(defaultPath + "fall-speed")) {
            config.set(defaultPath + "fall-speed", 0.08);
        }
        if (!config.contains(defaultPath + "particle-count")) {
            config.set(defaultPath + "particle-count", 5);
        }
        if (!config.contains(defaultPath + "effect-type")) {
            config.set(defaultPath + "effect-type", "FALLING");
        }
        if (!config.contains(defaultPath + "spawn-radius")) {
            config.set(defaultPath + "spawn-radius", 60.0);
        }
        if (!config.contains(defaultPath + "spawn-height")) {
            config.set(defaultPath + "spawn-height", 35.0);
        }
    }

    private void loadBiomeConfigs() {
        biomeConfigs.clear();

        ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
        if (biomesSection == null) {
            plugin.getLogger().warning("No biomes section found in config!");
            return;
        }

        for (String biome : biomesSection.getKeys(false)) {
            String path = "biomes." + biome + ".";

            String particleType = config.getString(path + "particle-type", "END_ROD");
            String effectType = config.getString(path + "effect-type", "FALLING");

            if (!validParticles.contains(particleType)) {
                plugin.getLogger().warning("Invalid particle type '" + particleType +
                        "' for biome " + biome + ", using END_ROD");
                particleType = "END_ROD";
            }

            if (!validEffects.contains(effectType)) {
                plugin.getLogger().warning("Invalid effect type '" + effectType +
                        "' for biome " + biome + ", using FALLING");
                effectType = "FALLING";
            }

            double fallSpeed = Math.max(0.001, Math.min(1.0,
                    config.getDouble(path + "fall-speed", 0.1)));
            int particleCount = Math.max(1, Math.min(50,
                    config.getInt(path + "particle-count", 3)));
            double spawnRadius = Math.max(1.0, Math.min(200.0,
                    config.getDouble(path + "spawn-radius", 50.0)));
            double spawnHeight = Math.max(1.0, Math.min(100.0,
                    config.getDouble(path + "spawn-height", 30.0)));

            BiomeConfig biomeConfig = new BiomeConfig(
                    particleType,
                    fallSpeed,
                    particleCount,
                    effectType,
                    spawnRadius,
                    spawnHeight
            );

            biomeConfigs.put(biome.toUpperCase(), biomeConfig);
        }
    }

    public BiomeConfig getBiomeConfig(String biome) {
        BiomeConfig config = biomeConfigs.get(biome.toUpperCase());
        if (config == null) {
            config = biomeConfigs.get(simplifyBiomeName(biome));
        }
        return config != null ? config : getDefaultBiomeConfig();
    }

    private String simplifyBiomeName(String biome) {
        if (biome.contains("OCEAN")) return "OCEAN";
        if (biome.contains("FOREST")) return "FOREST";
        if (biome.contains("MOUNTAIN") || biome.contains("PEAK")) return "MOUNTAIN";
        if (biome.contains("DESERT") || biome.contains("BADLANDS")) return "DESERT";
        if (biome.contains("SNOW") || biome.contains("ICE") || biome.contains("FROZEN")) return "SNOWY_PLAINS";
        if (biome.contains("JUNGLE")) return "JUNGLE";
        if (biome.contains("SWAMP")) return "SWAMP";
        if (biome.contains("RIVER")) return "RIVER";
        if (biome.contains("BEACH")) return "BEACH";
        if (biome.contains("NETHER")) return "CRIMSON_FOREST";
        if (biome.contains("END")) return "END_HIGHLANDS";
        return biome.toUpperCase();
    }

    private BiomeConfig getDefaultBiomeConfig() {
        return new BiomeConfig(
                config.getString("default.particle-type", "END_ROD"),
                config.getDouble("default.fall-speed", 0.08),
                config.getInt("default.particle-count", 5),
                config.getString("default.effect-type", "FALLING"),
                config.getDouble("default.spawn-radius", 60.0),
                config.getDouble("default.spawn-height", 35.0)
        );
    }

    public int getMaxPlayers() {
        return config.getInt("max-players", 20);
    }

    public long getSpawnInterval() {
        return config.getLong("spawn-interval", 3L);
    }

    public int getParticleLifetime() {
        return config.getInt("particle-lifetime", 300);
    }

    public boolean isWorldEnabled(String worldName) {
        return config.getStringList("enabled-worlds").contains(worldName);
    }

    public boolean checkForUpdates() {
        return config.getBoolean("check-for-updates", true);
    }

    public void reloadWithValidation() {
        long startTime = System.currentTimeMillis();

        plugin.reloadConfig();
        config = plugin.getConfig();

        validateConfig();
        loadBiomeConfigs();

        long loadTime = System.currentTimeMillis() - startTime;
        plugin.getLogger().info("Configuration reloaded and validated in " + loadTime + "ms");
    }

    public Map<String, BiomeConfig> getAllBiomeConfigs() {
        return new HashMap<>(biomeConfigs);
    }

    public void addWorld(String worldName) {
        List<String> worlds = config.getStringList("enabled-worlds");
        if (!worlds.contains(worldName)) {
            worlds.add(worldName);
            config.set("enabled-worlds", worlds);
            plugin.saveConfig();
        }
    }

    public void removeWorld(String worldName) {
        List<String> worlds = config.getStringList("enabled-worlds");
        if (worlds.remove(worldName)) {
            config.set("enabled-worlds", worlds);
            plugin.saveConfig();
        }
    }
}