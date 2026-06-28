package dev.tulis.proxieSuite.Login;

import java.util.HashMap;
import java.util.UUID;

public class StateManager {

    private static HashMap<UUID, PlayerState> states = new HashMap<>();

    public static enum PlayerState {
        AUTHENTICATED,
        UNAUTHENTICATED,
    }

    public static PlayerState getPlayerState(UUID uuid) {
        return states.getOrDefault(uuid, PlayerState.UNAUTHENTICATED);
    }

    public static void setPlayerState(UUID uuid, PlayerState state) {
        states.put(uuid, state);
    }

    public static void removePlayer(UUID uuid) {
        states.remove(uuid);
    }
}
