package me.bartuabihd.loginsystem.api.v2.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * LoginSystem API'sindeki tüm kimlik doğrulama olayları için temel sınıf.
 */
public abstract class AuthEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    protected final Player player;

    public AuthEvent(Player player) {
        this.player = player;
    }

    /**
     * Olayla ilişkili oyuncuyu döndürür.
     * @return Player nesnesi.
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}