package me.pafias.pffa.objects;

import com.google.gson.JsonObject;

public class UserSettings {

    private boolean oldDamageTilt = true;

    public UserSettings() {
    }

    public UserSettings(JsonObject json) {
        this.oldDamageTilt = json.get("oldDamageTilt").getAsBoolean();
    }

    public boolean isOldDamageTilt() {
        return oldDamageTilt;
    }

    public void setOldDamageTilt(boolean oldDamageTilt) {
        this.oldDamageTilt = oldDamageTilt;
    }

    @Override
    public String toString() {
        JsonObject json = new JsonObject();
        json.addProperty("oldDamageTilt", oldDamageTilt);
        return json.toString();
    }

}
