package me.bartuabihd.loginsystem.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Şifreleri güvenli bir şekilde hash'lemek ve doğrulamak için yardımcı metotlar içeren utility sınıfı.
 * Bu sınıfın bir örneği (instance) oluşturulamaz.
 */
public final class PasswordUtil {

    // BCrypt algoritmasının "work factor" (iş faktörü).
    // Bu değer ne kadar yüksek olursa, hash'leme o kadar yavaş ve güvenli olur.
    // 10 ile 12 arası değerler genellikle iyi bir denge sunar.
    private static final int WORK_FACTOR = 12;

    /**
     * Bu sınıf bir utility sınıfı olduğu için constructor'ı private yapıyoruz.
     * Böylece 'new PasswordUtil()' şeklinde yeni bir nesne oluşturulmasını engelliyoruz.
     */
    private PasswordUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Verilen düz metin şifreyi BCrypt algoritması kullanarak hash'ler.
     *
     * @param plainTextPassword Hash'lenecek olan orijinal şifre.
     * @return Güvenli bir şekilde hash'lenmiş şifre (String).
     */
    public static String hashPassword(String plainTextPassword) {
        // BCrypt.gensalt() metodu, her şifre için benzersiz bir "tuz" (salt) oluşturur.
        // Bu, aynı şifreye sahip iki kullanıcının bile veritabanında farklı hash'lere sahip olmasını sağlar.
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Kullanıcının girdiği düz metin şifrenin, veritabanında saklanan hash'lenmiş şifre ile
     * eşleşip eşleşmediğini kontrol eder.
     *
     * @param plainTextPassword Kullanıcının giriş yaparken girdiği şifre.
     * @param hashedPassword Veritabanından alınan, daha önce hash'lenmiş olan şifre.
     * @return Şifreler eşleşiyorsa true, eşleşmiyorsa false döner.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        // BCrypt, hash'in içine "tuz" bilgisini de gömdüğü için doğrulama sırasında
        // ayrıca bir tuz parametresine ihtiyaç duymaz.
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}