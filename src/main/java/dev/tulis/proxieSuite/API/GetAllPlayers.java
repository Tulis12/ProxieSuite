package dev.tulis.proxieSuite.API;

import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Main.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GetAllPlayers
 */
public class GetAllPlayers {

    private static long MS_BETWEEN_FETCHES = 20_000;

    private static Long lastFetch;
    private static List<String> lastFetchResults = new ArrayList<>();
    private static Main plugin;

    public GetAllPlayers(Main m) {
        plugin = m;
    }

    private static List<String> fetchFromDb() {
        List<String> players = new ArrayList<>();

        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                "SELECT username FROM proxie_players"
            );

            ResultSet set = statement.executeQuery();
            while (set.next()) {
                players.add(set.getString("username"));
            }

            return players;
        } catch (SQLException e) {
            plugin
                .getLogger()
                .error("Error occured during query to the database!", e);
        }

        return plugin
            .getProxy()
            .getAllPlayers()
            .stream()
            .map(p -> p.getUsername())
            .toList();
    }

    public static List<String> getAllKnownPlayers() {
        if (lastFetch == null) {
            lastFetch = System.currentTimeMillis();
            lastFetchResults = fetchFromDb();
            return lastFetchResults;
        }

        long delta = System.currentTimeMillis() - lastFetch;

        if (delta > MS_BETWEEN_FETCHES) {
            lastFetch = System.currentTimeMillis();
            lastFetchResults = fetchFromDb();
            return lastFetchResults;
        }

        return lastFetchResults;
    }
}
