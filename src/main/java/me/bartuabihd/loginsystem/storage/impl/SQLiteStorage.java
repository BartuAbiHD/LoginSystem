package me.bartuabihd.loginsystem.storage.impl;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.model.PlayerData;
import me.bartuabihd.loginsystem.storage.IStorage;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteStorage implements IStorage {

    private final LoginSystem plugin;
    private Connection connection;
    private final File dbFile;

    public SQLiteStorage(LoginSystem plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "auth_data.db");
    }

    @Override
    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                String query = "CREATE TABLE IF NOT EXISTS players (" +
                        "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                        "username VARCHAR(16) NOT NULL," +
                        "password_hash VARCHAR(255) NOT NULL," +
                        "registration_ip VARCHAR(45)," +
                        "last_login_date BIGINT," +
                        "email VARCHAR(255) UNIQUE" +
                        ");";
                statement.execute(query);
            }
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite veritabanı bağlantısı veya tablo oluşturma başarısız!");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Boolean> isPlayerRegistered(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM players WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                return ps.executeQuery().next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> countRegistrationsByIp(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(uuid) FROM players WHERE registration_ip = ?")) {
                ps.setString(1, ip);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> registerPlayer(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO players (uuid, username, password_hash, registration_ip, last_login_date, email) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, playerData.getUuid().toString());
                ps.setString(2, playerData.getUsername());
                ps.setString(3, playerData.getHashedPassword());
                ps.setString(4, playerData.getRegistrationIp());
                ps.setLong(5, System.currentTimeMillis());
                ps.setString(6, playerData.getEmail());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PlayerData data = new PlayerData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("registration_ip")
                    );
                    data.setLastLoginDate(rs.getLong("last_login_date"));
                    data.setEmail(rs.getString("email"));
                    return data;
                }
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> updatePassword(UUID uuid, String newHashedPassword) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET password_hash = ? WHERE uuid = ?")) {
                ps.setString(1, newHashedPassword);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> unregisterPlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM players WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateEmail(UUID uuid, String email) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET email = ? WHERE uuid = ?")) {
                ps.setString(1, email);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isEmailInUse(String email) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM players WHERE email = ?")) {
                ps.setString(1, email);
                return ps.executeQuery().next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}