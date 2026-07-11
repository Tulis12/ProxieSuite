package dev.tulis.proxieSuite.i18n;

import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.velocitypowered.api.proxy.Player;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.tulis.proxieSuite.CommandUtils.CommandBuilder;
import dev.tulis.proxieSuite.CommandUtils.CommandNode;
import dev.tulis.proxieSuite.Main.Main;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public class I18N {

    Path folder;
    static Main plugin;
    private static String locale;

    @Getter
    private static YamlDocument loadedLocale;

    public I18N(Main m) {
        new Jokes(m);

        plugin = m;
        folder = plugin.getDataDirectory().resolve("i18n");
        folder.toFile().mkdir();

        locale = plugin.getConfig().getString("locale");
        load();
    }

    public void load() {
        try {
            File file = new File(folder.toFile(), locale + ".yaml");

            loadedLocale = YamlDocument.create(
                file,
                Objects.requireNonNull(
                    getClass().getResourceAsStream("/i18n/" + locale + ".yaml")
                ),
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                    .setVersioning(new BasicVersioning("file_version"))
                    .setOptionSorting(
                        UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS
                    )
                    .build()
            );

            loadedLocale.update();
            loadedLocale.save();
        } catch (IOException e) {
            plugin
                .getLogger()
                .error("Cannot load language: {}.yaml", locale, e);
        }
    }

    public static String l_console(String key) {
        return loadedLocale.getString(key).replaceAll("(?<!\\\\)&.", "");
    }

    public static String l(String key) {
        return loadedLocale
            .getString(key)
            .replaceAll("(?<!\\\\)&", "§")
            .replace("\\&", "&");
    }

    public static Component commandSyntax(
        String command,
        Invocation invocation
    ) {
        String baseRoute = "command.commands." + command;
        StringBuilder route = new StringBuilder(baseRoute);

        for (String args : invocation.arguments()) {
            route.append(".args.").append(args);
        }

        route.append(".syntax");
        String message = loadedLocale.getString(route.toString());
        if (message == null) {
            message = loadedLocale.getString(baseRoute + ".syntax");
        }

        message = message.replace("{alias}", invocation.alias());

        if (invocation instanceof Player) {
            message = message.replaceAll("(?<!\\\\)&", "§").replace("\\&", "&");
        } else {
            message = message.replaceAll("(?<!\\\\)&.", "");
        }

        return Component.text(message);
    }

    public static String l(String key, Map<String, Object> placeholders) {
        String msg = loadedLocale.getString(key);

        if (msg == null) {
            throw new NullPointerException(
                "Message " + key + " does not exist in loaded locale: " + locale
            );
        }

        msg = msg.replaceAll("(?<!\\\\)&", "§").replace("\\&", "&");

        if (placeholders != null) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                msg = msg.replace(
                    "{" + entry.getKey() + "}",
                    String.valueOf(entry.getValue())
                );
            }
        }

        return msg;
    }

    public static String l_console(
        String key,
        Map<String, Object> placeholders
    ) {
        String msg = loadedLocale.getString(key);

        if (msg == null) {
            throw new NullPointerException(
                "Message " + key + " does not exist in loaded locale: " + locale
            );
        }

        msg = msg.replaceAll("(?<!\\\\)&.", "");

        if (placeholders != null) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                msg = msg.replace(
                    "{" + entry.getKey() + "}",
                    String.valueOf(entry.getValue())
                );
            }
        }

        return msg;
    }

    public static List<String> handleSuggestion(String command, String[] args) {
        CommandNode node = CommandBuilder.load(command);

        if (node == null) {
            return List.of();
        }

        for (int i = 0; i < args.length - 1; i++) {
            node = node.children.get(args[i]);

            if (node == null) {
                return List.of();
            }
        }

        String prefix = args.length == 0 ? "" : args[args.length - 1];

        return node.children
            .keySet()
            .stream()
            .filter(name -> name.startsWith(prefix))
            .toList();
    }
}
