package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.util.Map;

class Version {

    private static Main plugin;

    public Version(Main m) {
        plugin = m;
    }

    protected static LiteralArgumentBuilder<CommandSource> versionArgument() {
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
}
