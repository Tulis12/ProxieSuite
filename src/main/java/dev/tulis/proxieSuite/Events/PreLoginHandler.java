package dev.tulis.proxieSuite.Events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import dev.tulis.proxieSuite.API.PlayerSession;
import dev.tulis.proxieSuite.Administration.BanAPI;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Login.LoginPreLoginHandler;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public class PreLoginHandler {

    Main plugin;

    public PreLoginHandler(Main m) {
        plugin = m;
    }

    private void addUser(String username, UUID uuid) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement createUser = conn.prepareStatement(
                "INSERT INTO proxie_players(username, uuid) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );

            createUser.setString(1, username);
            createUser.setString(2, Objects.toString(uuid, null));

            createUser.executeUpdate();

            ResultSet set = createUser.getGeneratedKeys();
            if (set.next()) {
                PlayerSession.put(username, "id", set.getInt(1));
            }
        } catch (SQLException e) {
            plugin
                .getLogger()
                .error("Unable to insert new user into the database!", e);
        }
    }

    @Subscribe
    public void onHandshake(PreLoginEvent event) {
        String username = event.getUsername();

        if (BanAPI.handlePreLogin(event)) return;

        boolean onlineauth = false;
        boolean joined = false;
        boolean onlineAccountExists = false;
        String password = null;

        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                "SELECT id, onlineauth, online_account_exists, password FROM proxie_players WHERE username = ?"
            );

            statement.setString(1, username);

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                onlineauth = set.getBoolean("onlineauth");
                onlineAccountExists = set.getBoolean("online_account_exists");
                password = set.getString("password");
                PlayerSession.put(event.getUsername(), "id", set.getInt("id"));

                joined = true;
            }
        } catch (SQLException e) {
            plugin.getLogger().error("Database exception at handshake!", e);
            event.setResult(
                PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(I18N.l("kick.server_error"))
                )
            );
            return;
        }

        if (!joined) {
            addUser(username, event.getUniqueId());
        }

        PlayerSession.put(username, "joinedBefore", joined);

        if (plugin.getConfig().getBoolean("login.enabled")) {
            PlayerSession.put(username, "password", password);

            LoginPreLoginHandler loginHandler = new LoginPreLoginHandler(
                plugin
            );

            loginHandler.handlePreLogin(
                event,
                onlineauth,
                joined,
                onlineAccountExists
            );
        }
    }
}
