package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.PlayerCache.PlayerCache;
import dev.tulis.proxieSuite.i18n.I18N;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import net.kyori.adventure.text.Component;

public final class Premium implements SimpleCommand {

    private Main plugin;

    public Premium(Main m) {
        plugin = m;

        CommandManager commandManager = plugin.getProxy().getCommandManager();
        CommandMeta commandMeta = commandManager
            .metaBuilder("premium")
            .plugin(plugin)
            .build();

        commandManager.register(commandMeta, this);
    }

    private boolean ensurePremiumAvaliable(String username) {
        Boolean onlineAccountExists = PlayerCache.getAs(
            username,
            "onlineAccountExists",
            Boolean.class
        );

        return onlineAccountExists;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(
                Component.text(
                    I18N.l_console("command.general_error.only_player")
                )
            );

            return;
        }

        Player p = (Player) source;

        if (p.getUniqueId().version() == 4) {
            p.sendMessage(
                Component.text(
                    I18N.l("command.error.premium.you_are_already_premium")
                )
            );
        }

        if (args.length == 1) {
            if (
                args[0].equals("confirm") &&
                ensurePremiumAvaliable(p.getUsername())
            ) {
                try (Connection conn = Database.getConnection()) {
                    PreparedStatement statement = conn.prepareStatement(
                        "UPDATE proxie_players SET onlineauth = 1 WHERE username = ?"
                    );

                    statement.setString(1, p.getUsername());

                    statement.executeUpdate();
                } catch (SQLException e) {
                    plugin
                        .getLogger()
                        .error("Database exception at enabling premium!", e);

                    I18N.sendMessage(p, "command.general_error.server_error");
                    return;
                }

                p.disconnect(Component.text(I18N.l("command.success.premium")));
                return;
            }

            p.sendMessage(I18N.commandSyntax("premium", invocation));
            return;
        }

        if (ensurePremiumAvaliable(p.getUsername())) {
            I18N.sendMessage(p, "command.info.premium.are_you_sure");
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) return true;

        return ((Player) invocation.source()).getUniqueId().version() != 4;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) return List.of("confirm");

        return List.of();
    }
}
