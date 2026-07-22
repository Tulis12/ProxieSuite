package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.tulis.proxieSuite.API.GetAllPlayers;
import dev.tulis.proxieSuite.Administration.BanAPI;
import dev.tulis.proxieSuite.i18n.I18N;

class Ban {

    protected static LiteralArgumentBuilder<CommandSource> banArgument() {
        LiteralArgumentBuilder<CommandSource> argument =
            BrigadierCommand.literalArgumentBuilder("ban")
                .requires(source -> {
                    return source.hasPermission("proxiesuite.proxie.ban");
                })
                .then(
                    RequiredArgumentBuilder.<CommandSource, String>argument(
                        "player",
                        StringArgumentType.word()
                    )
                        .suggests((context, builder) -> {
                            String start = builder.getRemaining();
                            for (String username : GetAllPlayers.getAllKnownPlayers()) {
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

                            String bannedBy =
                                ctx.getSource() instanceof Player
                                    ? ((Player) ctx.getSource()).getUsername()
                                    : null;

                            String reason = I18N.l("kick.default_ban_reason");

                            BanAPI.banPlayer(username, bannedBy, reason);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(banReasonArgument())
                );

        return argument;
    }

    protected static RequiredArgumentBuilder<
        CommandSource,
        String
    > banReasonArgument() {
        RequiredArgumentBuilder<CommandSource, String> argument =
            RequiredArgumentBuilder.<CommandSource, String>argument(
                "reason",
                StringArgumentType.greedyString()
            ).executes(ctx -> {
                String username = ctx.getArgument("player", String.class);

                String bannedBy =
                    ctx.getSource() instanceof Player
                        ? ((Player) ctx.getSource()).getUsername()
                        : "Console";

                String reason = ctx.getArgument("reason", String.class);

                BanAPI.banPlayer(username, bannedBy, reason);
                return Command.SINGLE_SUCCESS;
            });

        return argument;
    }
}
