package dev.tulis.proxieSuite.Administration;

import dev.tulis.proxieSuite.Administration.ProxieSuite.ProxieSuiteCommand;
import dev.tulis.proxieSuite.Main.Main;

public class Administration {

    private Main plugin;

    public Administration(Main m) {
        plugin = m;

        new ProxieSuiteCommand(plugin);
        new BanAPI(plugin);
    }
}
