package me.bartuabihd.loginsystem.api.v2.event;

import org.bukkit.entity.Player;

/**
 * Bir oyuncu başarıyla kayıt olduğunda tetiklenir.
 */
public class RegisterEvent extends AuthEvent {
    public RegisterEvent(Player player) {
        super(player);
    }
}