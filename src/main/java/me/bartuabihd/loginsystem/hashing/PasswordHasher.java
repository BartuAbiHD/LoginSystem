package me.bartuabihd.loginsystem.hashing;

public interface PasswordHasher {
    /**
     * Verilen şifreyi hash'ler.
     * @param password Düz metin şifre.
     * @return Hash'lenmiş şifre.
     */
    String hash(String password);

    /**
     * Düz metin şifrenin, verilen hash ile eşleşip eşleşmediğini kontrol eder.
     * @param password Düz metin şifre.
     * @param hash Veritabanından gelen hash.
     * @return Eşleşiyorsa true, aksi halde false.
     */
    boolean check(String password, String hash);
}