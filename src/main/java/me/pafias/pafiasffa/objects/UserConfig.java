package me.pafias.pafiasffa.objects;

import me.pafias.pafiasffa.PafiasFFA;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserConfig {

    private final PafiasFFA plugin = PafiasFFA.get();

    private UUID uuid;
    private String name;

    private File file;
    private FileConfiguration config;

    public UserConfig(UUID uuid) {
        this.uuid = uuid;
        if (!plugin.getSM().getVariables().useMysql) {
            File dir = new File(plugin.getDataFolder() + "/playerdata/");
            if (!dir.exists())
                dir.mkdirs();
            file = new File(dir, uuid.toString() + ".yml");
            if (file.exists())
                config = YamlConfiguration.loadConfiguration(file);
        }
    }

    public UserConfig(String name) {
        this.name = name;
    }

    public CompletableFuture<List<Object>> get(String... key) {
        CompletableFuture<List<Object>> future = new CompletableFuture<>();
        if (!plugin.getSM().getVariables().useMysql) {
            List<Object> list = new ArrayList<>();
            for (String k : key)
                if (file.exists())
                    list.add(config.get(k));
                else
                    list.add(null);
            future.complete(list);
        } else {
            exists().thenAccept(exists -> {
                if (!exists)
                    createPlayer();
            });
            plugin.getSM().getDBManager().getConnection().thenAccept(connection -> {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < key.length; i++)
                        sb.append(key[i]).append(i == key.length - 1 ? "" : ", ");
                    PreparedStatement pst;
                    if (uuid != null) {
                        pst = connection.prepareStatement(String.format("SELECT %s FROM %s WHERE uuid = ?;", sb.toString(), plugin.getSM().getVariables().mysqlTable));
                        pst.setString(1, uuid.toString());
                    } else {
                        pst = connection.prepareStatement(String.format("SELECT %s FROM %s WHERE name = ?;", sb.toString(), plugin.getSM().getVariables().mysqlTable));
                        pst.setString(1, name);
                    }
                    ResultSet result = pst.executeQuery();
                    List<Object> values = new ArrayList<>();
                    while (result.next()) {
                        for (int i = 0; i < key.length; i++)
                            values.add(result.getObject(i + 1));
                    }
                    result.close();
                    future.complete(values);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return future;
    }

    public void update(String key, Object value) throws IOException {
        if (!plugin.getSM().getVariables().useMysql) {
            if (!file.exists()) {
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);
            }
            config.set(key, value);
            config.save(file);
        } else {
            exists().thenAccept(exists -> {
                if (!exists)
                    createPlayer();
            });
            plugin.getSM().getDBManager().getConnection().thenAccept(connection -> {
                try {
                    PreparedStatement pst;
                    if (uuid != null) {
                        pst = connection.prepareStatement(String.format("UPDATE %s SET %s = ? WHERE uuid = ?;", plugin.getSM().getVariables().mysqlTable, key));
                        pst.setObject(1, value);
                        pst.setString(2, uuid.toString());
                    } else {
                        pst = connection.prepareStatement(String.format("UPDATE %s SET %s = ? WHERE name = ?;", plugin.getSM().getVariables().mysqlTable, key));
                        pst.setObject(1, value);
                        pst.setString(2, name);
                    }
                    pst.executeUpdate();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public CompletableFuture<Boolean> exists() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (!plugin.getSM().getVariables().useMysql) {
            if (uuid != null)
                future.complete(plugin.getServer().getOfflinePlayer(uuid).hasPlayedBefore());
            else
                future.complete(plugin.getServer().getOfflinePlayer(name).hasPlayedBefore());
        } else {
            plugin.getSM().getDBManager().getConnection().thenAccept(connection -> {
                try {
                    PreparedStatement pst;
                    if (uuid != null) {
                        pst = connection.prepareStatement(String.format("SELECT 1 FROM %s WHERE uuid = ?;", plugin.getSM().getVariables().mysqlTable));
                        pst.setString(1, uuid.toString());
                    } else {
                        pst = connection.prepareStatement(String.format("SELECT 1 FROM %s WHERE name = ?;", plugin.getSM().getVariables().mysqlTable));
                        pst.setString(1, name);
                    }
                    ResultSet resultSet = pst.executeQuery();
                    if (resultSet.next())
                        future.complete(true);
                    else
                        future.complete(false);
                    resultSet.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return future;
    }

    public void createPlayer() {
        plugin.getSM().getDBManager().getConnection().thenAccept(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO " + plugin.getSM().getVariables().mysqlTable + " (uuid, name) " +
                        "SELECT * FROM (SELECT '" + uuid + "', '" + name + "') AS tmp " +
                        "WHERE NOT EXISTS (" +
                        "SELECT uuid FROM " + plugin.getSM().getVariables().mysqlTable + " WHERE uuid = '" + uuid + "'" +
                        ") LIMIT 1;");
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
