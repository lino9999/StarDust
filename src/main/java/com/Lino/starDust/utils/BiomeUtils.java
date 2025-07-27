package com.Lino.starDust.utils;

import org.bukkit.block.Biome;
import org.bukkit.Location;

public class BiomeUtils {

    public static String getBiomeName(Location location) {
        try {
            Biome biome = location.getBlock().getBiome();

            try {
                java.lang.reflect.Method getKeyMethod = biome.getClass().getMethod("getKey");
                Object namespaceKey = getKeyMethod.invoke(biome);
                java.lang.reflect.Method getKeyStringMethod = namespaceKey.getClass().getMethod("getKey");
                return ((String) getKeyStringMethod.invoke(namespaceKey)).toUpperCase();
            } catch (Exception e) {
                return biome.name();
            }
        } catch (Exception e) {
            return "PLAINS";
        }
    }
}