package dev.tulis.proxieSuite.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.tulis.proxieSuite.Main.Main;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private static HikariDataSource dataSource;
    private Main plugin;

    public Database(Main m) {
        plugin = m;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(
            "jdbc:mysql://" +
                plugin.getConfig().getString("database.host") +
                "/" +
                plugin.getConfig().getString("database.database")
        );

        hikariConfig.setUsername(
            plugin.getConfig().getString("database.username")
        );
        hikariConfig.setPassword(
            plugin.getConfig().getString("database.password")
        );

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setMaxLifetime(300000);
        hikariConfig.setValidationTimeout(5000);
        hikariConfig.setKeepaliveTime(30000);
        dataSource = new HikariDataSource(hikariConfig);
    }

    public static void closeConnection() {
        dataSource.close();
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static String generateRandomString(int length) {
        String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
