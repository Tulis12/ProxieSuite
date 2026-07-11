package dev.tulis.proxieSuite.Administration;

import dev.tulis.proxieSuite.Main.Main;

public class ProxieSuite {

    private Main plugin;

    public ProxieSuite(Main m) {
        plugin = m;

        new ProxieSuiteCommand(plugin);
    }
}
