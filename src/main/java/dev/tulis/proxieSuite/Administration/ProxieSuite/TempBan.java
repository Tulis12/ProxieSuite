package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.tulis.proxieSuite.API.GetAllPlayers;
import dev.tulis.proxieSuite.Administration.BanAPI;
import dev.tulis.proxieSuite.i18n.I18N;
import java.time.ZonedDateTime;
import java.util.List;

class TempBan {

    protected static LiteralArgumentBuilder<CommandSource> tempBanArgument() {
        LiteralArgumentBuilder<CommandSource> argument =
            BrigadierCommand.literalArgumentBuilder("tempban")
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
                        .then(tempBanDurationNumberArgument())
                );

        return argument;
    }

    private static RequiredArgumentBuilder<
        CommandSource,
        Integer
    > tempBanDurationNumberArgument() {
        RequiredArgumentBuilder<CommandSource, Integer> argument =
            RequiredArgumentBuilder.<CommandSource, Integer>argument(
                "durationNumber",
                IntegerArgumentType.integer(1)
            )
                .suggests((ctx, builder) -> {
                    List<String> durations = List.of(
                        "1 years",
                        "3 months",
                        "1 weeks",
                        "3 days",
                        "10 hours",
                        "30 minutes"
                    );

                    String beginning = builder.getRemaining();

                    durations.forEach(r -> {
                        if (r.startsWith(beginning)) builder.suggest(r);
                    });

                    return builder.buildFuture();
                })
                .then(tempBanDurationUnitArgument());

        return argument;
    }

    private static RequiredArgumentBuilder<
        CommandSource,
        String
    > tempBanDurationUnitArgument() {
        RequiredArgumentBuilder<CommandSource, String> argument =
            RequiredArgumentBuilder.<CommandSource, String>argument(
                "durationUnit",
                StringArgumentType.word()
            )
                .suggests((ctx, builder) -> {
                    List<String> durations = List.of(
                        "years",
                        "months",
                        "weeks",
                        "days",
                        "hours",
                        "minutes"
                    );

                    String beginning = builder.getRemaining();

                    durations.forEach(r -> {
                        if (r.startsWith(beginning)) builder.suggest(r);
                    });

                    return builder.buildFuture();
                })
                .then(tempBanReasonArgument());

        return argument;
    }

    private static RequiredArgumentBuilder<
        CommandSource,
        String
    > tempBanReasonArgument() {
        RequiredArgumentBuilder<CommandSource, String> argument =
            RequiredArgumentBuilder.<CommandSource, String>argument(
                "reason",
                StringArgumentType.greedyString()
            ).executes(ctx -> {
                String username = ctx.getArgument("player", String.class);

                String bannedBy =
                    ctx.getSource() instanceof Player
                        ? ((Player) ctx.getSource()).getUsername()
                        : null;

                String reason = ctx.getArgument("reason", String.class);

                Integer durationNumber = ctx.getArgument(
                    "durationNumber",
                    Integer.class
                );

                String durationUnit = ctx.getArgument(
                    "durationUnit",
                    String.class
                );

                ZonedDateTime result = ZonedDateTime.now();

                switch (durationUnit) {
                    case "years": {
                        result = result.plusYears(durationNumber);
                        break;
                    }
                    case "months": {
                        result = result.plusMonths(durationNumber);
                        break;
                    }
                    case "weeks": {
                        result = result.plusWeeks(durationNumber);
                        break;
                    }
                    case "days": {
                        result = result.plusDays(durationNumber);
                        break;
                    }
                    case "hours": {
                        result = result.plusHours(durationNumber);
                        break;
                    }
                    case "minutes": {
                        result = result.plusMinutes(durationNumber);
                        break;
                    }
                    default: {
                    } // TODO: error handling
                }

                BanAPI.banPlayer(
                    username,
                    bannedBy,
                    reason,
                    result.toInstant()
                );

                return Command.SINGLE_SUCCESS;
            });

        return argument;
    }
}
