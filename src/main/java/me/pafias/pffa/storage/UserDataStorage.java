package me.pafias.pffa.storage;

import me.pafias.pffa.commands.subcommands.LeaderboardCommand;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.objects.exceptions.UserLoadingException;

import java.util.List;

public interface UserDataStorage {

    /**
     * Retrieves the user data for a player by their UUID.
     *
     * @param uuid The player's uuid
     * @return UserData for the player, or null if not found
     * @throws UserLoadingException
     */
    UserData getUserData(String uuid);

    /**
     * Saves the user data to the storage.
     *
     * @param userData The user data to save
     */
    void setUserData(UserData userData);

    /**
     * Returns an ordered list of users based on the specified statistic, limited to the specified number of results.
     * Use(d/ful) for like a leaderboard
     *
     * @param statistic   The statistic to be ordered by
     * @param resultLimit The amount of results to be returned (ex.: top 3, top 10)
     */
    List<UserData> getTopStatistic(LeaderboardCommand.Statistic statistic, int resultLimit);

}
