package me.pafias.pffa.objects;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.Tasks;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserConfig {

    private final pFFA plugin = pFFA.get();

    private final UUID uuid;

    private File file;
    private FileConfiguration config;

    public boolean isFileStorageType() {
        return plugin.getSM().getMongoManager() == null && plugin.getSM().getMysqlManager() == null;
    }

    public boolean isMongoStorageType() {
        return plugin.getSM().getMongoManager() != null;
    }

    public boolean isMysqlStorageType() {
        return plugin.getSM().getMysqlManager() != null;
    }

    public UserConfig(UUID uuid) {
        this.uuid = uuid;
        if (isFileStorageType()) {
            File dir = new File(plugin.getDataFolder() + "/playerdata/");
            if (!dir.exists())
                dir.mkdirs();
            file = new File(dir, uuid.toString() + ".yml");
            if (file.exists())
                config = YamlConfiguration.loadConfiguration(file);
        }
    }

    public CompletableFuture<Map<String, Object>> get(String... keys) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        if (isFileStorageType()) {
            Map<String, Object> map = new HashMap<>();
            for (String key : keys)
                if (file.exists())
                    map.put(key, config.get(key));
                else
                    map.put(key, null);
            future.complete(map);
        } else if (isMongoStorageType()) {
            Tasks.runAsync(() -> {
                MongoCollection<Document> collection = plugin.getSM().getMongoManager().getCollection();
                Document doc = collection.find(Filters.eq("_id", uuid.toString())).first();
                future.complete(doc != null ? doc : new HashMap<>());
            });
        } else if (isMysqlStorageType()) {
            exists().thenAccept(exists -> {
                if (!exists)
                    createMysqlPlayer();
            });
            plugin.getSM().getMysqlManager().getConnection().thenAccept(connection -> {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < keys.length; i++)
                        sb.append(keys[i]).append(i == keys.length - 1 ? "" : ", ");
                    PreparedStatement pst = connection.prepareStatement(String.format("SELECT %s FROM %s WHERE uuid = ?;", sb.toString(), plugin.getSM().getVariables().mysql.get("table", "ffa_players")));
                    pst.setString(1, uuid.toString());
                    ResultSet result = pst.executeQuery();
                    Map<String, Object> map = new HashMap<>();
                    while (result.next()) {
                        for (int i = 0; i < keys.length; i++)
                            map.put(keys[i], result.getObject(i + 1));
                    }
                    result.close();
                    future.complete(map);
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

    public void update(Map<String, Object> data) throws IOException {
        if (isFileStorageType()) {
            if (!file.exists()) {
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);
            }
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            config.save(file);
        } else if (isMongoStorageType()) {
            Tasks.runAsync(() -> {
                MongoCollection<Document> collection = plugin.getSM().getMongoManager().getCollection();
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                if (!exists().join())
                    future.complete(collection.insertOne(new Document("_id", uuid.toString())
                            .append("kills", 0)
                            .append("deaths", 0)
                            .append("killstreak", 0)).wasAcknowledged());
                else
                    future.complete(true);
                future.thenRunAsync(() -> {
                    Document updateDocument = new Document();
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        updateDocument.append(entry.getKey(), entry.getValue());
                    }
                    Document setDocument = new Document("$set", updateDocument);
                    collection.updateOne(Filters.eq("_id", uuid.toString()), setDocument, new UpdateOptions().upsert(true));
                });
            });
        } else if (isMysqlStorageType()) {
            exists().thenAccept(exists -> {
                if (!exists)
                    createMysqlPlayer();
            });
            plugin.getSM().getMysqlManager().getConnection().thenAccept(connection -> {
                try {
                    StringBuilder updateQuery = new StringBuilder(String.format("UPDATE %s SET ", plugin.getSM().getVariables().mysql.get("table", "ffa_players")));
                    boolean first = true;
                    for (String key : data.keySet()) {
                        if (!first) {
                            updateQuery.append(", ");
                        }
                        updateQuery.append(String.format("%s = ?", key));
                        first = false;
                    }
                    updateQuery.append(" WHERE uuid = ?;");

                    PreparedStatement pst = connection.prepareStatement(updateQuery.toString());
                    int i = 1;
                    for (Object value : data.values()) {
                        pst.setObject(i++, value);
                    }
                    pst.setString(i, uuid.toString());
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
        if (isFileStorageType()) {
            if (uuid != null)
                future.complete(plugin.getServer().getOfflinePlayer(uuid).hasPlayedBefore());
        } else if (isMongoStorageType()) {
            Tasks.runAsync(() -> {
                MongoCollection<Document> collection = plugin.getSM().getMongoManager().getCollection();
                future.complete(collection.find(Filters.eq("_id", uuid.toString())).first() != null);
            });
        } else if (isMysqlStorageType()) {
            plugin.getSM().getMysqlManager().getConnection().thenAccept(connection -> {
                try {
                    PreparedStatement pst = connection.prepareStatement(String.format("SELECT 1 FROM %s WHERE uuid = ?;", plugin.getSM().getVariables().mysql.get("table", "ffa_players")));
                    pst.setString(1, uuid.toString());
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

    public void createMysqlPlayer() {
        plugin.getSM().getMysqlManager().getConnection().thenAccept(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO " + plugin.getSM().getVariables().mysql.get("table", "ffa_players") + " (uuid) " +
                        "SELECT * FROM (SELECT '" + uuid + "') AS tmp " +
                        "WHERE NOT EXISTS (" +
                        "SELECT uuid FROM " + plugin.getSM().getVariables().mysql.get("table", "ffa_players") + " WHERE uuid = '" + uuid + "'" +
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
