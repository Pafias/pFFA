package me.pafias.pffa.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserData {

    private final UUID uniqueId;
    private final FfaData ffaData;

    public UserData copy() {
        return new UserData(uniqueId, ffaData.copy());
    }
}

