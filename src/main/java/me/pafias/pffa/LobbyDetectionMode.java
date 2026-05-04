package me.pafias.pffa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public enum LobbyDetectionMode {

    Y_COORD("ycoord"),
    BOUNDS("bounds"),
    OTHER(null);

    private final String configValue;

    public static LobbyDetectionMode parse(@Nullable String input) {
        if (input == null || input.isBlank() || input.equalsIgnoreCase("none")) return OTHER;
        for (LobbyDetectionMode mode : LobbyDetectionMode.values()) {
            if (mode.getConfigValue() != null && mode.getConfigValue().equalsIgnoreCase(input)) return mode;
        }
        return OTHER;
    }

}
