package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.i18n;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public class PreLoginHandler {

    Main plugin;

    public PreLoginHandler(Main m) {
        plugin = m;
    }

    private Runnable addUser(PreLoginEvent event, boolean onlineauth) {
        return new Runnable() {
            @Override
            public void run() {
                try (Connection conn = Database.getConnection()) {
                    PreparedStatement createUser = conn.prepareStatement(
                        "INSERT INTO proxie_players(username, uuid, onlineauth) VALUES (?, ?, ?)"
                    );

                    createUser.setString(1, event.getUsername());
                    createUser.setString(2, event.getUniqueId().toString());
                    createUser.setBoolean(3, onlineauth);
                    createUser.executeUpdate();
                } catch (SQLException e) {
                    plugin
                        .getLogger()
                        .error(
                            "Unable to insert new user into the database!",
                            e
                        );
                }
            }
        };
    }

    @Subscribe
    public void onHandshake(PreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (uuid != null) {
            int version = uuid.version();
            boolean onlineauth = false;
            boolean joined = false;

            try (Connection conn = Database.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(
                    "SELECT onlineauth FROM proxie_players WHERE username = ?"
                );

                statement.setString(1, event.getUsername());

                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    onlineauth = set.getBoolean("onlineauth");
                    joined = true;
                }
            } catch (SQLException e) {
                plugin.getLogger().error("Database exception at handshake!", e);
                event.setResult(
                    PreLoginEvent.PreLoginComponentResult.denied(
                        Component.text(i18n.l("kick.server_error"))
                    )
                );
                return;
            }

            if (version == 3) {
                if (onlineauth) {
                    event.setResult(
                        PreLoginEvent.PreLoginComponentResult.denied(
                            Component.text(
                                i18n.l("kick.nonpremium_logged_as_premium")
                            )
                        )
                    );

                    return;
                }

                if (!joined) {
                    plugin.runTask(() -> {
                        addUser(event, false);
                    });
                }

                event.setResult(
                    PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                );

                StateManager.setPlayerState(uuid, PlayerState.UNAUTHENTICATED);

                return;
            }

            if (!onlineauth && joined) {
                event.setResult(
                    PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                );

                StateManager.setPlayerState(uuid, PlayerState.UNAUTHENTICATED);

                return;
            }

            if (!joined) {
                plugin.runTask(() -> {
                    addUser(event, true);
                });
            }

            event.setResult(
                PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            );

            StateManager.setPlayerState(uuid, PlayerState.AUTHENTICATED);

            return;
        }

        // This is 1.19.3 and below
    }
}
