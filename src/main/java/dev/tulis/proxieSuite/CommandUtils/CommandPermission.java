package dev.tulis.proxieSuite.CommandUtils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand.Invocation;
import dev.tulis.proxieSuite.i18n.I18N;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandPermission {

    public static boolean handlePermission(
        String command,
        Invocation invocation
    ) {
        String rootPermission = CommandBuilder.getCommands().getString(
            "commands." + command + ".permission"
        );

        if (rootPermission.equals("proxiesuite.everyone")) {
            return true;
        }

        String[] args = invocation.arguments();
        CommandNode cnode = CommandBuilder.load(command, invocation.source());

        CommandNode lastFoundWithPermission = null;

        for (String arg : args) {
            if (cnode != null) cnode = cnode.children.get(arg);

            if (cnode != null) {
                if (cnode.permission != null) lastFoundWithPermission = cnode;
            }
        }

        CommandSource source = invocation.source();

        if (
            !source.hasPermission(lastFoundWithPermission.permission) &&
            !lastFoundWithPermission.permission.equals(
                "proxiesuite.everyone"
            ) &&
            !source.hasPermission(rootPermission)
        ) {
            I18N.sendMessage(
                source,
                "command.general_error.insufficient_permissions"
            );
            return false;
        }

        return true;
    }

    public static boolean handlePermissionPrediction(
        String command,
        Invocation invocation
    ) {
        String startRoute = "commands." + command;
        StringBuilder route = new StringBuilder(startRoute);

        String rootPermission = CommandBuilder.getCommands().getString(
            route + ".permission"
        );

        if (rootPermission.equals("proxiesuite.everyone")) {
            return true;
        }

        String[] args = invocation.arguments();

        CommandNode cnode = CommandBuilder.load(command, invocation.source());

        CommandNode lastFound = null;
        int countSinceLastFound = 0;
        CommandNode lastFoundWithPermission = null;

        for (String arg : args) {
            if (cnode != null) cnode = cnode.children.get(arg);

            if (cnode != null) {
                lastFound = cnode;
                if (cnode.permission != null) lastFoundWithPermission = cnode;
            } else {
                countSinceLastFound++;
            }
        }

        // Args are empty
        if (args.length == 0) {
            List<Map<?, ?>> possibleArgs =
                CommandBuilder.getCommands().getMapList(
                    route.toString() + ".args",
                    new ArrayList<Map<?, ?>>()
                );

            if (possibleArgs.isEmpty()) {
                return (
                    invocation
                        .source()
                        .hasPermission(lastFoundWithPermission.permission) ||
                    invocation.source().hasPermission(rootPermission) ||
                    lastFoundWithPermission.permission.equals(
                        "proxiesuite.everyone"
                    )
                );
            }

            for (Map<?, ?> possibleArg : possibleArgs) {
                if (!possibleArg.containsKey("permission")) {
                    return true;
                }

                String value = (String) possibleArg.get("permission");

                if (
                    invocation.source().hasPermission(value) ||
                    value.equals("proxiesuite.everyone")
                ) return true;
            }
        }

        // Predictions whether you can execute the next command
        CommandNode node = CommandBuilder.load(command, invocation.source());

        for (int i = 0; i < args.length - 1; i++) {
            node = node.children.get(args[i]);
            if (node == null) {
                return (
                    invocation
                        .source()
                        .hasPermission(lastFoundWithPermission.permission) ||
                    invocation.source().hasPermission(rootPermission) ||
                    lastFoundWithPermission.permission.equals(
                        "proxiesuite.everyone"
                    )
                );
            }
        }

        String prefix = args.length == 0 ? "" : args[args.length - 1];
        boolean predictionIsEmpty = node.children
            .keySet()
            .stream()
            .filter(name -> name.startsWith(prefix))
            .toList()
            .isEmpty();

        if (!predictionIsEmpty) {
            return true;
        }

        if (
            lastFound != null &&
            lastFound.paramType != null &&
            countSinceLastFound <= 1
        ) {
            return true;
        }

        return false;
    }
}
