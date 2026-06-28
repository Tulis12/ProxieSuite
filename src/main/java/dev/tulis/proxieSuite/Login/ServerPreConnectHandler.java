package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent.ServerResult;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.i18n;
import net.kyori.adventure.text.Component;

public class ServerPreConnectHandler {

    private Main plugin;

    public ServerPreConnectHandler(Main m) {
        plugin = m;
    }

    @Subscribe
    public void onPreServerConnect(ServerPreConnectEvent event) {
        RegisteredServer target = event.getOriginalServer();

        try {
            PlayerState state = StateManager.getPlayerState(
                event.getPlayer().getUniqueId()
            );

            if (state == PlayerState.AUTHENTICATED) {
                return;
            }

            String targetServerName = target.getServerInfo().getName();

            if (
                !targetServerName.equals(
                    plugin.getConfig().getString("servers.login")
                )
            ) {
                event
                    .getPlayer()
                    .sendMessage(
                        Component.text(
                            i18n.l("event.error.change_server.not_logged_in")
                        )
                    );
                event.setResult(ServerResult.denied());
            }
        } catch (Exception e) {
            plugin
                .getLogger()
                .error(
                    "Cannot forward player to another server as it may be unsafe!",
                    e
                );

            event.setResult(ServerResult.denied());
            event
                .getPlayer()
                .sendMessage(
                    Component.text(
                        "§4A critical error occured in the server. Cannot forward you to another server as it may be unsafe, sorry for trouble. Please contact admins or create issue at: https://github.com/Tulis12/ProxieSuite"
                    )
                );
        }
    }
}
