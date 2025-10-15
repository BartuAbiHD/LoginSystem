package me.bartuabihd.loginsystem.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SHA1Hasher implements PasswordHasher {

    private final SecureRandom random = new SecureRandom();
    private static final String ALGORITHM = "SHA-1";
    private static final String PREFIX = "$SHA1$";

    @Override
    public String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltString = bytesToHex(salt);
        String hash = bytesToHex(digest(password, salt));
        return PREFIX + saltString + "$" + hash;
    }

    @Override
    public boolean check(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith(PREFIX)) {
            // Eski, tuzsuz SHA1 hash'lerini kontrol etmek için bir geri dönüş (fallback) eklenebilir.
            // Örneğin: return storedHash.equals(bytesToHex(digest(password, new byte[0])));
            // Ancak bu, güvenlik riskini artırır. Bu örnekte sadece tuzlu formatı destekliyoruz.
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        byte[] salt = hexToBytes(parts[2]);
        String originalHash = parts[3];
        String checkHash = bytesToHex(digest(password, salt));
        return originalHash.equals(checkHash);
    }

    /**
     * Verilen şifre ve salt ile bir SHA-1 özeti oluşturur.
     * @param password Düz metin şifre.
     * @param salt Tuzlama için kullanılacak byte dizisi.
     * @return Şifrenin SHA-1 özeti (byte dizisi).
     */
    private byte[] digest(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            // Önce salt, sonra şifre eklenir. Bu sıra tutarlı olmalıdır.
            md.update(salt);
            md.update(password.getBytes());
            return md.digest();
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

    /**
     * Onaltılık (hexadecimal) bir string'i byte dizisine dönüştürür.
     */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}