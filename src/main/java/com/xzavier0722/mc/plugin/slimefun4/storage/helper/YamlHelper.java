package com.xzavier0722.mc.plugin.slimefun4.storage.helper;

import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

public class YamlHelper {
    public static void migratePlayerData() {
        var playerFolder = new File("data-storage/Slimefun/Players/");

        if (!playerFolder.isDirectory()) return;

        for (File file : playerFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                try {
                    var uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    var p = Bukkit.getOfflinePlayer(uuid);
                    if (p != null && Slimefun.getRegistry().getProfileDataController().getProfile(p) == null) {
                        migratePlayerProfile(uuid);
                    }

                    var backupFile = new File(file.getAbsolutePath() + ".bak");
                    backupFile.createNewFile();
                    Files.copy(file.toPath(), backupFile.toPath());

                    file.delete();
                } catch (IllegalArgumentException ignored) {
                    // Illegal file name
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void migratePlayerProfile(@Nonnull UUID uuid) {
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
