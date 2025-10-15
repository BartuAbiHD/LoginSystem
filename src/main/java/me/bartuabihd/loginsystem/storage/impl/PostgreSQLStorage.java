package me.bartuabihd.loginsystem.storage.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.model.PlayerData;
import me.bartuabihd.loginsystem.storage.IStorage;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PostgreSQLStorage implements IStorage {

    private final LoginSystem plugin;
    private HikariDataSource dataSource;

    public PostgreSQLStorage(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        ConfigurationSection dbConfig = plugin.getConfig().getConfigurationSection("postgresql");
        if (dbConfig == null) {
            plugin.getLogger().severe("Config.yml dosyasında 'postgresql' ayarları bulunamadı!");
            return;
        }

        HikariConfig config = new HikariConfig();
        // JDBC URL'si PostgreSQL için farklıdır.
        config.setJdbcUrl("jdbc:postgresql://" + dbConfig.getString("host") + ":" + dbConfig.getInt("port") + "/" + dbConfig.getString("dbname"));
        config.setUsername(dbConfig.getString("user"));
        config.setPassword(dbConfig.getString("password"));
        config.setMaximumPoolSize(10);

        this.dataSource = new HikariDataSource(config);

        // PostgreSQL ile uyumlu, email sütunu eklenmiş tablo oluşturma sorgusu.
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS players (" +
                             "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                             "username VARCHAR(16) NOT NULL," +
                             "password_hash VARCHAR(255) NOT NULL," +
                             "registration_ip VARCHAR(45)," +
                             "last_login_date BIGINT," +
                             "email VARCHAR(255) UNIQUE" +
                             ");")) {
            ps.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("PostgreSQL veritabanı tablosu oluşturulamadı!");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public CompletableFuture<Boolean> isPlayerRegistered(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM players WHERE uuid = ?")) {
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
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(uuid) FROM players WHERE registration_ip = ?")) {
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
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
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
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
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
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE players SET password_hash = ? WHERE uuid = ?")) {
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
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM players WHERE uuid = ?")) {
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
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE players SET email = ? WHERE uuid = ?")) {
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
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM players WHERE email = ?")) {
                ps.setString(1, email);
                return ps.executeQuery().next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}