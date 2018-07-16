package vip.ourcraft.mcserverplugins.ocprefixes;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class OcPrefixes extends JavaPlugin {
    private static OcPrefixes instance;
    private Settings settings;
    private PrefixManager prefixManager;
    private Chat vaultChat;

    public void onEnable() {
        instance = this;
        this.settings = new Settings();
        this.prefixManager = new PrefixManager();

        if (!setupChat()) {
            getLogger().warning("Vault Chat Hook 失败!");
            setEnabled(false);

            return;
        }

        if (!initFiles()) {
            getLogger().warning("文件(夹) 初始化完败!");
            setEnabled(false);

            return;
        }

        loadConfig();
        getCommand("prefix").setExecutor(new PlayerCommand(this));
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> Bukkit.getScheduler().runTask(instance, new ExpiredPrefixCleanerTask(this)), 0L, 20L);
        getLogger().info("初始化完毕!");
    }

    private boolean initFiles() {
        File playerDataFolder = new File(getDataFolder(), "playerdata");

        if (!playerDataFolder.exists() && !playerDataFolder.mkdirs()) {
            return false;
        }

        return true;
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();

        FileConfiguration config = getConfig();

        settings.setDefaultPrefixName(config.getString("default_prefix_name"));
    }

    public PrefixManager getPrefixManager() {
        return prefixManager;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);

        if (chatProvider != null) {
            vaultChat = chatProvider.getProvider();
        }

        return vaultChat != null;
    }

    public Settings getSettings() {
        return settings;
    }

    public Chat getVaultChat() {
        return vaultChat;
    }

    public static OcPrefixes getInstance() {
        return instance;
    }
}
