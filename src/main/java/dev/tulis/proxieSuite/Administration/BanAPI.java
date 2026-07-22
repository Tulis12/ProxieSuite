package dev.tulis.proxieSuite.Administration;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.proxy.Player;
import dev.tulis.proxieSuite.API.PlayerSession;
import dev.tulis.proxieSuite.Database.Database;
import dev.tulis.proxieSuite.Main.Main;
import dev.tulis.proxieSuite.i18n.I18N;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;

public class BanAPI {

    private static final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static HashMap<String, Ban> bans = new HashMap<>();
    private static Main plugin;

    public BanAPI(Main m) {
        plugin = m;
        refreshCache();
    }

    private static Ban isBanned(String username) {
        if (!bans.containsKey(username)) return null;

        LocalDateTime now = LocalDateTime.now();

        Ban ban = bans.get(username);
        if (ban.end == null) return ban;

        boolean expired = ban.end.isBefore(now);
        if (!expired) return ban;
        return null;
    }

    public static boolean handlePreLogin(PreLoginEvent event) {
        Ban ban = BanAPI.isBanned(event.getUsername());

        if (ban == null) {
            return false;
        }

        if (ban.end != null) {
            LocalDateTime now = LocalDateTime.now();

            long years = ChronoUnit.YEARS.between(now, ban.end);
            now = now.plusYears(years);

            long months = ChronoUnit.MONTHS.between(now, ban.end);
            now = now.plusMonths(months);

            long weeks = ChronoUnit.WEEKS.between(now, ban.end);
            now = now.plusWeeks(weeks);

            long days = ChronoUnit.DAYS.between(now, ban.end);
            now = now.plusDays(days);

            long hours = ChronoUnit.HOURS.between(now, ban.end);
            now = now.plusHours(hours);

            long minutes = ChronoUnit.MINUTES.between(now, ban.end);

            List<String> parts = new ArrayList<>();

            if (years > 0) parts.add(years + " years");
            if (months > 0) parts.add(months + " months");
            if (weeks > 0) parts.add(weeks + " weeks");
            if (days > 0) parts.add(days + " days");
            if (hours > 0) parts.add(hours + " hours");
            if (minutes > 0) parts.add(minutes + " minutes");

            Map<String, Object> placeholders = Map.of(
                "reason",
                ban.reason,

                "bannedBy",
                ban.bannedBySafe,

                "end",
                String.join(" ", parts),

                "endDate",
                ban.end.format(formatter)
            );

            event.setResult(
                PreLoginComponentResult.denied(
                    Component.text(I18N.l("kick.tempban", placeholders))
                )
            );

            return true;
        }

        Map<String, Object> placeholders = Map.of(
            "reason",
            ban.reason,

            "bannedBy",
            ban.bannedBySafe
        );

        event.setResult(
            PreLoginComponentResult.denied(
                Component.text(I18N.l("kick.ban", placeholders))
            )
        );

        return true;
    }

    public static void refreshCache() {
        bans.clear();

        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                """
                SELECT
                    b.id,
                    p.username AS player,
                    bp.username AS banned_by,
                    b.reason,
                    b.start,
                    b.end
                FROM proxie_bans b
                JOIN proxie_players p
                    ON b.player_id = p.id
                LEFT JOIN proxie_players bp
                    ON b.banned_by_id = bp.id
                WHERE b.end IS NULL OR CURRENT_TIMESTAMP() < b.end
                """
            );

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                String player = set.getString("player");
                String bannedBy = set.getString("banned_by");
                String reason = set.getString("reason");
                LocalDateTime start =
                    set.getTimestamp("start") != null
                        ? set.getTimestamp("start").toLocalDateTime()
                        : null;

                LocalDateTime end =
                    set.getTimestamp("end") != null
                        ? set.getTimestamp("end").toLocalDateTime()
                        : null;

                bans.put(player, new Ban(player, bannedBy, reason, start, end));
            }
        } catch (SQLException e) {
            plugin
                .getLogger()
                .error("Error occured during query to the database!", e);
        }
    }

    public static void banPlayer(
        String username,
        String bannedBy,
        String reason
    ) {
        banPlayer(username, bannedBy, reason, null);
    }

    public static void banPlayer(
        String username,
        String bannedBy,
        String reason,
        LocalDateTime end
    ) {
        Integer playerId = null;
        Integer bannedById = null;

        Player cel = plugin.getProxy().getPlayer(username).orElse(null);
        if (cel != null) {
            Component reasonComponent;

            if (end == null) {
                Map<String, Object> placeholders = Map.of(
                    "reason",
                    reason,

                    "bannedBy",
                    bannedBy
                );

                reasonComponent = Component.text(
                    I18N.l("kick.ban", placeholders)
                );
            } else {
                Map<String, Object> placeholders = Map.of(
                    "reason",
                    reason,

                    "bannedBy",
                    bannedBy,

                    "end",
                    end
                );

                reasonComponent = Component.text(
                    I18N.l("kick.tempban", placeholders)
                );
            }

            playerId = PlayerSession.getAs(username, "id", Integer.class);
            cel.disconnect(reasonComponent);
        } else {
            try (Connection conn = Database.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(
                    "SELECT id FROM proxie_players WHERE username = ?"
                );

                statement.setString(1, username);
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    playerId = set.getInt("id");
                }
            } catch (SQLException e) {
                plugin
                    .getLogger()
                    .error("Error occured during query to the database!", e);
            }
        }

        if (bannedBy != null) {
            bannedById = PlayerSession.getAs(bannedBy, "id", Integer.class);
        }

        try (Connection conn = Database.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                """
                INSERT INTO proxie_bans(
                    player_id,
                    banned_by_id,
                    reason,
                    end
                ) VALUES (?, ?, ?, ?)
                """
            );

            statement.setInt(1, playerId);
            statement.setObject(2, bannedById, Types.INTEGER);
            statement.setString(3, reason);

            Timestamp timestamp = end != null ? Timestamp.valueOf(end) : null;
            statement.setTimestamp(4, timestamp);

            statement.executeUpdate();
        } catch (SQLException e) {
            plugin
                .getLogger()
                .error("Error occured during query to the database!", e);
        }

        refreshCache();
    }

    public static class Ban {

        public Ban(
            String username,
            String bannedBy,
            String reason,
            LocalDateTime start,
            LocalDateTime end
        ) {
            this.username = username;
            this.bannedBy = bannedBy;

            if (bannedBy == null) {
                this.bannedBySafe = I18N.l("utils.console");
            } else {
                this.bannedBySafe = bannedBy;
            }

            this.reason = reason;
            this.start = start;
            this.end = end;
        }

        public String username;
        public String bannedBy;
        public String bannedBySafe;
        public String reason;
        public LocalDateTime start;
        public LocalDateTime end;
    }
}
