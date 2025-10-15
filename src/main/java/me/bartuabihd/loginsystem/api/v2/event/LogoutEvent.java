package me.bartuabihd.loginsystem.api.v2.event;

import org.bukkit.entity.Player;

/**
 * Bir oyuncu /logout komutu ile çıkış yaptığında tetiklenir.
 */
public class LogoutEvent extends AuthEvent {
    public LogoutEvent(Player player) {
        super(player);
    }
}