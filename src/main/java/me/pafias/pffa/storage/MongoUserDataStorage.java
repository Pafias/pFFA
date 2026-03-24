package me.pafias.pffa.storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import me.pafias.pffa.commands.subcommands.LeaderboardCommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MongoUserDataStorage implements UserDataStorage {

    private final MongoCollection<Document> collection;

    public MongoUserDataStorage(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public UserData fromDocument(@Nullable Document document) {
        if (document == null) return null;
        return new UserData(false,
                UUID.fromString(document.getString("_id")),
                new FfaData(
                        document.getInteger("kills"),
                        document.getInteger("deaths"),
                        document.getInteger("killstreak")
                )
        );
    }

    public Document toDocument(@UnknownNullability UserData userData) {
        if (userData == null) return null;
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
                        new ReplaceOptions().upsert(true));
    }

    @Override
    public List<UserData> getTopStatistic(LeaderboardCommand.Statistic statistic, int resultLimit) {
        if (statistic == null || resultLimit <= 0)
            return Collections.emptyList();

        final List<UserData> list = new ArrayList<>();
        collection
                .find()
                .sort(Sorts.orderBy(
                        Sorts.descending(statistic.getDbColumnName())
                ))
                .limit(resultLimit)
                .forEach(document -> {
                    final UserData userData = fromDocument(document);
                    if (userData != null)
                        list.add(userData);
                });
        return list;
    }

}
