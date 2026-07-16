package dev.tulis.proxieSuite.Administration;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.io.IOException;
import java.util.Map;

public final class ProxieSuiteCommand {

    private Main plugin;

    public ProxieSuiteCommand(Main m) {
        plugin = m;

        CommandManager commandManager = plugin.getProxy().getCommandManager();
        CommandMeta commandMeta = commandManager
            .metaBuilder("proxiesuite")
            .aliases("proxie")
            .plugin(plugin)
            .build();

        commandManager.register(
            commandMeta,
            createBrigadierCommand(plugin.getProxy())
        );
    }

    private BrigadierCommand createBrigadierCommand(ProxyServer proxy) {
        LiteralCommandNode<CommandSource> command =
            BrigadierCommand.literalArgumentBuilder("proxie")
                .then(reloadArgument())
                .then(versionArgument())
                .build();

        return new BrigadierCommand(command);
    }

    private LiteralArgumentBuilder<CommandSource> versionArgument() {
        LiteralArgumentBuilder<CommandSource> reloadArgument =
            BrigadierCommand.literalArgumentBuilder("version").executes(ctx -> {
                CommandSource source = ctx.getSource();

                PluginContainer this_plugin = plugin
                    .getProxy()
                    .getPluginManager()
                    .getPlugin("proxiesuite")
                    .get();

                String version = this_plugin
                    .getDescription()
                    .getVersion()
                    .orElse("unknown");

                I18N.sendMessage(
                    source,
                    "command.info.proxiesuite.version",
                    Map.of("version", version)
                );
                return Command.SINGLE_SUCCESS;
            });

        return reloadArgument;
    }

    private LiteralArgumentBuilder<CommandSource> reloadArgument() {
        LiteralArgumentBuilder<CommandSource> reloadArgument =
            BrigadierCommand.literalArgumentBuilder("reload")
                .requires(source -> {
                    return source.hasPermission("proxiesuite.proxie.reload");
                })
                .executes(ctx -> {
                    CommandSource source = ctx.getSource();

                    try {
                        boolean success = plugin.getConfig().reload();

                        if (success) {
                            I18N.sendMessage(
                                source,
                                "command.success.proxiesuite.reload"
                            );

                            return Command.SINGLE_SUCCESS;
                        }
                    } catch (IOException e) {
                        plugin.getLogger().error("Unable to reload config!", e);
                    }

                    I18N.sendMessage(
                        source,
                        "command.error.proxiesuite.reload"
                    );
                    return Command.SINGLE_SUCCESS;
                });

        return reloadArgument;
    }
}
