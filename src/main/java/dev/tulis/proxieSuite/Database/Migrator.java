package dev.tulis.proxieSuite.Database;

import dev.tulis.proxieSuite.Main.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Migrator {

    private static final String TABLE = "proxie_migrations";

    private Migrator() {}

    public static void migrate(
        Connection connection,
        List<Migration> migrations,
        Main plugin
    ) throws SQLException {
        if (!connection.getAutoCommit()) {
            throw new SQLException(
                "Migration requires a connection with auto-commit enabled"
            );
        }

        createHistoryTable(connection);

        List<Migration> sorted = new ArrayList<>(migrations);
        sorted.sort(Comparator.comparingInt(Migration::version));

        validate(sorted);

        Set<Integer> applied = getAppliedVersions(connection);

        for (Migration migration : sorted) {
            if (applied.contains(migration.version())) {
                continue;
            }

            plugin
                .getLogger()
                .info("Running migration: " + migration.version());
            runMigration(connection, migration);
        }
    }

    private static void createHistoryTable(Connection connection)
        throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS %s (
                    version INTEGER PRIMARY KEY,
                    description VARCHAR(255) NOT NULL,
                    installed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """.formatted(TABLE)
            );
        }
    }

    private static Set<Integer> getAppliedVersions(Connection connection)
        throws SQLException {
        Set<Integer> versions = new HashSet<>();

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(
                "SELECT version FROM " + TABLE
            )
        ) {
            while (result.next()) {
                versions.add(result.getInt("version"));
            }
        }

        return versions;
    }

    private static void validate(List<Migration> migrations)
        throws SQLException {
        Set<Integer> versions = new HashSet<>();

        for (Migration migration : migrations) {
            if (migration.version() < 1) {
                throw new SQLException(
                    "Migration version must be positive: " + migration.version()
                );
            }

            if (!versions.add(migration.version())) {
                throw new SQLException(
                    "Duplicate migration version: " + migration.version()
                );
            }
        }
    }

    private static void runMigration(Connection connection, Migration migration)
        throws SQLException {
        connection.setAutoCommit(false);

        try {
            migration.migrate(connection);

            try (
                PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO %s (version, description)
                    VALUES (?, ?)
                    """.formatted(TABLE)
                )
            ) {
                statement.setInt(1, migration.version());
                statement.setString(2, migration.description());
                statement.executeUpdate();
            }

            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                exception.addSuppressed(rollbackException);
            }

            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
