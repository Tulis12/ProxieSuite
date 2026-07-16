package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.tulis.proxieSuite.Main.Main;

public class Login {

    private Main plugin;

    public Login(Main m) {
        plugin = m;

        if (!plugin.getConfig().getBoolean("login.enabled")) {
            RegisteredServer main = plugin
                .getProxy()
                .getServer(plugin.getConfig().getString("servers.main"))
                .orElseThrow();

            plugin
                .getProxy()
                .getEventManager()
                .register(plugin, new ChooseInitialServerHandler(plugin, main));

            return;
        }

        plugin
            .getProxy()
            .getEventManager()
            .register(plugin, new ServerPreConnectHandler(plugin));

        plugin
            .getProxy()
            .getEventManager()
            .register(plugin, new PreLoginHandler(plugin));

        plugin
            .getProxy()
            .getEventManager()
            .register(plugin, new ChooseInitialServerHandler(plugin));

        new LoginCommand(plugin);
        new RegisterCommand(plugin);
        new Premium(plugin);
    }
}
