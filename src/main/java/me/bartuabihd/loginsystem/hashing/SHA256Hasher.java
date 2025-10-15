package me.bartuabihd.loginsystem.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SHA256Hasher implements PasswordHasher {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltString = bytesToHex(salt);
        String hash = bytesToHex(digest(password, salt));
        return "$SHA$" + saltString + "$" + hash;
    }

    @Override
    public boolean check(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$SHA$")) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        byte[] salt = hexToBytes(parts[2]);
        String checkHash = bytesToHex(digest(password, salt));
        return checkHash.equals(parts[3]);
    }

    private byte[] digest(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            md.update(salt);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

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