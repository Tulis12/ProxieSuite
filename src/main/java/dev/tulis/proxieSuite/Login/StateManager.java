package dev.tulis.proxieSuite.Login;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import dev.tulis.proxieSuite.Main.Main;
import java.util.HashMap;

public class StateManager {

    private static HashMap<String, PlayerState> states = new HashMap<>();

    public StateManager(Main m) {
        m.getProxy().getEventManager().register(m, this);
    }

    @Subscribe
    public void disconnectEvent(DisconnectEvent event) {
        states.remove(event.getPlayer().getUsername());
    }

    public static enum PlayerState {
        AUTHENTICATED,
        UNAUTHENTICATED,
        UNAUTHENTICATED_FIRST_JOIN,
        UNAUTHENTICATED_LEGACY,
        UNAUTHENTICATED_LEGACY_FIRST_JOIN,
    }

    public static PlayerState getPlayerState(String username) {
        return states.getOrDefault(username, PlayerState.UNAUTHENTICATED);
    }

    public static void setPlayerState(String username, PlayerState state) {
        states.put(username, state);
    }

    public static void removePlayer(String username) {
        states.remove(username);
    }
}
