package me.bartuabihd.loginsystem.util;

import me.bartuabihd.loginsystem.LoginSystem;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final LoginSystem plugin;
    private final int resourceId;

    public UpdateChecker(LoginSystem plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    /**
     * SpigotMC API'sinden en son sürüm numarasını asenkron olarak alır.
     * @param consumer Sonuç döndüğünde çalıştırılacak olan kod bloğu (en son versiyonu içerir).
     */
    public void getLatestVersion(Consumer<String> consumer) {
        // Ağ işlemlerini asenkron yaparak sunucunun ana thread'ini tıkamıyoruz.
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream();
                 Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                plugin.getLogger().info(plugin.getLocaleManager().getMessage("error.update_check_failed", "{exception_message}", String.valueOf(exception.getMessage())));
            }
        });
    }
}