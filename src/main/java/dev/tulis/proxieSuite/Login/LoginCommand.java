package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.tulis.proxieSuite.Login.PasswordManager.PasswordState;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.i18n;
import java.util.List;
import net.kyori.adventure.text.Component;

public final class LoginCommand implements SimpleCommand {

    private Main plugin;

    public LoginCommand(Main m) {
        plugin = m;

        CommandManager commandManager = plugin.getProxy().getCommandManager();
        CommandMeta commandMeta = commandManager
            .metaBuilder("login")
            .aliases("l")
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
                    i18n.l_console("command.general_error.only_player")
                )
            );

            return;
        }

        Player p = (Player) source;

        if (
            StateManager.getPlayerState(p.getUniqueId()) ==
            PlayerState.AUTHENTICATED
        ) {
            p.sendMessage(
                Component.text(
                    i18n.l("command.error.login.already_authenticated")
                )
            );

            return;
        }

        if (args.length != 1) {
            p.sendMessage(
                Component.text(i18n.l_command("login", invocation.alias()))
            );

            return;
        }

        PasswordState valid = PasswordManager.verifyPassword(
            args[0],
            p.getUsername()
        );

        if (valid == PasswordState.INCORRECT) {
            p.sendMessage(
                Component.text(i18n.l("command.error.login.wrong_password"))
            );

            return;
        }

        if (valid == PasswordState.NOT_REGISTERED) {
            p.sendMessage(
                Component.text(i18n.l("command.error.login.not_registered"))
            );

            return;
        }

        RegisteredServer login = plugin
            .getProxy()
            .getServer(plugin.getConfig().getString("servers.main"))
            .orElseThrow();

        p.sendMessage(Component.text(i18n.l("command.success.login")));
        StateManager.setPlayerState(p.getUniqueId(), PlayerState.AUTHENTICATED);
        p.createConnectionRequest(login).fireAndForget();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) return true;

        Player p = (Player) invocation.source();
        if (
            StateManager.getPlayerState(p.getUniqueId()) ==
            PlayerState.AUTHENTICATED
        ) return false;

        return true;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        return i18n.handleSuggestion("login", args);
    }
}
