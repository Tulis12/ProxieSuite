package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import dev.tulis.proxieSuite.Main.Main;

public final class ProxieSuiteCommand {

    private Main plugin;

    public ProxieSuiteCommand(Main m) {
        plugin = m;

        new Reload(plugin);
        new Version(plugin);

        CommandManager commandManager = plugin.getProxy().getCommandManager();
        CommandMeta commandMeta = commandManager
            .metaBuilder("proxiesuite")
            .aliases("proxie")
            .plugin(plugin)
            .build();

        commandManager.register(commandMeta, createBrigadierCommand());
    }

    private BrigadierCommand createBrigadierCommand() {
        LiteralCommandNode<CommandSource> command =
            BrigadierCommand.literalArgumentBuilder("proxie")
                .then(Reload.reloadArgument())
                .then(Version.versionArgument())
                .then(Ban.banArgument())
                .build();

        return new BrigadierCommand(command);
    }
}
