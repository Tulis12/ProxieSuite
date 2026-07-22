package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.io.IOException;

class Reload {

    private static Main plugin;

    public Reload(Main m) {
        plugin = m;
    }

    protected static LiteralArgumentBuilder<CommandSource> reloadArgument() {
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
