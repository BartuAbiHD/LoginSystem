package me.bartuabihd.loginsystem.model;

import java.util.UUID;

/**
 * Bir oyuncunun kimlik doğrulama verilerini temsil eden model sınıfı.
 */
public class PlayerData {

    private final UUID uuid;
    private final String username;
    private String hashedPassword;
    private String email;
    private final String registrationIp;
    private long lastLoginDate;

    /**
     * Yeni bir oyuncu veri nesnesi oluşturur.
     *
     * @param uuid Oyuncunun eşsiz kimliği (UUID).
     * @param username Oyuncunun adı.
     * @param hashedPassword Oyuncunun BCrypt ile hash'lenmiş şifresi.
     * @param registrationIp Oyuncunun kayıt olduğu IP adresi.
     */
    public PlayerData(UUID uuid, String username, String hashedPassword, String registrationIp) {
        this.uuid = uuid;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.registrationIp = registrationIp;
        this.lastLoginDate = System.currentTimeMillis(); // Varsayılan olarak kayıt anı
        this.email = null;
    }

    // --- Getters ---

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getRegistrationIp() {
        return registrationIp;
    }

    public long getLastLoginDate() {
        return lastLoginDate;
    }

    // --- Setters ---

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLastLoginDate(long lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}