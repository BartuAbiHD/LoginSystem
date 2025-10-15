package me.bartuabihd.loginsystem.storage;

import me.bartuabihd.loginsystem.model.PlayerData;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IStorage {

    void connect();
    void disconnect();
    CompletableFuture<Boolean> isPlayerRegistered(UUID uuid);
    CompletableFuture<Void> registerPlayer(PlayerData playerData);
    CompletableFuture<PlayerData> getPlayerData(UUID uuid);
    CompletableFuture<Void> updatePassword(UUID uuid, String newHashedPassword);
    CompletableFuture<Void> unregisterPlayer(UUID uuid);
    CompletableFuture<Void> updateEmail(UUID uuid, String email);
    CompletableFuture<Boolean> isEmailInUse(String email);
    CompletableFuture<Integer> countRegistrationsByIp(String ip);
}