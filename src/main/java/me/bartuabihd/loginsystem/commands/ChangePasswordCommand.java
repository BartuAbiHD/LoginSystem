package me.bartuabihd.loginsystem.commands;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.manager.HashingManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChangePasswordCommand implements CommandExecutor {

    private final LoginSystem plugin;

    public ChangePasswordCommand(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("error.not_a_player"));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getAuthManager().isPlayerLoggedIn(player.getUniqueId())) {
            player.sendMessage(plugin.getLocaleManager().getMessage("error.not_logged_in"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getLocaleManager().getMessage("changepass.usage"));
            return true;
        }

        // --- Senkron Kontroller ---

        String oldPassword = args[0];
        String newPassword = args[1];

        // Yeni şifrenin güvenlik kurallarına uyup uymadığını kontrol et
        if (isPasswordInvalid(player, newPassword)) {
            return true;
        }

        String passwordRegex = plugin.getConfig().getString("settings.restrictions.allowedPasswordCharacters", "[!-~]*");
        if (!newPassword.matches(passwordRegex)) {
            player.sendMessage(plugin.getLocaleManager().getMessage("changepass.password_invalid_chars"));
            return true;
        }

        // --- Asenkron İşlemler ---

        plugin.getStorage().getPlayerData(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData == null) {
                // Bu durum normalde olmamalı, çünkü oyuncu zaten login durumda.
                player.sendMessage(plugin.getLocaleManager().getMessage("error.generic", "{error}", plugin.getLocaleManager().getMessage("error.player_data_not_found")));
                return;
            }

            // HashingManager'ı kullanarak eski şifrenin doğruluğunu kontrol et.
            // Bu metot, legacy hash'leri de otomatik olarak kontrol eder.
            HashingManager.CheckResult result = plugin.getHashingManager().checkPassword(oldPassword, playerData.getHashedPassword());

            if (result.isValid) {
                // Eski şifre doğru, yeni şifreyi ana hash algoritması ile hash'le ve güncelle
                String newHashedPassword = plugin.getHashingManager().hash(newPassword);
                plugin.getStorage().updatePassword(player.getUniqueId(), newHashedPassword).thenRun(() -> {
                    player.sendMessage(plugin.getLocaleManager().getMessage("changepass.success"));
                });
            } else {
                player.sendMessage(plugin.getLocaleManager().getMessage("changepass.wrong_old_password"));
            }
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
            player.sendMessage(plugin.getLocaleManager().getMessage("changepass.password_length",
                    "{min}", String.valueOf(minLength),
                    "{max}", String.valueOf(maxLength))
            );
            return true;
        }

        if (unsafePasswords.stream().anyMatch(unsafe -> unsafe.equalsIgnoreCase(password))) {
            player.sendMessage(plugin.getLocaleManager().getMessage("changepass.password_unsafe"));
            return true;
        }

        return false;
    }
}