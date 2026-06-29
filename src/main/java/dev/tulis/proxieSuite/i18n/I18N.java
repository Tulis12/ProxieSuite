package dev.tulis.proxieSuite.i18n;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.tulis.proxieSuite.Main.Main;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class I18N {

    Path folder;
    static Main plugin;
    private static String locale;
    private static YamlDocument loadedLocale;

    public I18N(Main m) {
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

    public static String l_command(String command_syntax_key, String alias) {
        return (
            loadedLocale.getString("command.general_error.syntax") +
            " " +
            loadedLocale.getString("command.syntax." + command_syntax_key)
        )
            .replace("{alias}", alias)
            .replaceAll("(?<!\\\\)&", "§")
            .replace("\\&", "&");
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
        try {
            @SuppressWarnings("unchecked")
            List<List<String>> cmdArgs = (List<
                List<String>
            >) loadedLocale.getList("command.args." + command);

            if (args.length > cmdArgs.size()) return List.of();

            if (args.length == 0) return cmdArgs.get(0);
            return cmdArgs.get(args.length - 1);
        } catch (ClassCastException e) {
            plugin.getLogger().error("Config schema is wrong!", e);
            return List.of();
        }
    }
}
