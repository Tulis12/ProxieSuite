package dev.tulis.proxieSuite.Main;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.tulis.proxieSuite.API.LogFilter;
import dev.tulis.proxieSuite.Administration.ProxieSuite;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Login.Login;
import dev.tulis.proxieSuite.Login.StateManager;
import dev.tulis.proxieSuite.PlayerCache.PlayerCache;
import dev.tulis.proxieSuite.VPNDetection.VPN;
import dev.tulis.proxieSuite.i18n.I18N;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.slf4j.Logger;

@Plugin(id = "proxiesuite", name = "ProxieSuite", version = "0.1-SNAPSHOT")
public class Main {

    @Getter
    private Logger logger;

    @Getter
    private YamlDocument config;

    @Getter
    private ProxyServer proxy;

    @Getter
    private Path dataDirectory;

    @Getter
    private VPN vpn;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        new Database(this);

        vpn = new VPN(this);
        new I18N(this);

        new Login(this);
        new ProxieSuite(this);
        new StateManager(this);
        new PlayerCache(this);
    }

    @Inject
    public Main(
        ProxyServer server,
        Logger logger,
        @DataDirectory Path dataDirectory
    ) {
        LogFilter.registerFilter();

        this.proxy = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        try {
            config = YamlDocument.create(
                new File(dataDirectory.toFile(), "config.yaml"),
                Objects.requireNonNull(
                    getClass().getResourceAsStream("/config.yaml")
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

            config.update();
            config.save();
        } catch (IOException e) {
            e.printStackTrace(); // TODO: More robust logging
            Optional<PluginContainer> container = server
                .getPluginManager()
                .getPlugin("proxiesuite");
            container.ifPresent(pluginContainer ->
                pluginContainer.getExecutorService().shutdown()
            );
        }
    }

    public void runTask(Runnable runnable) {
        proxy.getScheduler().buildTask(this, runnable).schedule();
    }
}
