package me.pafias.pffa.services.persistency;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import me.pafias.pffa.objects.OfflineUser;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserDAOMongo implements UserDAO {

    private final MongoCollection<Document> collection;

    public UserDAOMongo(MongoCollection<Document> collection) {
        this.collection = collection;
    }


    @Override
    public void save(OfflineUser user) {
        collection.insertOne(new Document
                ("_id", user.getUuid().toString())
                .append("kills", user.getKills())
                .append("deaths", user.getDeaths())
                .append("killstreak", user.getKillstreak()));
    }

    @Override
    public void update(OfflineUser user) {
        collection.updateOne(
                Filters.eq("_id", user.getUuid().toString()),
                new Document("$set",
                        new Document("kills", user.getKills())
                                .append("deaths", user.getDeaths())
                                .append("killstreak", user.getKillstreak()))
        );
    }

    @Override
    public OfflineUser findByUuid(UUID uuid) {
        Document document = collection.find(Filters.eq("_id", uuid.toString())).first();
        return new OfflineUser(
                UUID.fromString(document.getString("_id")),
                document.getInteger("kills"),
                document.getInteger("deaths"),
                document.getInteger("killstreak")
        );
    }

    @Override
    public Set<OfflineUser> findAll() {
        Set<OfflineUser> set = new HashSet<>();
        collection.find().forEach(document -> {
            OfflineUser user = new OfflineUser(
                    UUID.fromString(document.getString("_id")),
                    document.getInteger("kills"),
                    document.getInteger("deaths"),
                    document.getInteger("killstreak")
            );
            set.add(user);
        });
        return set;
    }

}
