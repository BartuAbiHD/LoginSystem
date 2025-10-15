package me.bartuabihd.loginsystem.api.v2.event;

import org.bukkit.entity.Player;

/**
 * Bir oyuncu başarıyla giriş yaptığında tetiklenir.
 */
public class LoginEvent extends AuthEvent {
    public LoginEvent(Player player) {
        super(player);
    }
}