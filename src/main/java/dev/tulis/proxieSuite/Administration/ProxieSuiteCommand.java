package dev.tulis.proxieSuite.Administration;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.util.List;

public final class ProxieSuiteCommand implements SimpleCommand {

    private Main plugin;

    public ProxieSuiteCommand(Main m) {
        plugin = m;

        CommandManager commandManager = plugin.getProxy().getCommandManager();
        CommandMeta commandMeta = commandManager
            .metaBuilder("proxiesuite")
            .aliases("proxie")
            .plugin(plugin)
            .build();

        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            source.sendMessage(I18N.commandSyntax("proxiesuite", invocation));
            return;
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        return I18N.handleSuggestion("proxiesuite", args);
    }
}
