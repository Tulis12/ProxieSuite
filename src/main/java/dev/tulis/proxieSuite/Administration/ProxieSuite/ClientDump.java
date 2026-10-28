package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

class ClientDump {

    private static Component c(String text) {
        return Component.text(text);
    }

    protected static LiteralArgumentBuilder<CommandSource> clientDumpArgument() {
        LiteralArgumentBuilder<CommandSource> reloadArgument =
            BrigadierCommand.literalArgumentBuilder("clientdump")
                .requires(source -> {
                    return source.hasPermission(
                        "proxiesuite.proxie.debug.clientdump"
                    );
                })
                .executes(ctx -> {
                    CommandSource source = ctx.getSource();

                    if (source instanceof Player) {
                        Player p = (Player) source;

                        p.sendMessage(
                            c("Country: " + p.getEffectiveLocale().getCountry())
                        );

                        p.sendMessage(c("GameProfile: "));

                        p.getGameProfile()
                            .getProperties()
                            .forEach(r -> {
                                String name = r.getName();
                                String val = r.getValue();

                                if (val.length() > 15) {
                                    val = val.substring(0, 15) + "...";
                                }

                                p.sendMessage(c("  " + name + ": " + val));
                            });

                        p.sendMessage(
                            c(
                                "Language: " +
                                    p.getEffectiveLocale().getLanguage()
                            )
                        );
                    }

                    return Command.SINGLE_SUCCESS;
                });

        return reloadArgument;
    }
}
