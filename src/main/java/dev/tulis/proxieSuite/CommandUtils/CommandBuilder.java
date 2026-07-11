package dev.tulis.proxieSuite.CommandUtils;

import dev.tulis.proxieSuite.i18n.I18N;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CommandBuilder {

    public static CommandNode load(String commandName) {
        CommandNode node = new CommandNode();

        List<Map<String, Object>> command = (List<
            Map<String, Object>
        >) I18N.getLoadedLocale().get(
            "command.commands." + commandName + ".args"
        );

        build(command, node);
        return node;
    }

    private static void build(
        List<Map<String, Object>> section,
        CommandNode node
    ) {
        for (Map<String, Object> command : section) {
            String parameter = command.keySet().iterator().next();
            Map<String, Object> args = (Map<String, Object>) command
                .values()
                .iterator()
                .next();

            CommandNode nextNode = new CommandNode();
            nextNode.name = (String) args.get("name");

            if (args.containsKey("args")) {
                List<Map<String, Object>> newCommand = (List<
                    Map<String, Object>
                >) args.get("args");

                build(newCommand, nextNode);
            }

            node.children.put(parameter, nextNode);
        }
    }
}
