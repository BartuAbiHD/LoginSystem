package me.bartuabihd.loginsystem.api.v2;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * LoginSystem v1 API için ana arayüz.
 * Diğer eklentilerin LoginSystem ile etkileşime girmesini sağlar.
 * API'ye erişmek için: LoginSystem.getApi()
 */
public interface LoginSystemAPI {

    /**
     * Bir oyuncunun sunucuya kayıtlı olup olmadığını asenkron olarak kontrol eder.
     * Bu metot veritabanı sorgusu yaptığı için CompletableFuture döner.
     *
     * @param playerUUID Kontrol edilecek oyuncunun UUID'si.
     * @return Oyuncu kayıtlıysa true, değilse false içeren bir CompletableFuture.
     */
    CompletableFuture<Boolean> isRegistered(UUID playerUUID);

    /**
     * Çevrimiçi bir oyuncunun o anki oturumda giriş yapıp yapmadığını senkron olarak kontrol eder.
     * Bu metot hafızadan (in-memory) çalıştığı için anında sonuç döner.
     *
     * @param player Kontrol edilecek çevrimiçi oyuncu.
     * @return Oyuncu giriş yapmışsa true, değilse false.
     */
    boolean isLoggedIn(Player player);

    /**
     * Bir oyuncuyu, şifresini girmesine gerek kalmadan, zorla giriş yapmış olarak işaretler.
     * Bu, genellikle admin komutları veya özel sistemler için kullanılır.
     *
     * @param player Giriş yapması zorlanacak oyuncu.
     */
    void forceLogin(Player player);

    /**
     * Bir oyuncunun kaydını veritabanından asenkron olarak siler.
     * DİKKAT: Bu işlem geri alınamaz.
     *
     * @param playerUUID Kaydı silinecek oyuncunun UUID'si.
     * @return İşlem tamamlandığında sona erecek bir CompletableFuture.
     */
    CompletableFuture<Void> forceUnregister(UUID playerUUID);

    /**
     * Bir oyuncunun kayıtlı e-posta adresini asenkron olarak alır.
     *
     * @param playerUUID Bilgisi alınacak oyuncunun UUID'si.
     * @return Oyuncunun e-postasını içeren bir Optional, eğer e-postası yoksa boş bir Optional
     * içeren CompletableFuture.
     */
    CompletableFuture<Optional<String>> getEmail(UUID playerUUID);

    /**
     * Bir oyuncunun e-posta adresini asenkron olarak ayarlar veya günceller.
     *
     * @param playerUUID E-postası ayarlanacak oyuncunun UUID'si.
     * @param email      Ayarlanacak yeni e-posta adresi.
     * @return İşlem tamamlandığında sona erecek bir CompletableFuture.
     */
    CompletableFuture<Void> setEmail(UUID playerUUID, String email);

    /**
     * Bir e-posta adresinin veritabanında başka bir oyuncu tarafından kullanılıp
     * kullanılmadığını asenkron olarak kontrol eder.
     *
     * @param email Kontrol edilecek e-posta adresi.
     * @return E-posta kullanımdaysa true, değilse false içeren bir CompletableFuture.
     */
    CompletableFuture<Boolean> isEmailInUse(String email);
}