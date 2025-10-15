package me.bartuabihd.loginsystem.api.v2.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Bir oyuncu e-posta adresini başarıyla değiştirdiğinde veya eklediğinde tetiklenir.
 */
public class EmailChangeEvent extends AuthEvent {

    private final String oldEmail;
    private final String newEmail;

    /**
     * @param player Olayla ilişkili oyuncu.
     * @param oldEmail Oyuncunun eski e-posta adresi (ilk kez ekliyorsa null olabilir).
     * @param newEmail Oyuncunun yeni e-posta adresi.
     */
    public EmailChangeEvent(Player player, @Nullable String oldEmail, String newEmail) {
        super(player);
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
    }

    /**
     * Oyuncunun önceki e-posta adresini döndürür.
     * @return Eski e-posta adresi veya oyuncunun daha önce bir e-postası yoksa null.
     */
    @Nullable
    public String getOldEmail() {
        return oldEmail;
    }

    /**
     * Oyuncunun yeni ayarlanan e-posta adresini döndürür.
     * @return Yeni e-posta adresi.
     */
    public String getNewEmail() {
        return newEmail;
    }
}