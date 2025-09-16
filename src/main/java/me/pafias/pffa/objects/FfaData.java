package me.pafias.pffa.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FfaData {

    private int kills;
    private int deaths;
    private int killstreak;

    public double getKDR() {
        return kills / (double) (deaths == 0 ? 1 : deaths);
    }

}
