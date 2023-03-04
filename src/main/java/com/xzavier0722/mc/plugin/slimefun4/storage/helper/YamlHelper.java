package com.xzavier0722.mc.plugin.slimefun4.storage.helper;

import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

public class YamlHelper {
    public static void migratePlayerProfile(@Nonnull UUID uuid) {
        var p = Bukkit.getOfflinePlayer(uuid);
        var configFile = new Config("data-storage/Slimefun/Players/" + uuid + ".yml");

        if (!configFile.getFile().exists()) {
            return;
        }

        var controller = Slimefun.getRegistry().getProfileDataController();
        var profile = controller.createProfile(p);

        // Research migrate
        for (String researchID : configFile.getKeys("researches")) {
            var research = Research.getResearchByID(Integer.parseInt(researchID));

            if (research.isEmpty()) {
                return;
            }

            profile.setResearched(research.get(), true);
        }

        // Backpack migrate
        for (String backpackID : configFile.getKeys("backpacks")) {
            var size = configFile.getInt("backpacks." + backpackID + ".size");

            var bp = controller.createBackpack(
                    p,
                    profile.nextBackpackNum(),
                    size
            );

            for (String key : configFile.getKeys("backpacks." + backpackID + ".contents")) {
                var item = configFile.getItem("backpacks." + backpackID + ".contents." + key);
                bp.getInventory().setItem(Integer.parseInt(key), item);
            }
        }
    }
}
