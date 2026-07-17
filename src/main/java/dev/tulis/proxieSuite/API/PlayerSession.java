package dev.tulis.proxieSuite.API;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import dev.tulis.proxieSuite.Main.Main;
import java.util.HashMap;

public class PlayerSession {

    private static HashMap<String, HashMap<String, Object>> playerCache =
        new HashMap<>();

    public PlayerSession(Main m) {
        m.getProxy().getEventManager().register(m, this);
    }

    @Subscribe
    public void disconnectEvent(DisconnectEvent event) {
        playerCache.remove(event.getPlayer().getUsername());
    }

    public static void put(String username, String key, Object value) {
        HashMap<String, Object> pc = playerCache.getOrDefault(
            username,
            new HashMap<String, Object>()
        );

        pc.put(key, value);

        playerCache.put(username, pc);
    }

    public static <T> T getAs(String username, String key, Class<T> type) {
        HashMap<String, Object> pc = playerCache.getOrDefault(
            username,
            new HashMap<String, Object>()
        );

        Object value = pc.get(key);

        if (value == null) {
            return null;
        }

        return type.cast(value);
    }

    public static void remove(String username, String key) {
        playerCache
            .getOrDefault(username, new HashMap<String, Object>())
            .remove(key);
    }
}
