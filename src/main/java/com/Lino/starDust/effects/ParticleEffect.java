package com.Lino.starDust.effects;

import com.Lino.starDust.config.BiomeConfig;
import org.bukkit.entity.Player;

public interface ParticleEffect {
    void spawnEffect(Player player, BiomeConfig config);
    void cleanup();
}