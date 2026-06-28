package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;

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

    @Subscribe
    public void onChooseInitialServerEvent(
        PlayerChooseInitialServerEvent event
    ) {
        if (main != null) {
            event.setInitialServer(main);
            return;
        }

        PlayerState state = StateManager.getPlayerState(
            event.getPlayer().getUniqueId()
        );

        if (state == PlayerState.AUTHENTICATED) {
            RegisteredServer main = plugin
                .getProxy()
                .getServer(plugin.getConfig().getString("servers.main"))
                .orElseThrow();

            event.setInitialServer(main);
        }
    }
}
