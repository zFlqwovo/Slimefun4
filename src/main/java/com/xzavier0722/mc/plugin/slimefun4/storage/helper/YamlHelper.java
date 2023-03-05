package com.xzavier0722.mc.plugin.slimefun4.storage.helper;

import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

public class YamlHelper {
    public static void migratePlayerData() {
        var playerFolder = new File("data-storage/Slimefun/Players/");

        if (!playerFolder.exists() || !playerFolder.isDirectory()) return;

        var backupFolder = new File("data-storage/Slimefun/Players_Backup");
        backupFolder.mkdirs();

        for (File file : playerFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                try {
                    var uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    var p = Bukkit.getOfflinePlayer(uuid);
                    if (p != null && Slimefun.getDatabaseManager().getProfileDataController().getProfile(p) == null) {
                        migratePlayerProfile(uuid);
                    }

                    var backupFile = new File(backupFolder, file.getName());
                    backupFile.createNewFile();
                    Files.copy(file.toPath(), backupFile.toPath());

                    file.delete();
                } catch (IOException | IllegalArgumentException e) {
                    Slimefun.logger().log(Level.WARNING, "迁移玩家数据时出现问题", e);
                }
            }
        }

        Slimefun.logger().log(Level.INFO, "迁移玩家数据完成! 迁移前的数据已储存在 " + backupFolder.getAbsolutePath());
    }

    private static void migratePlayerProfile(@Nonnull UUID uuid) {
        var p = Bukkit.getOfflinePlayer(uuid);
        var configFile = new Config("data-storage/Slimefun/Players/" + uuid + ".yml");

        if (!configFile.getFile().exists()) {
            return;
        }

        var controller = Slimefun.getDatabaseManager().getProfileDataController();
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

            var bp = controller.createBackpack(p, Integer.parseInt(backpackID), size);

            var changedSlot = new HashSet<Integer>();

            for (String key : configFile.getKeys("backpacks." + backpackID + ".contents")) {
                var item = configFile.getItem("backpacks." + backpackID + ".contents." + key);
                var bpKey = Integer.parseInt(key);
                bp.getInventory().setItem(bpKey, item);
                changedSlot.add(bpKey);
            }

            controller.saveBackpackInventory(bp, changedSlot);
        }
    }
}
