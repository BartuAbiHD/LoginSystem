package me.bartuabihd.loginsystem.storage.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.model.PlayerData;
import me.bartuabihd.loginsystem.storage.IStorage;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoStorage implements IStorage {

    private final LoginSystem plugin;
    private MongoClient mongoClient;
    private MongoCollection<Document> playersCollection;

    public MongoStorage(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        String connectionUri = plugin.getConfig().getString("mongodb.connection-uri");
        String databaseName = plugin.getConfig().getString("mongodb.database");

        if (connectionUri == null || databaseName == null) {
            plugin.getLogger().severe("Config.yml dosyasında 'mongodb' ayarları eksik!");
            return;
        }

        try {
            this.mongoClient = MongoClients.create(connectionUri);
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            this.playersCollection = database.getCollection("players");
        } catch (Exception e) {
            plugin.getLogger().severe("MongoDB bağlantısı kurulamadı!");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    private Document playerToDocument(PlayerData playerData) {
        return new Document("_id", playerData.getUuid().toString())
                .append("username", playerData.getUsername())
                .append("password_hash", playerData.getHashedPassword())
                .append("registration_ip", playerData.getRegistrationIp())
                .append("last_login_date", playerData.getLastLoginDate())
                .append("email", playerData.getEmail());
    }

    private PlayerData documentToPlayer(Document doc) {
        if (doc == null) return null;
        PlayerData data = new PlayerData(
                UUID.fromString(doc.getString("_id")),
                doc.getString("username"),
                doc.getString("password_hash"),
                doc.getString("registration_ip")
        );
        data.setLastLoginDate(doc.getLong("last_login_date"));
        data.setEmail(doc.getString("email"));
        return data;
    }

    @Override
    public CompletableFuture<Boolean> isPlayerRegistered(UUID uuid) {
        return CompletableFuture.supplyAsync(() ->
                playersCollection.find(Filters.eq("_id", uuid.toString())).first() != null
        );
    }

    @Override
    public CompletableFuture<Integer> countRegistrationsByIp(String ip) {
        return CompletableFuture.supplyAsync(() ->
                (int) playersCollection.countDocuments(Filters.eq("registration_ip", ip))
        );
    }

    @Override
    public CompletableFuture<Void> registerPlayer(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            Document doc = playerToDocument(playerData);
            playersCollection.replaceOne(Filters.eq("_id", doc.get("_id")), doc, new ReplaceOptions().upsert(true));
        });
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document doc = playersCollection.find(Filters.eq("_id", uuid.toString())).first();
            return documentToPlayer(doc);
        });
    }

    @Override
    public CompletableFuture<Void> updatePassword(UUID uuid, String newHashedPassword) {
        return CompletableFuture.runAsync(() -> {
            playersCollection.updateOne(
                    Filters.eq("_id", uuid.toString()),
                    Updates.set("password_hash", newHashedPassword)
            );
        });
    }

    @Override
    public CompletableFuture<Void> unregisterPlayer(UUID uuid) {
        return CompletableFuture.runAsync(() ->
                playersCollection.deleteOne(Filters.eq("_id", uuid.toString()))
        );
    }

    @Override
    public CompletableFuture<Void> updateEmail(UUID uuid, String email) {
        return CompletableFuture.runAsync(() -> {
            playersCollection.updateOne(
                    Filters.eq("_id", uuid.toString()),
                    Updates.set("email", email)
            );
        });
    }

    @Override
    public CompletableFuture<Boolean> isEmailInUse(String email) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null) return false;
            return playersCollection.find(Filters.eq("email", email)).first() != null;
        });
    }
}