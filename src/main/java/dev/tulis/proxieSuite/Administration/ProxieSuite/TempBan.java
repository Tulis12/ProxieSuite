package dev.tulis.proxieSuite.Administration.ProxieSuite;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import java.util.List;

class TempBan {

    private RequiredArgumentBuilder<
        CommandSource,
        Integer
    > tempbanDurationNumberArgument() {
        RequiredArgumentBuilder<CommandSource, Integer> argument =
            RequiredArgumentBuilder.<CommandSource, Integer>argument(
                "durationNumber",
                IntegerArgumentType.integer(1)
            )
                .suggests((ctx, builder) -> {
                    List<String> durations = List.of(
                        "1 year",
                        "3 months",
                        "1 week",
                        "3 days",
                        "10 hours",
                        "30 minutes"
                    );

                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    return Command.SINGLE_SUCCESS;
                });
        // .then(banReasonArgument());

        return argument;
    }
}
