package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.PlayerCache.PlayerCache;
import dev.tulis.proxieSuite.i18n.I18N;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private void onlineAccountExists(
        boolean onlineAccountExists,
        String username
    ) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement createUser = conn.prepareStatement(
                "UPDATE proxie_players SET online_account_exists = ? WHERE username = ?"
            );

            createUser.setBoolean(1, onlineAccountExists);
            createUser.setString(2, username);
            createUser.executeUpdate();
        } catch (SQLException e) {
            plugin
                .getLogger()
                .error("Unable to insert new user into the database!", e);
        }
    }

    private void addUser(PreLoginEvent event, boolean onlineauth) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement createUser = conn.prepareStatement(
                "INSERT INTO proxie_players(username, uuid, onlineauth) VALUES (?, ?, ?)"
            );

            createUser.setString(1, event.getUsername());
            createUser.setString(
                2,
                event.getUniqueId() != null
                    ? event.getUniqueId().toString()
                    : null
            );
            createUser.setBoolean(3, onlineauth);
            createUser.executeUpdate();
        } catch (SQLException e) {
            plugin
                .getLogger()
                .error("Unable to insert new user into the database!", e);
        }
    }

    @Subscribe
    public void onHandshake(PreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String username = event.getUsername();

        boolean onlineauth = false;
        boolean joined = false;
        boolean onlineAccountExists = false;

        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                "SELECT onlineauth, online_account_exists FROM proxie_players WHERE username = ?"
            );

            statement.setString(1, username);

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                onlineauth = set.getBoolean("onlineauth");
                onlineAccountExists = set.getBoolean("online_account_exists");
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

        PlayerCache.put(username, "joinedBefore", joined);

        if (uuid != null) {
            int version = uuid.version();

            if (version == 3) {
                if (onlineauth) {
                    event.setResult(
                        PreLoginEvent.PreLoginComponentResult.denied(
                            Component.text(
                                I18N.l("kick.nonpremium_logged_as_premium")
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

                StateManager.setPlayerState(
                    username,
                    PlayerState.UNAUTHENTICATED
                );

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

            if (!joined) {
                plugin.runTask(() -> {
                    addUser(event, true);
                });
            }

            event.setResult(
                PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            );

            StateManager.setPlayerState(username, PlayerState.AUTHENTICATED);
            return;
        }

        // This is 1.19.3 and below

        if (onlineauth) {
            System.out.println("force auth!");

            event.setResult(
                PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            );

            StateManager.setPlayerState(username, PlayerState.AUTHENTICATED);
            return;
        }

        if (onlineAccountExists) {
            StateManager.setPlayerState(
                event.getUsername(),
                PlayerState.UNAUTHENTICATED_LEGACY
            );

            PlayerCache.put(
                username,
                "onlineAccountExists",
                onlineAccountExists
            );

            return;
        }

        if (!joined) {
            addUser(event, false);

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

            PlayerCache.put(username, "onlineAccountExists", exists);

            plugin.runTask(() -> {
                onlineAccountExists(accountExists, username);
            });

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

        PlayerCache.put(username, "onlineAccountExists", onlineAccountExists);
    }
}
