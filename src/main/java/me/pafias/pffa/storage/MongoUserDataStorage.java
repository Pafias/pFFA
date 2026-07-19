package me.pafias.pffa.storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import me.pafias.pffa.commands.subcommands.LeaderboardCommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MongoUserDataStorage implements UserDataStorage {

    private final MongoCollection<Document> collection;

    public MongoUserDataStorage(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    private static final ReplaceOptions UPSERT_OPTIONS = new ReplaceOptions().upsert(true);

    public UserData fromDocument(@Nullable Document document) {
        if (document == null) return null;
        return new UserData(
                UUID.fromString(document.getString("_id")),
                new FfaData(
                        document.getInteger("kills"),
                        document.getInteger("deaths"),
                        document.getInteger("killstreak")
                )
        );
    }

    public Document toDocument(UserData userData) {
        Objects.requireNonNull(userData);
        return new Document("_id", userData.getUniqueId().toString())
                .append("kills", userData.getFfaData().getKills())
                .append("deaths", userData.getFfaData().getDeaths())
                .append("killstreak", userData.getFfaData().getKillstreak());
    }

    @Override
    public UserData getUserData(String uuid) {
        return fromDocument(collection
                .find(Filters.eq("_id", uuid))
                .first());
    }

    @Override
    public void setUserData(UserData userData) {
        collection
                .replaceOne(
                        Filters.eq("_id", userData.getUniqueId().toString()),
                        toDocument(userData),
                        UPSERT_OPTIONS);
    }

    @Override
    public void setUserDataBatch(Collection<UserData> userData) {
        if (userData == null || userData.isEmpty())
            return;

        final List<WriteModel<Document>> writes = new ArrayList<>(userData.size());

        for (UserData data : userData) {
            if (data == null)
                continue;

            writes.add(new ReplaceOneModel<>(
                    Filters.eq("_id", data.getUniqueId().toString()),
                    toDocument(data),
                    UPSERT_OPTIONS
            ));
        }

        if (!writes.isEmpty()) {
            collection.bulkWrite(
                    writes,
                    new BulkWriteOptions().ordered(false)
            );
        }
    }

    @Override
    public List<UserData> getTopStatistic(LeaderboardCommand.Statistic statistic, int resultLimit) {
        if (statistic == null || resultLimit <= 0)
            return Collections.emptyList();

        final List<UserData> list = new ArrayList<>();
        collection.find()
                .projection(Projections.include(
                        "_id",
                        "kills",
                        "deaths",
                        "killstreak"
                ))
                .sort(Sorts.descending(statistic.getDbColumnName()))
                .limit(resultLimit)
                .map(this::fromDocument)
                .into(list);
        return list;
    }

}
