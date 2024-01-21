package me.pafias.pffa.objects;

import me.pafias.pffa.util.CC;
import me.pafias.pffa.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SettingsMenu extends GuiMenu {

    private final UserSettings settings;

    public SettingsMenu(Player player) {
        super(player, CC.t("&eSettings"), 9);
        protocolLib = plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib");
        settings = plugin.getSM().getUserManager().getUser(player).getSettings();
        render();
    }

    private final boolean protocolLib;

    public void render() {
        getInventory().clear();
        player.setItemOnCursor(null);
        getInventory().setItem(0, new ItemBuilder(protocolLib ? Material.STICK : Material.BARRIER)
                .setName(CC.t(protocolLib ? "&cDamage &oTilt" : "&m&7Damage &oTilt&r &7Not available"))
                .setLore("",
                        CC.t((protocolLib ? "" : "&m") + "&7The player's screen shakes when taking damage."),
                        CC.t((protocolLib ? "" : "&m") + "&7The original desired behaviour was to tilt the screen"),
                        CC.t((protocolLib ? "" : "&m") + "  &7in the direction of the damage,"),
                        CC.t((protocolLib ? "" : "&m") + "  &7but due to a bug, the tilting ended up happening"),
                        CC.t((protocolLib ? "" : "&m") + "  &7only to one side no matter the damage direction."),
                        "",
                        CC.t((protocolLib ? "" : "&m") + "&7With 1.19.4 Mojang fixed this years old bug, but many players"),
                        CC.t((protocolLib ? "" : "&m") + "  &7were already used to the old (broken) damage tilt, and dislike the new (fixed) one."),
                        CC.t((protocolLib ? "" : "&m") + "&7Which is why we give you this setting, &bto let you choose"),
                        CC.t((protocolLib ? "" : "&m") + "  &bwhether you want the old damage tilt, or the new one"),
                        "",
                        CC.tf((protocolLib ? "" : "&m") + "&eCurrent damage tilt: %s", settings.isOldDamageTilt() ? "&cOld" : "&aNew"),
                        "")
                .build());
    }

    @Override
    public void clickHandler(ItemStack item, int slot) {
        setCloseOnClick(false);
        if (item == null) return;
        switch (item.getType()) {
            case STICK:
                if (!protocolLib) {
                    player.playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
                    return;
                }
                settings.setOldDamageTilt(!settings.isOldDamageTilt());
                break;
            default:
                return;
        }
        render();
    }

}
