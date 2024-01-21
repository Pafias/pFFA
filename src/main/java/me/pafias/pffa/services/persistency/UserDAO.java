package me.pafias.pffa.services.persistency;

import me.pafias.pffa.objects.OfflineUser;

import java.util.Set;
import java.util.UUID;

public interface UserDAO {

    void save(OfflineUser user);

    void update(OfflineUser user);

    OfflineUser findByUuid(UUID uuid);

    Set<OfflineUser> findAll();

}
