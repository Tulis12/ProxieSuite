package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.tulis.proxieSuite.API.PlayerSession;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class ChooseInitialServerHandler {

    private Main plugin;
    private RegisteredServer main = null;

    public ChooseInitialServerHandler(Main m) {
        plugin = m;
    }

    public ChooseInitialServerHandler(Main m, RegisteredServer main) {
        plugin = m;
        this.main = main;
    }

    private void updateMissingUUID(String username, UUID uuid) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement createUser = conn.prepareStatement(
                "UPDATE proxie_players SET uuid = ? WHERE username = ?"
            );

            createUser.setString(1, uuid.toString());
            createUser.setString(2, username);
            createUser.executeUpdate();
        } catch (SQLException e) {
            plugin
                .getLogger()
                .error(
                    "Unable to add the missing player UUID to the database!",
                    e
                );
        }
    }

    @Subscribe
    public void onChooseInitialServerEvent(
        PlayerChooseInitialServerEvent event
    ) {
        Player p = event.getPlayer();

        if (main != null) {
            event.setInitialServer(main);
            return;
        }

        PlayerState state = StateManager.getPlayerState(p.getUsername());

        if (state == PlayerState.AUTHENTICATED) {
            RegisteredServer main = plugin
                .getProxy()
                .getServer(plugin.getConfig().getString("servers.main"))
                .orElseThrow();

            event.setInitialServer(main);
            return;
        }

        if (
            state == PlayerState.UNAUTHENTICATED_FIRST_JOIN ||
            state == PlayerState.UNAUTHENTICATED_LEGACY_FIRST_JOIN
        ) {
            plugin.runTask(() -> {
                updateMissingUUID(p.getUsername(), p.getUniqueId());
            });
        }

        if (
            state == PlayerState.UNAUTHENTICATED_LEGACY ||
            state == PlayerState.UNAUTHENTICATED_LEGACY_FIRST_JOIN
        ) {
            I18N.sendMessage(p, "event.info.legacy_join.propose_premium");
        }

        if (
            state == PlayerState.UNAUTHENTICATED ||
            state == PlayerState.UNAUTHENTICATED_LEGACY ||
            state == PlayerState.UNAUTHENTICATED_FIRST_JOIN ||
            state == PlayerState.UNAUTHENTICATED_LEGACY_FIRST_JOIN
        ) {
            if (
                PlayerSession.getAs(
                    p.getUsername(),
                    "password",
                    String.class
                ) != null
            ) {
                I18N.sendMessage(p, "event.info.join.login");
            } else {
                I18N.sendMessage(p, "event.info.join.register");
            }
        }
    }
}
