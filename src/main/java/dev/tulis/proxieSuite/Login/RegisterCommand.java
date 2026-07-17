package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.PlayerCache.PlayerCache;
import dev.tulis.proxieSuite.i18n.I18N;
import dev.tulis.proxieSuite.i18n.Jokes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import net.kyori.adventure.text.Component;

public final class RegisterCommand implements SimpleCommand {

    private Main plugin;

    public RegisterCommand(Main m) {
        plugin = m;

        CommandManager commandManager = plugin.getProxy().getCommandManager();
        CommandMeta commandMeta = commandManager
            .metaBuilder("register")
            .aliases("reg")
            .plugin(plugin)
            .build();

        commandManager.register(commandMeta, this);
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

        if (
            StateManager.getPlayerState(p.getUsername()) ==
            PlayerState.AUTHENTICATED
        ) {
            p.sendMessage(
                Component.text(
                    I18N.l("command.error.login.already_authenticated")
                )
            );

            return;
        }

        if (args.length != 2) {
            p.sendMessage(
                I18N.commandSyntax("command.syntax.register", invocation)
            );

            return;
        }

        if (!args[0].equals(args[1])) {
            p.sendMessage(
                Component.text(
                    I18N.l("command.error.register.password_do_not_match")
                )
            );

            return;
        }

        if (args[0].equals("password")) {
            if (Jokes.jokesEnabled()) {
                p.sendMessage(
                    Component.text(
                        I18N.l("command.joke.register.password_is_password")
                    )
                );

                return;
            }

            p.sendMessage(
                Component.text(
                    I18N.l("command.error.register.password_is_password")
                )
            );

            return;
        }

        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                "UPDATE proxie_players SET password = ? WHERE username = ? AND password IS NULL"
            );
            statement.setString(1, PasswordManager.hashPassword(args[0]));
            statement.setString(2, p.getUsername());

            int updated = statement.executeUpdate();
            if (updated == 0) {
                p.sendMessage(
                    Component.text(
                        I18N.l("command.error.register.you_already_registered")
                    )
                );

                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } // TODO: robust logging

        p.sendMessage(Component.text(I18N.l("command.success.register")));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) return true;

        Player p = (Player) invocation.source();
        if (
            StateManager.getPlayerState(p.getUsername()) ==
                PlayerState.AUTHENTICATED ||
            PlayerCache.getAs(p.getUsername(), "password", String.class) != null
        ) return false;

        return true;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) return List.of(
            I18N.l("command.info.register.password")
        );

        if (args.length <= 2) return List.of(
            I18N.l("command.info.register.repeat_password")
        );

        return List.of();
    }
}
