package dev.tulis.proxieSuite.VPNDetection;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.util.Map;
import net.kyori.adventure.text.Component;

public class onHandshakeHandlerVPN {

    Main plugin;

    public onHandshakeHandlerVPN(Main m) {
        plugin = m;
    }

    @Subscribe
    public void onHandshake(PreLoginEvent event) {
        if (!plugin.getVpn().isReady()) {
            event.setResult(
                PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(I18N.l("kick.proxy_not_ready"))
                )
            );
            return;
        }

        if (
            plugin
                .getVpn()
                .check(
                    event
                        .getConnection()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
                )
        ) {
            event.setResult(
                PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(
                        I18N.l(
                            "kick.vpn_abuse",
                            Map.of(
                                "ip",
                                event
                                    .getConnection()
                                    .getRemoteAddress()
                                    .getAddress()
                                    .getHostAddress()
                            )
                        )
                    )
                )
            );

            plugin
                .getLogger()
                .info(
                    "[VPN] Player kicked during prelogin: {}",
                    event.getConnection().getRemoteAddress()
                );
            return;
        }
    }
}
