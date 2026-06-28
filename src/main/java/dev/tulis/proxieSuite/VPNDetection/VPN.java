package dev.tulis.proxieSuite.VPNDetection;

import dev.tulis.proxieSuite.Main.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import lombok.Getter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class VPN {

    boolean enabled;

    @Getter
    boolean ready;

    Main plugin;
    HashSet<String> vpnIps = new HashSet<>();

    public VPN(Main m) {
        plugin = m;

        enabled = plugin.getConfig().getBoolean("vpn.enabled");
        if (!enabled) return;

        plugin
            .getProxy()
            .getEventManager()
            .register(plugin, new onHandshakeHandlerVPN(plugin));

        plugin
            .getProxy()
            .getScheduler()
            .buildTask(plugin, () -> {
                fetchRepo();
                loadToMemory();
                ready = true;
            })
            .schedule();
    }

    private void fetchRepo() {
        String repoUrl = plugin.getConfig().getString("vpn.is_vpn_repo");
        Path localPath = plugin.getDataDirectory().resolve("vpn_detection");

        File localRepo = localPath.toFile();

        try {
            if (
                !localRepo.exists() ||
                !localPath.resolve(".git").toFile().exists()
            ) {
                System.out.println("Klonowanie VPN...");
                Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localRepo)
                    .call();
                System.out.println("Udało się!");
            } else {
                System.out.println("Pobieranie aktualizacji!");
                Git git = Git.open(localRepo);
                git.pull().call();
            }
        } catch (GitAPIException | IOException e) {
            plugin
                .getLogger()
                .error(
                    "Cannot fetch/pull {} repository for VPN detection!",
                    repoUrl,
                    e
                );
        }
    }

    private void loadToMemory() {
        try (
            BufferedReader reader = new BufferedReader(
                new FileReader(
                    plugin
                        .getDataDirectory()
                        .resolve("vpn_detection")
                        .resolve("vpn-or-datacenter-ipv4-ranges.txt")
                        .toFile()
                )
            );
        ) {
            String line;
            vpnIps = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                vpnIps.add(line);
            }
        } catch (IOException e) {
            plugin
                .getLogger()
                .error("Cannot load vpn-ranges file for VPN detection!", e);
        }
    }

    public boolean check(String address) {
        int slash = address.indexOf('/');

        String ip = slash == -1 ? address : address.substring(slash + 1);

        int ipInt;
        try {
            ipInt = parseIpv4(ip);
        } catch (IllegalArgumentException e) {
            return false;
        }

        for (int prefix = 32; prefix >= 0; prefix--) {
            int mask = prefix == 0 ? 0 : -1 << (32 - prefix);
            int network = ipInt & mask;

            String networkStr = networkToString(network, prefix);

            if (vpnIps.contains(networkStr)) {
                return true;
            }
        }

        return false;
    }

    private static int parseIpv4(String ipStr) {
        int ip = 0;
        int value = 0;
        int parts = 0;

        for (int i = 0; i < ipStr.length(); i++) {
            char c = ipStr.charAt(i);

            if (c == '.') {
                ip = (ip << 8) | (value & 0xFF);
                value = 0;
                parts++;
            } else if (c >= '0' && c <= '9') {
                value = value * 10 + (c - '0');
            } else {
                throw new IllegalArgumentException("Invalid IP");
            }
        }

        ip = (ip << 8) | (value & 0xFF);
        parts++;

        if (parts != 4) {
            throw new IllegalArgumentException("Invalid IP");
        }

        return ip;
    }

    private static String networkToString(int network, int prefix) {
        return (
            ((network >>> 24) & 255) +
            "." +
            ((network >>> 16) & 255) +
            "." +
            ((network >>> 8) & 255) +
            "." +
            (network & 255) +
            "/" +
            prefix
        );
    }
}
