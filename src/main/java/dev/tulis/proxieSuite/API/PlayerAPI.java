package dev.tulis.proxieSuite.API;

import com.velocitypowered.api.proxy.Player;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Main.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerAPI {

    private static Main plugin;

    public PlayerAPI(Main m) {
        plugin = m;
    }

    public static int getPlayerId(String username) throws SQLException {
        Integer playerId = null;

        Player cel = plugin.getProxy().getPlayer(username).orElse(null);
        if (cel != null) {
            playerId = PlayerSession.getAs(username, "id", Integer.class);
        } else {
            try (Connection conn = Database.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(
                    "SELECT id FROM proxie_players WHERE username = ?"
                );

                statement.setString(1, username);
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    playerId = set.getInt("id");
                }
            }
        }

        return playerId;
    }
}
