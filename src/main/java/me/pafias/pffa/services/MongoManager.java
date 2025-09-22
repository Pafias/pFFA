package me.pafias.pffa.services;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
public class MongoManager {

    private final MongoClient client;
    private final MongoDatabase database;

    public MongoManager(
            String host,
            int port,
            String username,
            String password,
            String database,
            String options
    ) throws UnsupportedEncodingException {
        client = MongoClients.create(
                new ConnectionString(
                        String.format("mongodb://%s:%s@%s:%d/%s%s",
                                URLEncoder.encode(username, StandardCharsets.UTF_8.name()),
                                URLEncoder.encode(password, StandardCharsets.UTF_8.name()),
                                host,
                                port,
                                database,
                                options)
                )
        );
        this.database = client.getDatabase(database);
    }

    public void shutdown() {
        if (client != null)
            client.close();
    }

}
