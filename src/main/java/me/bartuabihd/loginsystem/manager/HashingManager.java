package me.bartuabihd.loginsystem.manager;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.hashing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HashingManager {

    private final LoginSystem plugin;
    private final PasswordHasher primaryHasher;
    private final List<PasswordHasher> legacyHashers = new ArrayList<>();

    public HashingManager(LoginSystem plugin) {
        this.plugin = plugin;
        this.primaryHasher = createHasher(plugin.getConfig().getString("settings.security.passwordHash", "SHA256"))
                .orElseGet(() -> {
                    plugin.getLogger().warning("Invalid passwordHash, SHA256 is used by default.");
                    return new SHA256Hasher();
                });

        for (String legacyName : plugin.getConfig().getStringList("settings.security.legacyHashes")) {
            createHasher(legacyName).ifPresent(legacyHashers::add);
        }
    }

    private Optional<PasswordHasher> createHasher(String name) {
        switch (name.toUpperCase()) {
            case "SHA256":
                return Optional.of(new SHA256Hasher());
            case "BCRYPT":
                return Optional.of(new BCryptHasher());
            case "SHA1":
                return Optional.of(new SHA1Hasher());
            case "MD5":
                return Optional.of(new MD5Hasher());
            default:
                plugin.getLogger().warning("Unsupported hash algorithm: " + name);
                return Optional.empty();
        }
    }

    /**
     * Şifreyi ana algoritma ile hash'ler.
     */
    public String hash(String password) {
        return primaryHasher.hash(password);
    }

    /**
     * Şifrenin doğru olup olmadığını kontrol eder. Önce ana algoritmayı,
     * sonra eski algoritmaları dener.
     * @return Doğruysa ve güncelleme gerekiyorsa yeni hash'i, doğruysa ama güncelleme gerekmiyorsa
     * boş Optional'ı, yanlışsa null döner.
     */
    public CheckResult checkPassword(String password, String storedHash) {
        if (primaryHasher.check(password, storedHash)) {
            return new CheckResult(true, Optional.empty());
        }

        for (PasswordHasher legacyHasher : legacyHashers) {
            if (legacyHasher.check(password, storedHash)) {
                // Eski hash ile eşleşti! Yeni hash'i oluşturup dön.
                return new CheckResult(true, Optional.of(primaryHasher.hash(password)));
            }
        }

        return new CheckResult(false, Optional.empty());
    }

    // Sonucu tutmak için küçük bir yardımcı sınıf
    public static class CheckResult {
        public final boolean isValid;
        public final Optional<String> newHashToUpdate;

        public CheckResult(boolean isValid, Optional<String> newHashToUpdate) {
            this.isValid = isValid;
            this.newHashToUpdate = newHashToUpdate;
        }
    }
}