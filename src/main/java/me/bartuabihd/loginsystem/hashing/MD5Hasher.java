package me.bartuabihd.loginsystem.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MD5Hasher implements PasswordHasher {

    private final SecureRandom random = new SecureRandom();
    private static final String ALGORITHM = "MD5";

    @Override
    public String hash(String password) {
        // MD5 için genellikle 16 byte'lık bir salt yeterlidir.
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltString = bytesToHex(salt);
        String hash = bytesToHex(digest(password, saltString));
        // AuthMe uyumlu format
        return "$MD5$" + saltString + "$" + hash;
    }

    @Override
    public boolean check(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$MD5$")) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        String salt = parts[2];
        String originalHash = parts[3];
        String checkHash = bytesToHex(digest(password, salt));
        return originalHash.equals(checkHash);
    }

    /**
     * Verilen şifre ve salt ile bir MD5 özeti oluşturur.
     * @param password Düz metin şifre.
     * @param salt Tuzlama için kullanılacak string.
     * @return Şifrenin MD5 özeti (byte dizisi).
     */
    private byte[] digest(String password, String salt) {
        try {
            // Şifre ve salt birleştirilir: MD5(password + salt)
            String toHash = password + salt;
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            return md.digest(toHash.getBytes());
        } catch (NoSuchAlgorithmException e) {
            // Bu durumun modern Java sistemlerinde yaşanması beklenmez.
            throw new RuntimeException(ALGORITHM + " algorithm is not supported!", e);
        }
    }

    /**
     * Bir byte dizisini onaltılık (hexadecimal) string'e dönüştürür.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}