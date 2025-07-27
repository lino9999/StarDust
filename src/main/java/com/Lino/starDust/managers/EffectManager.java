package com.Lino.starDust.managers;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.effects.*;
import com.Lino.starDust.utils.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EffectManager {

    private final StarDust plugin;
    private BukkitTask mainTask;
    private final Map<String, ParticleEffect> effects = new HashMap<>();
    private final Map<Player, Long> lastEffectTime = new ConcurrentHashMap<>();
    private final Map<Player, Integer> effectIntensity = new ConcurrentHashMap<>();

    public EffectManager(StarDust plugin) {
        this.plugin = plugin;
        registerEffects();
        startPerformanceMonitor();
    }

    private void registerEffects() {
        effects.put("FALLING", new FallingEffect(plugin));
        effects.put("SPIRAL", new SpiralEffect(plugin));
        effects.put("WAVE", new WaveEffect(plugin));
        effects.put("FIREFLY", new FireflyEffect(plugin));
        effects.put("SNOW", new SnowEffect(plugin));
        effects.put("METEOR", new MeteorEffect(plugin));
        effects.put("AURORA", new AuroraEffect(plugin));
        effects.put("FLOATING", new FloatingEffect(plugin));
    }

    public void startEffects() {
        mainTask = new BukkitRunnable() {
            private int tickCounter = 0;

            @Override
            public void run() {
                tickCounter++;

                for (Player player : plugin.getPlayerManager().getActiveParticlePlayers()) {
                    if (!isValidPlayer(player)) continue;

                    World world = player.getWorld();
                    if (!isValidEnvironment(player, world)) continue;

                    long currentTime = System.currentTimeMillis();
                    Long lastTime = lastEffectTime.get(player);

                    if (lastTime != null && currentTime - lastTime < getPlayerEffectDelay(player)) {
                        continue;
                    }

                    String biomeName = BiomeUtils.getBiomeName(player.getLocation());
                    BiomeConfig config = plugin.getConfigManager().getBiomeConfig(biomeName);

                    ParticleEffect effect = effects.get(config.getEffectType());
                    if (effect != null) {
                        adjustEffectIntensity(player);

                        if (shouldSpawnEffect(player, tickCounter)) {
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        effect.spawnEffect(player, config);
                                    });
                                } catch (Exception e) {
                                    plugin.getLogger().warning("Error spawning effect for " + player.getName() + ": " + e.getMessage());
                                }
                            });
                            lastEffectTime.put(player, currentTime);
                        }
                    }
                }

                if (tickCounter % 100 == 0) {
                    cleanupMaps();
                }
            }
        }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getSpawnInterval());
    }

    private void startPerformanceMonitor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                double tps = getServerTPS();

                for (Player player : effectIntensity.keySet()) {
                    if (tps < 18.0) {
                        effectIntensity.put(player, Math.max(1, effectIntensity.get(player) - 1));
                    } else if (tps > 19.5) {
                        effectIntensity.put(player, Math.min(10, effectIntensity.get(player) + 1));
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    private double getServerTPS() {
        return 20.0;
    }

    private void adjustEffectIntensity(Player player) {
        effectIntensity.putIfAbsent(player, 10);

        int playerCount = Bukkit.getOnlinePlayers().size();
        if (playerCount > 20) {
            effectIntensity.put(player, Math.max(3, effectIntensity.get(player) - 1));
        }

        double distance = player.getLocation().distance(player.getWorld().getSpawnLocation());
        if (distance > 1000) {
            effectIntensity.put(player, Math.max(5, effectIntensity.get(player) - 1));
        }
    }

    private boolean shouldSpawnEffect(Player player, int tickCounter) {
        Integer intensity = effectIntensity.get(player);
        if (intensity == null) intensity = 10;

        return tickCounter % (11 - intensity) == 0;
    }

    private long getPlayerEffectDelay(Player player) {
        Integer intensity = effectIntensity.get(player);
        if (intensity == null) intensity = 10;

        return (11 - intensity) * 50L;
    }

    private boolean isValidEnvironment(Player player, World world) {
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            return false;
        }

        if (world.getEnvironment() == World.Environment.NORMAL && !isNightTime(world)) {
            return false;
        }

        if (world.getEnvironment() == World.Environment.NETHER ||
                world.getEnvironment() == World.Environment.THE_END) {
            return true;
        }

        return isNightTime(world);
    }

    private void cleanupMaps() {
        lastEffectTime.entrySet().removeIf(entry ->
                !entry.getKey().isOnline() ||
                        System.currentTimeMillis() - entry.getValue() > 60000
        );

        effectIntensity.entrySet().removeIf(entry -> !entry.getKey().isOnline());
    }

    public void stopEffects() {
        if (mainTask != null) {
            mainTask.cancel();
        }

        for (ParticleEffect effect : effects.values()) {
            effect.cleanup();
        }

        lastEffectTime.clear();
        effectIntensity.clear();
    }

    private boolean isValidPlayer(Player player) {
        return player != null &&
                player.isOnline() &&
                player.getWorld() != null &&
                player.getGameMode() != GameMode.SPECTATOR &&
                !player.isDead();
    }

    private boolean isNightTime(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) return true;
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }
}