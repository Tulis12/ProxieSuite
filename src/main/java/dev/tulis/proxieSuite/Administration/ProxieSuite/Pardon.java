package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import dev.tulis.proxieSuite.Administration.BanAPI;

class Pardon {

    protected static LiteralArgumentBuilder<CommandSource> pardonArgument() {
        LiteralArgumentBuilder<CommandSource> argument =
            BrigadierCommand.literalArgumentBuilder("pardon")
                .requires(source -> {
                    return source.hasPermission("proxiesuite.proxie.pardon");
                })
                .then(
                    RequiredArgumentBuilder.<CommandSource, String>argument(
                        "player",
                        StringArgumentType.word()
                    )
                        .suggests((context, builder) -> {
                            String start = builder.getRemaining();
                            for (String username : BanAPI.getBans().keySet()) {
                                if (username.startsWith(start)) builder.suggest(
                                    username
                                );
                            }

                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String username = ctx.getArgument(
                                "player",
                                String.class
                            );

                            BanAPI.pardonPlayer(username);
                            return Command.SINGLE_SUCCESS;
                        })
                );

        return argument;
    }
}
