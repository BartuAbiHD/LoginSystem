package me.bartuabihd.loginsystem.hashing;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptHasher implements PasswordHasher {

    private final int logRounds;

    public BCryptHasher() {
        // AuthMe'deki varsayılan değerdir, yapılandırılabilir hale getirilebilir.
        this.logRounds = 10;
    }

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean check(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            // Geçersiz hash formatı
            return false;
        }
    }
}