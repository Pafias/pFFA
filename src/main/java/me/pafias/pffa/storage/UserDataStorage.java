package me.pafias.pffa.storage;

import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.objects.exceptions.UserLoadingException;

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

}
