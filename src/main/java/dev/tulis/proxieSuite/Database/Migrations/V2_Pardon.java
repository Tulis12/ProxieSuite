package dev.tulis.proxieSuite.Database.Migrations;

import dev.tulis.proxieSuite.Database.Migration;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class V2_Pardon implements Migration {

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String description() {
        return "Update table add pardon column";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                """
                ALTER TABLE `proxie_bans` ADD COLUMN `pardon` BOOLEAN NOT NULL DEFAULT FALSE;
                """
            );
        }
    }
}
