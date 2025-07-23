package com.Lino.starDust;

import com.Lino.starDust.commands.StarDustCommand;
import com.Lino.starDust.config.ConfigManager;
import com.Lino.starDust.listeners.PlayerJoinListener;
import com.Lino.starDust.managers.EffectManager;
import com.Lino.starDust.managers.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class StarDust extends JavaPlugin {

    private static StarDust instance;
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private EffectManager effectManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        playerManager = new PlayerManager(this);
        effectManager = new EffectManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getCommand("stardust").setExecutor(new StarDustCommand(this));

        effectManager.startEffects();

        getLogger().info("StarDust has been enabled!");
    }

    @Override
    public void onDisable() {
        if (effectManager != null) {
            effectManager.stopEffects();
        }
        getLogger().info("StarDust has been disabled!");
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
}