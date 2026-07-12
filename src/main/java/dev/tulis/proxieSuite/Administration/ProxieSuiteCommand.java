package dev.tulis.proxieSuite.Administration;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.tulis.proxieSuite.CommandUtils.CommandPermission;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.io.IOException;
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

        if (!CommandPermission.handlePermission("proxiesuite", invocation)) {
            return;
        }

        if (args.length == 0) {
            source.sendMessage(I18N.commandSyntax("proxiesuite", invocation));
            return;
        }

        switch (args[0]) {
            case "reload": {
                try {
                    boolean success = plugin.getConfig().reload();

                    if (success) {
                        I18N.sendMessage(
                            source,
                            "command.success.proxiesuite.reload"
                        );

                        return;
                    }
                } catch (IOException e) {
                    plugin.getLogger().error("Unable to reload config!", e);
                }

                I18N.sendMessage(source, "command.error.proxiesuite.reload");

                break;
            }
            default: {
                source.sendMessage(
                    I18N.commandSyntax("proxiesuite", invocation)
                );
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        boolean result = CommandPermission.handlePermissionPrediction(
            "proxiesuite",
            invocation
        );

        return result;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return I18N.handleSuggestion("proxiesuite", invocation);
    }
}
