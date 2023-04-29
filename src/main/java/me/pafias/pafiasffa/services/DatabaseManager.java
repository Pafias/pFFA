package me.pafias.pafiasffa.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final PafiasFFA plugin;

    private HikariDataSource dataSource;

    public DatabaseManager(PafiasFFA plugin, Variables variables) {
        this.plugin = plugin;
        if (variables.useMysql) {
            String host = variables.mysqlHost;
            int port = variables.mysqlPort;
            String database = variables.mysqlDatabase;
            String username = variables.mysqlUsername;
            String password = variables.mysqlPassword;
            boolean ssl = variables.mysqlSSL;
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=" + ssl);
            config.setUsername(username);
            config.setPassword(password);
            dataSource = new HikariDataSource(config);
            if (variables.setupDbOnStart)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        setup();
                    }
                }.runTaskAsynchronously(plugin);
        }
    }

    private void setup() {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + plugin.getSM().getVariables().mysqlTable + " (" +
                            "uuid varchar(36) NOT NULL," +
                            "kills INT DEFAULT 0 NOT NULL," +
                            "deaths INT DEFAULT 0 NOT NULL," +
                            "killstreak INT DEFAULT 0 NOT NULL," +
                            "PRIMARY KEY (uuid)" +
                            ");"
            );
            ps.execute();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getConfig().set("mysql.setup_on_start", false);
                plugin.saveConfig();
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Connection> getConnection() {
        CompletableFuture<Connection> future = new CompletableFuture<>();
        try {
            Connection connection = dataSource.getConnection();
            future.complete(connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
            future.completeExceptionally(ex);
        }
        return future;
    }

    public void closePool() {
        dataSource.close();
    }

}
