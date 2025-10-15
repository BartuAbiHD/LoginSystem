package me.bartuabihd.loginsystem.storage;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.storage.impl.H2Storage;
import me.bartuabihd.loginsystem.storage.impl.SQLiteStorage;
import me.bartuabihd.loginsystem.storage.impl.MySQLStorage;
import me.bartuabihd.loginsystem.storage.impl.MongoStorage;
import me.bartuabihd.loginsystem.storage.impl.PostgreSQLStorage;

/**
 * Yapılandırma dosyasına (config.yml) göre uygun IStorage nesnesini oluşturan fabrika sınıfı.
 */
public class StorageFactory {

    /**
     * Config dosyasındaki 'storage.type' değerine göre bir veritabanı depolama nesnesi oluşturur.
     *
     * @param plugin Ana eklenti sınıfının bir örneği (instance).
     * @return Yapılandırılmış IStorage implementasyonu veya geçersiz tür için null.
     */
    public static IStorage createStorage(LoginSystem plugin) {
        // config.yml'den 'storage.type' yolundaki değeri oku.
        // Eğer değer yoksa, varsayılan olarak "sqlite" kullan.
        String storageType = plugin.getConfig().getString("storage.type", "sqlite").toLowerCase();

        plugin.getLogger().info(storageType + " veritabanı türü kullanılıyor...");

        switch (storageType) {
            case "sqlite":
                return new SQLiteStorage(plugin);
            case "h2":
                return new H2Storage(plugin);
            case "mysql":
            case "mariadb":
                return new MySQLStorage(plugin);
            case "postgresql":
            case "postgres":   // Takma ad (alias)
                return new PostgreSQLStorage(plugin);
            case "mongodb":
                return new MongoStorage(plugin);
            default:
                // Eğer config'e geçersiz bir tür yazılmışsa
                plugin.getLogger().severe("'" + storageType + "' geçerli bir veritabanı türü değil! Lütfen config.yml dosyanızı kontrol edin.");
                return null;
        }
    }
}