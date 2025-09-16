package me.pafias.pffa.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Getter
public class MysqlManager {

    private final HikariDataSource dataSource;

    public MysqlManager(
            String host,
            int port,
            String username,
            String password,
            String database,
            boolean ssl
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=" + ssl);
        config.setUsername(username);
        config.setPassword(password);
        dataSource = new HikariDataSource(config);
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
