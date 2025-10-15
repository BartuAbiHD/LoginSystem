package me.bartuabihd.loginsystem;

import me.bartuabihd.loginsystem.api.ApiImplementation;
import me.bartuabihd.loginsystem.api.v2.LoginSystemAPI;
import me.bartuabihd.loginsystem.commands.*;
import me.bartuabihd.loginsystem.listeners.PlayerSessionListener;
import me.bartuabihd.loginsystem.manager.AuthManager;
import me.bartuabihd.loginsystem.manager.HashingManager;
import me.bartuabihd.loginsystem.manager.LocaleManager;
import me.bartuabihd.loginsystem.storage.IStorage;
import me.bartuabihd.loginsystem.storage.StorageFactory;
import me.bartuabihd.loginsystem.util.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginSystem extends JavaPlugin {

    private static LoginSystem instance;
    private LoginSystemAPI api;

    private IStorage storage;
    private AuthManager authManager;
    private HashingManager hashingManager;
    private LocaleManager localeManager;
    private final Map<UUID, BukkitTask> timeoutTasks = new HashMap<>();

    private String latestVersion;
    private boolean updateAvailable = false;

    @Override
    public void onEnable() {
        instance = this; // API'ye statik erişim için
        saveDefaultConfig();

        // API'yi başlat
        this.api = new ApiImplementation(this);

        this.localeManager = new LocaleManager(this);

        this.authManager = new AuthManager();
        this.hashingManager = new HashingManager(this);

        this.storage = StorageFactory.createStorage(this);
        if (this.storage == null) {
            getLogger().severe("Unsupported database type! Plugin is being disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.storage.connect();

        registerCommands();
        registerListeners();

        // Güncelleme kontrolünü başlat
        if (getConfig().getBoolean("settings.update_checker.enabled", true)) {
            checkForUpdates();
        }

        getLogger().info("LoginSystem has been enabled!");
    }

    private void checkForUpdates() {
        new UpdateChecker(this, 74894).getLatestVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.latestVersion = version;
                this.updateAvailable = true;

                // Konsola bildirim gönder
                getLogger().info(getLocaleManager().getMessage("update.available_console",
                        "{new_version}", latestVersion,
                        "{current_version}", this.getDescription().getVersion()
                ));
            }
        });
    }

    // Bu metotlara PlayerSessionListener'dan erişeceğiz
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    @Override
    public void onDisable() {
        if (this.storage != null) {
            this.storage.disconnect();
        }
        getLogger().info("LoginSystem has been disabled!");

        this.api = null;
        instance = null;
    }

    private void registerCommands() {
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("logout").setExecutor(new LogoutCommand(this));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(this));
        getCommand("unregister").setExecutor(new UnregisterCommand(this));
        getCommand("email").setExecutor(new EmailCommand(this));
        getCommand("loginsystem").setExecutor(new LoginSystemAdminCommand(this));
    }

    /**
     * Eklentinin yapılandırmasını ve yöneticilerini yeniden yükler.
     */
    public void performReload() {
        // Bukkit'in kendi reloadConfig metodu ayarları hafızaya yeniden yükler.
        reloadConfig();

        // Dil ve şifreleme yöneticilerini yeni ayarlarla yeniden başlat.
        this.localeManager = new LocaleManager(this);
        this.hashingManager = new HashingManager(this);
        this.localeManager.loadMessages();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);
    }

    public void startTimeoutTask(Player player) {
        int timeoutSeconds = getConfig().getInt("settings.restrictions.timeout", 30);
        if (timeoutSeconds <= 0) {
            return; // Zaman aşımı devre dışı.
        }

        BukkitTask task = getServer().getScheduler().runTaskLater(this, () -> {
            if (player.isOnline() && !getAuthManager().isPlayerLoggedIn(player.getUniqueId())) {
                player.kickPlayer(this.getLocaleManager().getMessage("join.kick_timeout"));
            }
        }, timeoutSeconds * 20L); // Saniyeyi tick'e çevir (20 tick = 1 saniye)

        timeoutTasks.put(player.getUniqueId(), task);
    }

    public void cancelTimeoutTask(Player player) {
        BukkitTask task = timeoutTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * LoginSystem API v1'e statik erişim sağlar.
     * @return LoginSystemAPI'nin bir örneği.
     */
    public static LoginSystemAPI getApi() {
        if (instance == null || instance.api == null) {
            // Eklenti düzgün yüklenmediyse veya devre dışıysa null dönebilir.
            // Bu, diğer eklentilerin hata almasını önler.
            throw new IllegalStateException("LoginSystemAPI is not available. Is the plugin enabled?");
        }
        return instance.api;
    }

    public IStorage getStorage() {
        return storage;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public HashingManager getHashingManager() {
        return hashingManager;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

}