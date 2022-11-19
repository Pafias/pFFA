package me.pafias.pafiasffa.services;

import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final PafiasFFA plugin;

    public DatabaseManager(PafiasFFA plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getSM().getVariables().useMysql)
                    setup();
            }
        }.runTaskLaterAsynchronously(plugin, (2 * 20));
    }

    private Connection connection;

    private void setup() {
        getConnection().thenAccept(conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS " + plugin.getSM().getVariables().mysqlTable + " (" +
                                "uuid varchar(36) NOT NULL," +
                                "name varchar(16) NOT NULL," +
                                "kills INT DEFAULT 0 NOT NULL," +
                                "deaths INT DEFAULT 0 NOT NULL," +
                                "PRIMARY KEY (uuid)" +
                                ")"
                );
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Connection> getConnection() {
        try {
            CompletableFuture<Connection> future = new CompletableFuture<>();
            if (connection == null || connection.isClosed()) {
                String host = plugin.getSM().getVariables().mysqlHost;
                int port = plugin.getSM().getVariables().mysqlPort;
                String database = plugin.getSM().getVariables().mysqlDatabase;
                String username = plugin.getSM().getVariables().mysqlUsername;
                String password = plugin.getSM().getVariables().mysqlPassword;
                boolean ssl = plugin.getSM().getVariables().mysqlSSL;
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=" + ssl, username, password);
            }
            future.complete(connection);
            return future;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
