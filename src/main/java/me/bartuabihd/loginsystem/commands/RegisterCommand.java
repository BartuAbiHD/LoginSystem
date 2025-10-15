package me.bartuabihd.loginsystem.commands;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.api.v2.event.RegisterEvent;
import me.bartuabihd.loginsystem.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RegisterCommand implements CommandExecutor {

    private final LoginSystem plugin;

    public RegisterCommand(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("error.not_a_player"));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getAuthManager().isPlayerLoggedIn(player.getUniqueId())) {
            player.sendMessage(plugin.getLocaleManager().getMessage("error.already_logged_in"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getLocaleManager().getMessage("register.usage"));
            return true;
        }

        // --- Senkron Kontroller (Veritabanı gerektirmeyen hızlı kontroller) ---

        String username = player.getName();
        int minNickLength = plugin.getConfig().getInt("settings.restrictions.minNicknameLength", 3);
        int maxNickLength = plugin.getConfig().getInt("settings.restrictions.maxNicknameLength", 16);
        String nameRegex = plugin.getConfig().getString("settings.restrictions.allowedNicknameCharacters", "[a-zA-Z0-9_]*");

        if (username.length() < minNickLength || username.length() > maxNickLength) {
            player.sendMessage(plugin.getLocaleManager().getMessage("register.nickname_length",
                    "{min}", String.valueOf(minNickLength),
                    "{max}", String.valueOf(maxNickLength))
            );
            return true;
        }
        if (!username.matches(nameRegex)) {
            player.sendMessage(plugin.getLocaleManager().getMessage("register.nickname_invalid_chars"));
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        if (!password.equals(confirmPassword)) {
            player.sendMessage(plugin.getLocaleManager().getMessage("register.password_mismatch"));
            return true;
        }

        // Şifre kurallarını kontrol et (uzunluk, yasaklı listesi vb.)
        if (isPasswordInvalid(player, password)) {
            return true;
        }

        String passwordRegex = plugin.getConfig().getString("settings.restrictions.allowedPasswordCharacters", "[!-~]*");
        if (!password.matches(passwordRegex)) {
            player.sendMessage(plugin.getLocaleManager().getMessage("register.password_invalid_chars"));
            return true;
        }

        // --- Asenkron Kontroller (Veritabanı sorguları burada başlar) ---

        // 1. Oyuncunun zaten kayıtlı olup olmadığını kontrol et
        plugin.getStorage().isPlayerRegistered(player.getUniqueId())
                .thenCompose(isRegistered -> {
                    if (isRegistered) {
                        player.sendMessage(plugin.getLocaleManager().getMessage("register.already_registered"));
                        return CompletableFuture.completedFuture(null); // Zinciri durdur
                    }

                    // 2. IP başına kayıt limitini kontrol et
                    String ipAddress = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "UNKNOWN";
                    int maxReg = plugin.getConfig().getInt("settings.restrictions.maxRegPerIp", 2);

                    if (maxReg <= 0) {
                        return CompletableFuture.completedFuture(ipAddress); // IP kontrolü kapalı, zincire devam et
                    }

                    return plugin.getStorage().countRegistrationsByIp(ipAddress).thenApply(count -> {
                        if (count >= maxReg) {
                            player.sendMessage(plugin.getLocaleManager().getMessage("register.ip_limit"));
                            return null; // IP limitine takıldı, zinciri durdur
                        }
                        return ipAddress; // IP kontrolü başarılı, zincire devam et
                    });
                })
                .thenAccept(ipAddress -> {
                    if (ipAddress == null) {
                        return; // Önceki adımlardan birinde zincir durduruldu
                    }

                    // Tüm kontroller başarılı, kayıt işlemini gerçekleştir
                    String hashedPassword = plugin.getHashingManager().hash(password);
                    PlayerData data = new PlayerData(player.getUniqueId(), player.getName(), hashedPassword, ipAddress);

                    plugin.getStorage().registerPlayer(data).thenRun(() -> {

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.cancelTimeoutTask(player);
                                plugin.getAuthManager().loginPlayer(player.getUniqueId());
                                Bukkit.getPluginManager().callEvent(new RegisterEvent(player));
                                player.sendMessage(plugin.getLocaleManager().getMessage("register.success"));
                            }
                        }.runTask(plugin);

                    });
                });

        return true;
    }

    /**
     * Şifrenin güvenlik kurallarına (uzunluk, yasaklı kelimeler) uyup uymadığını kontrol eder.
     * @param player Komutu kullanan oyuncu
     * @param password Kontrol edilecek şifre
     * @return Şifre geçersizse true, geçerliyse false döner.
     */
    private boolean isPasswordInvalid(Player player, String password) {
        int minLength = plugin.getConfig().getInt("settings.security.minPasswordLength", 6);
        int maxLength = plugin.getConfig().getInt("settings.security.passwordMaxLength", 32);
        List<String> unsafePasswords = plugin.getConfig().getStringList("settings.security.unsafePasswords");

        if (password.length() < minLength || password.length() > maxLength) {
            player.sendMessage(plugin.getLocaleManager().getMessage("register.password_length",
                    "{min}", String.valueOf(minLength),
                    "{max}", String.valueOf(maxLength))
            );
            return true;
        }

        if (unsafePasswords.stream().anyMatch(unsafe -> unsafe.equalsIgnoreCase(password))) {
            player.sendMessage(plugin.getLocaleManager().getMessage("register.password_unsafe"));
            return true;
        }

        return false;
    }
}