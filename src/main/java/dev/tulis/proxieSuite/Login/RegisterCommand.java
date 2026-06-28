package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.tulis.proxieSuite.Login.StateManager.PlayerState;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.i18n;
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
                Component.text(i18n.l_cmd("command.error.only_player"))
            );

            return;
        }

        Player p = (Player) source;

        if (
            StateManager.getPlayerState(p.getUniqueId()) ==
            PlayerState.AUTHENTICATED
        ) {
            p.sendMessage(
                Component.text(i18n.l("command.error.already_authenticated"))
            );

            return;
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        if (invocation.arguments().length == 0) return List.of("haslo");
        return List.of();
    }
}
