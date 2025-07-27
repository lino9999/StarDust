package com.Lino.starDust;

import com.Lino.starDust.commands.StarDustCommand;
import com.Lino.starDust.config.ConfigManager;
import com.Lino.starDust.listeners.PlayerJoinListener;
import com.Lino.starDust.managers.EffectManager;
import com.Lino.starDust.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StarDust extends JavaPlugin {

    private static StarDust instance;
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private EffectManager effectManager;
    private boolean updateAvailable = false;
    private String latestVersion = "";

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;

        printBanner();

        getLogger().info("Initializing StarDust components...");

        configManager = new ConfigManager(this);
        configManager.loadConfig();
        getLogger().info("[✓] Configuration loaded");

        playerManager = new PlayerManager(this);
        getLogger().info("[✓] Player manager initialized");

        effectManager = new EffectManager(this);
        getLogger().info("[✓] Effect manager initialized");

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getLogger().info("[✓] Event listeners registered");

        StarDustCommand commandExecutor = new StarDustCommand(this);
        getCommand("stardust").setExecutor(commandExecutor);
        getCommand("stardust").setTabCompleter(commandExecutor);
        getLogger().info("[✓] Commands registered");

        effectManager.startEffects();
        getLogger().info("[✓] Particle effects started");

        scheduleAutoSave();

        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("StarDust has been enabled! (took " + loadTime + "ms)");

        if (Bukkit.getOnlinePlayers().size() > 0) {
            getLogger().info("Found " + Bukkit.getOnlinePlayers().size() + " online players");
        }
    }

    @Override
    public void onDisable() {
        if (effectManager != null) {
            effectManager.stopEffects();
        }

        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("StarDust has been disabled!");
    }

    private void printBanner() {
        getLogger().info("");
        getLogger().info("  ____  _             ____            _   ");
        getLogger().info(" / ___|| |_ __ _ _ __|  _ \\ _   _ ___| |_ ");
        getLogger().info(" \\___ \\| __/ _` | '__| | | | | | / __| __|");
        getLogger().info("  ___) | || (_| | |  | |_| | |_| \\__ \\ |_ ");
        getLogger().info(" |____/ \\__\\__,_|_|  |____/ \\__,_|___/\\__|");
        getLogger().info("");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: " + getDescription().getAuthors().get(0));
        getLogger().info("");
    }


    private void scheduleAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveConfig();
            }
        }.runTaskTimer(this, 12000L, 12000L);
    }

    public static StarDust getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void reloadPlugin() {
        getLogger().info("Reloading StarDust...");

        effectManager.stopEffects();
        configManager.loadConfig();
        effectManager.startEffects();

        getLogger().info("StarDust reloaded successfully!");
    }
}