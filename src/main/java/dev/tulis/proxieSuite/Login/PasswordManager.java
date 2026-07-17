package dev.tulis.proxieSuite.Login;

import com.password4j.Hash;
import com.password4j.Password;
import dev.tulis.proxieSuite.PlayerCache.PlayerCache;

public class PasswordManager {

    protected static String hashPassword(String password) {
        Hash hash = Password.hash(password).addRandomSalt().withArgon2();
        return hash.getResult();
    }

    protected static PasswordState verifyPassword(
        String password,
        String username
    ) {
        String cached_password = PlayerCache.getAs(
            username,
            "password",
            String.class
        );

        if (cached_password == null) return PasswordState.NOT_REGISTERED;

        return Password.check(password, cached_password).withArgon2()
            ? PasswordState.CORRECT
            : PasswordState.INCORRECT;
    }

    public static enum PasswordState {
        CORRECT,
        INCORRECT,
        NOT_REGISTERED,
    }
}
