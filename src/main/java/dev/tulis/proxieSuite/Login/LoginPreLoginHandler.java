package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import dev.tulis.proxieSuite.API.PlayerSession;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public class LoginPreLoginHandler {

    private static Main plugin;

    public LoginPreLoginHandler(Main m) {
        plugin = m;
    }

    private void saveOnlineAuthToDb(String username, boolean onlineauth) {
        plugin.runTask(() -> {
            // This doesn't have to block the event

            try (Connection conn = Database.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(
                    "UPDATE proxie_players SET onlineauth = ? WHERE username = ?"
                );

                statement.setBoolean(1, onlineauth);
                statement.setString(2, username);

                statement.executeUpdate();
            } catch (SQLException e) {
                plugin
                    .getLogger()
                    .error("Error occured during query to the database!", e);
            }
        });
    }

    private void saveOnlineAccountExists(
        String username,
        boolean onlineAccountExists
    ) {
        plugin.runTask(() -> {
            // This doesn't have to block the event

            try (Connection conn = Database.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(
                    "UPDATE proxie_players SET online_account_exists = ? WHERE username = ?"
                );

                statement.setBoolean(1, onlineAccountExists);
                statement.setString(2, username);

                statement.executeUpdate();
            } catch (SQLException e) {
                plugin
                    .getLogger()
                    .error("Error occured during query to the database!", e);
            }
        });
    }

    private void handleOfflineModern(
        PreLoginEvent event,
        boolean onlineauth,
        boolean joined
    ) {
        if (onlineauth) {
            event.setResult(
                PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(I18N.l("kick.nonpremium_logged_as_premium"))
                )
            );

            return;
        }

        if (!joined) {
            saveOnlineAuthToDb(event.getUsername(), false);
        }

        event.setResult(
            PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
        );

        StateManager.setPlayerState(
            event.getUsername(),
            PlayerState.UNAUTHENTICATED
        );
    }

    public void handlePreLogin(
        PreLoginEvent event,
        boolean onlineauth,
        boolean joined,
        boolean onlineAccountExists
    ) {
        UUID uuid = event.getUniqueId();
        String username = event.getUsername();

        if (uuid != null) {
            int version = uuid.version();

            if (version == 3) {
                handleOfflineModern(event, onlineauth, joined);
                return;
            }

            if (!onlineauth && joined) {
                event.setResult(
                    PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                );

                StateManager.setPlayerState(
                    username,
                    PlayerState.UNAUTHENTICATED
                );

                return;
            }

            // Everyone here is online-mode

            if (!joined) {
                saveOnlineAuthToDb(username, true);
            }

            event.setResult(
                PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            );

            StateManager.setPlayerState(username, PlayerState.AUTHENTICATED);
            return;
        }

        // This is 1.19.3 and below

        if (onlineauth) {
            // Someone declared they have an online-mode account, force
            event.setResult(
                PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            );

            StateManager.setPlayerState(username, PlayerState.AUTHENTICATED);
            return;
        }

        if (onlineAccountExists) {
            // For handling suggestion to enable premium logon
            StateManager.setPlayerState(
                event.getUsername(),
                PlayerState.UNAUTHENTICATED_LEGACY
            );

            PlayerSession.put(
                username,
                "onlineAccountExists",
                onlineAccountExists
            );

            return;
        }

        if (!joined) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(
                    URI.create(
                        "https://api.mojang.com/users/profiles/minecraft/" +
                            event.getUsername()
                    )
                )
                .build();

            boolean exists = false;

            try {
                HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

                exists = response.statusCode() == 200;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                // TODO: More robust logging
            }

            final boolean accountExists = exists;

            PlayerSession.put(username, "onlineAccountExists", exists);
            saveOnlineAccountExists(username, accountExists);

            if (accountExists) {
                StateManager.setPlayerState(
                    event.getUsername(),
                    PlayerState.UNAUTHENTICATED_LEGACY_FIRST_JOIN
                );

                return;
            }

            StateManager.setPlayerState(
                event.getUsername(),
                PlayerState.UNAUTHENTICATED_FIRST_JOIN
            );

            return;
        }

        StateManager.setPlayerState(
            event.getUsername(),
            PlayerState.UNAUTHENTICATED
        );

        PlayerSession.put(username, "onlineAccountExists", onlineAccountExists);
    }
}
