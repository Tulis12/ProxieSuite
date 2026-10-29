package dev.tulis.proxieSuite.Database.Migrations;

import dev.tulis.proxieSuite.Database.Migration;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class V1_CreatePlayersBans implements Migration {

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String description() {
        return "Create users table";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO'");
            statement.execute("SET time_zone = '+00:00'");

            statement.execute(
                """
                CREATE TABLE `proxie_bans` (
                  `id` int NOT NULL,
                  `player_id` int NOT NULL,
                  `banned_by_id` int DEFAULT NULL,
                  `reason` varchar(255) NOT NULL,
                  `start` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `end` timestamp NULL DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                """
            );

            statement.execute(
                """
                CREATE TABLE `proxie_players` (
                  `id` int NOT NULL,
                  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                  `uuid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                  `onlineauth` tinyint(1) NOT NULL DEFAULT '0',
                  `online_account_exists` tinyint(1) DEFAULT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                """
            );

            statement.execute(
                """
                ALTER TABLE `proxie_bans`
                  ADD PRIMARY KEY (`id`),
                  ADD KEY `player_id` (`player_id`),
                  ADD KEY `banned_by_id` (`banned_by_id`)
                """
            );

            statement.execute(
                """
                ALTER TABLE `proxie_players`
                  ADD PRIMARY KEY (`id`),
                  ADD KEY `nickname` (`username`)
                """
            );

            statement.execute(
                """
                ALTER TABLE `proxie_bans`
                  MODIFY `id` int NOT NULL AUTO_INCREMENT
                """
            );

            statement.execute(
                """
                ALTER TABLE `proxie_players`
                  MODIFY `id` int NOT NULL AUTO_INCREMENT
                """
            );

            statement.execute(
                """
                ALTER TABLE `proxie_bans`
                  ADD CONSTRAINT `proxie_bans_ibfk_1`
                    FOREIGN KEY (`player_id`) REFERENCES `proxie_players` (`id`)
                    ON DELETE CASCADE ON UPDATE CASCADE,
                  ADD CONSTRAINT `proxie_bans_ibfk_2`
                    FOREIGN KEY (`banned_by_id`) REFERENCES `proxie_players` (`id`)
                    ON DELETE SET NULL ON UPDATE SET NULL
                """
            );
        }
    }
}
