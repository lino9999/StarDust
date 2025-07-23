package com.Lino.starDust.managers;

import com.Lino.starDust.StarDust;
import com.Lino.starDust.config.BiomeConfig;
import com.Lino.starDust.effects.*;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class EffectManager {

    private final StarDust plugin;
    private BukkitTask mainTask;
    private final Map<String, ParticleEffect> effects = new HashMap<>();

    public EffectManager(StarDust plugin) {
        this.plugin = plugin;
        registerEffects();
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
            @Override
            public void run() {
                for (Player player : plugin.getPlayerManager().getActiveParticlePlayers()) {
                    if (!isValidPlayer(player)) continue;

                    World world = player.getWorld();
                    if (!isNightTime(world) || !plugin.getConfigManager().isWorldEnabled(world.getName())) continue;

                    String biomeName = player.getLocation().getBlock().getBiome().name();
                    BiomeConfig config = plugin.getConfigManager().getBiomeConfig(biomeName);

                    ParticleEffect effect = effects.get(config.getEffectType());
                    if (effect != null) {
                        effect.spawnEffect(player, config);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getSpawnInterval());
    }

    public void stopEffects() {
        if (mainTask != null) {
            mainTask.cancel();
        }

        for (ParticleEffect effect : effects.values()) {
            effect.cleanup();
        }
    }

    private boolean isValidPlayer(Player player) {
        return player != null && player.isOnline() && player.getWorld() != null;
    }

    private boolean isNightTime(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) return false;
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }
}