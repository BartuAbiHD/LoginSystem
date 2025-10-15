package me.bartuabihd.loginsystem.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthManager {

    private final Set<UUID> loggedInPlayers = new HashSet<>();

    public void loginPlayer(UUID uuid) {
        loggedInPlayers.add(uuid);
    }

    public void logoutPlayer(UUID uuid) {
        loggedInPlayers.remove(uuid);
    }

    public boolean isPlayerLoggedIn(UUID uuid) {
        return loggedInPlayers.contains(uuid);
    }
}