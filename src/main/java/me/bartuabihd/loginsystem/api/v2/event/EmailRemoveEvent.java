package me.bartuabihd.loginsystem.api.v2.event;

import org.bukkit.entity.Player;

/**
 * Bir oyuncu e-posta adresini başarıyla kaldırdığında tetiklenir.
 */
public class EmailRemoveEvent extends AuthEvent {

    private final String removedEmail;

    public EmailRemoveEvent(Player player, String removedEmail) {
        super(player);
        this.removedEmail = removedEmail;
    }

    /**
     * Hesaptan kaldırılan e-posta adresini döndürür.
     * @return Kaldırılan e-posta adresi.
     */
    public String getRemovedEmail() {
        return removedEmail;
    }
}