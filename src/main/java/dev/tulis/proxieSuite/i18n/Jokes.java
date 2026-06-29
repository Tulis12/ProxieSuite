package dev.tulis.proxieSuite.i18n;

import dev.tulis.proxieSuite.Main.Main;

public class Jokes {

    private static Main plugin;

    public Jokes(Main m) {
        plugin = m;
    }

    public static boolean jokesEnabled() {
        return plugin.getConfig().getBoolean("jokes");
    }
}
