package dev.tulis.proxieSuite.CommandUtils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.tulis.proxieSuite.CommandUtils.CommandNode.ParamType;
import dev.tulis.proxieSuite.Main.Main;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

@Deprecated
@SuppressWarnings("unchecked")
public class CommandBuilder {

    private static Main plugin;

    @Getter
    private static YamlDocument commands;

    public CommandBuilder(Main m) {
        plugin = m;

        try {
            commands = YamlDocument.create(
                Objects.requireNonNull(
                    getClass().getResourceAsStream("/commands.yaml")
                )
            );
        } catch (IOException e) {
            plugin
                .getLogger()
                .error("Cannot load internal commands.yaml file!", e);
        }
    }

    public static CommandNode load(String commandName, CommandSource source) {
        CommandNode node = new CommandNode();

        List<Map<String, Object>> command = (List<
            Map<String, Object>
        >) commands.get("commands." + commandName + ".args");

        String basePermission = commands.getString(
            "commands." + commandName + ".permission"
        );

        build(command, node, source, basePermission);
        return node;
    }

    private static void build(
        List<Map<String, Object>> section,
        CommandNode node,
        CommandSource source,
        String basePermission
    ) {
        for (Map<String, Object> command : section) {
            CommandNode nextNode = new CommandNode();
            nextNode.name = (String) command.get("name");

            if (command.containsKey("permission")) {
                if (
                    !source.hasPermission(basePermission) &&
                    !source.hasPermission((String) command.get("permission")) &&
                    !"proxiesuite.everyone".equals(command.get("permission"))
                ) continue;

                nextNode.permission = (String) command.get("permission");
            }

            if (command.containsKey("args")) {
                List<Map<String, Object>> newCommand = (List<
                    Map<String, Object>
                >) command.get("args");

                build(newCommand, nextNode, source, basePermission);
            }

            if (command.containsKey("params")) {
                nextNode.paramType = ParamType.valueOf(
                    ((String) command.get("params")).toUpperCase()
                );

                switch (nextNode.paramType) {
                    case PLAYERS: {
                        for (Player ps : plugin.getProxy().getAllPlayers()) {
                            CommandNode playerNode = new CommandNode();
                            playerNode.name = ps.getUsername();
                            nextNode.children.put(ps.getUsername(), playerNode);
                        }

                        break;
                    }
                    case STATIC: {
                        CommandNode staticNode = new CommandNode();
                        staticNode.name = (String) command.get("static");
                        nextNode.children.put(
                            (String) command.get("static"),
                            staticNode
                        );
                    }
                    case DUMMY: {
                        break;
                    }
                }
            }

            node.children.put((String) command.get("name"), nextNode);
        }
    }
}
