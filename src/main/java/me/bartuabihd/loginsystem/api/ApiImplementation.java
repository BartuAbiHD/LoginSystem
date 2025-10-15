package me.bartuabihd.loginsystem.api;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.api.v2.LoginSystemAPI;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiImplementation implements LoginSystemAPI {

    private final LoginSystem plugin;

    public ApiImplementation(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Boolean> isRegistered(UUID playerUUID) {
        return plugin.getStorage().isPlayerRegistered(playerUUID);
    }

    @Override
    public boolean isLoggedIn(Player player) {
        return plugin.getAuthManager().isPlayerLoggedIn(player.getUniqueId());
    }

    @Override
    public void forceLogin(Player player) {
        plugin.cancelTimeoutTask(player);
        plugin.getAuthManager().loginPlayer(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Void> forceUnregister(UUID playerUUID) {
        return plugin.getStorage().unregisterPlayer(playerUUID);
    }

    @Override
    public CompletableFuture<Optional<String>> getEmail(UUID playerUUID) {
        return plugin.getStorage().getPlayerData(playerUUID)
                .thenApply(playerData -> Optional.ofNullable(playerData != null ? playerData.getEmail() : null));
    }

    @Override
    public CompletableFuture<Void> setEmail(UUID playerUUID, String email) {
        // Burada null kontrolü ve temel format kontrolü yapmak API'yi daha sağlam hale getirir.
        if (email == null || email.trim().isEmpty()) {
            // Geçersiz bir e-postayı silme olarak yorumlayabiliriz.
            return plugin.getStorage().updateEmail(playerUUID, null);
        }
        return plugin.getStorage().updateEmail(playerUUID, email);
    }

    @Override
    public CompletableFuture<Boolean> isEmailInUse(String email) {
        return plugin.getStorage().isEmailInUse(email);
    }
}