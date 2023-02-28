package com.xzavier0722.mc.plugin.slimefun4.database.controller;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import org.apache.commons.lang.NotImplementedException;

public class PlayerProfileDataController {
    private static volatile PlayerProfileDataController instance;

    public static PlayerProfileDataController getInstance() {
        if (instance == null) {
            synchronized (PlayerProfileDataController.class) {
                if (instance == null) {
                    instance = new PlayerProfileDataController();
                }
            }
        }

        return instance;
    }

    private PlayerProfileDataController() {

    }

    public PlayerProfile getProfile() {
        // TODO
        throw new NotImplementedException();
    }

    public PlayerBackpack getBackpack(PlayerProfile profile, int id) {
        // TODO
        throw new NotImplementedException();
    }
}
