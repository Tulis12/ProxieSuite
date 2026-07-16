package dev.tulis.proxieSuite.Login;

import java.util.HashMap;
import java.util.UUID;

public class StateManager {

    private static HashMap<String, PlayerState> states = new HashMap<>();

    public static enum PlayerState {
        AUTHENTICATED,
        UNAUTHENTICATED,
        UNAUTHENTICATED_LEGACY,
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
