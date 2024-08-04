package me.pafias.pffa.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.pafias.pffa.pFFA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class MysqlManager {

    private final pFFA plugin;

    private final HikariDataSource dataSource;

    public MysqlManager(pFFA plugin, Variables variables) {
        this.plugin = plugin;
        String host = variables.mysql.getString("host", "127.0.0.1");
        int port = variables.mysql.getInt("port", 3306);
        String database = variables.mysql.getString("database", "minecraft");
        String username = variables.mysql.getString("username", "root");
        String password = variables.mysql.getString("password", "");
        boolean ssl = variables.mysql.getBoolean("ssl", false);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=" + ssl);
        config.setUsername(username);
        config.setPassword(password);
        dataSource = new HikariDataSource(config);
        if (variables.mysql.getBoolean("setup_on_start"))
            setup(variables);
    }

    private void setup(Variables variables) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + variables.mysql.getString("table", "ffa_players") + " (" +
                            "uuid varchar(36) NOT NULL," +
                            "kills INT DEFAULT 0 NOT NULL," +
                            "deaths INT DEFAULT 0 NOT NULL," +
                            "killstreak INT DEFAULT 0 NOT NULL," +
                            "settings LONGTEXT DEFAULT '[]'," +
                            "PRIMARY KEY (uuid)" +
                            ");"
            );
            ps.execute();
            plugin.getConfig().set("mysql.setup_on_start", false);
            plugin.saveConfig();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Connection> getConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public void closePool() {
        if (dataSource != null)
            dataSource.close();
    }

}
