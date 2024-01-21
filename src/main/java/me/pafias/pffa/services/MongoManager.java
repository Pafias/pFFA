package me.pafias.pffa.services;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.pafias.pffa.pFFA;
import org.bson.Document;

import java.util.Collections;

public class MongoManager {

    private final MongoClient client;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoManager(pFFA plugin, Variables variables) {
        String host = variables.mongo.getString("host", "localhost");
        int port = variables.mongo.getInt("port", 27017);
        MongoClientSettings.Builder settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))));
        String username = variables.mongo.getString("username", "root");
        String database = variables.mongo.getString("database", "minecraft");
        String password = variables.mongo.getString("password", "");
        settings.credential(MongoCredential.createCredential(username, database, password.toCharArray()));
        String options = variables.mongo.getString("options", "");
        client = MongoClients.create(new ConnectionString(String.format("mongodb://%s:%s@%s:%d/%s%s", username, password, host, port, database, options)));
        this.database = client.getDatabase(database);
        this.collection = this.database.getCollection(variables.mongo.getString("collection", "ffa"));
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void shutdown() {
        if (client != null)
            client.close();
    }

}
