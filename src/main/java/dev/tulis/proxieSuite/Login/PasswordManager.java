package dev.tulis.proxieSuite.Login;

import com.password4j.Hash;
import com.password4j.Password;
import dev.tulis.proxieSuite.Database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordManager {

    protected static String hashPassword(String password) {
        Hash hash = Password.hash(password).addRandomSalt().withArgon2();
        return hash.getResult();
    }

    protected static PasswordState verifyPassword(
        String password,
        String username
    ) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                "SELECT password FROM proxie_players WHERE username = ?"
            );
            statement.setString(1, username);

            ResultSet set = statement.executeQuery();
            if (!set.next()) return PasswordState.NOT_REGISTERED;
            if (
                set.getString("password") == null
            ) return PasswordState.NOT_REGISTERED;

            boolean verify = Password.check(
                password,
                set.getString("password")
            ).withArgon2();

            return verify ? PasswordState.CORRECT : PasswordState.INCORRECT;
        } catch (SQLException e) {
            e.printStackTrace();
        } // TODO: Robust logging

        return PasswordState.INCORRECT;
    }

    public static enum PasswordState {
        CORRECT,
        INCORRECT,
        NOT_REGISTERED,
    }
}
