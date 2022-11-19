package me.pafias.pafiasffa.util;

import me.pafias.pafiasffa.PafiasFFA;

import java.text.DecimalFormat;

public class RandomUtils {

    private static final PafiasFFA plugin = PafiasFFA.get();

    public static float getKDR(int kills, int deaths) {
        float k = (float) kills;
        float d = (float) deaths;
        float result = 0;
        if (d == 0) {
            if (k == 0) result = 0;
            else if (k >= 1) result = 1;
        } else result = k / d;
        result = Float.parseFloat(new DecimalFormat("#.##").format(result));
        return result;
    }

    public static int parseSizeToInvSize(int size) {
        if (size <= 9) return 9;
        else if (size > 9 && size <= 18) return 18;
        else if (size > 18 && size <= 27) return 27;
        else if (size > 27 && size <= 36) return 36;
        else if (size > 36 && size <= 45) return 45;
        else return 54;
    }

}
