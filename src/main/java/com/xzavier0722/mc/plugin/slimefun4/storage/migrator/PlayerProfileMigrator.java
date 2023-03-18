package com.xzavier0722.mc.plugin.slimefun4.storage.migrator;

import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

public class PlayerProfileMigrator {
    private static final File playerFolder = new File("data-storage/Slimefun/Players/");

    public static void checkOldData(Logger logger) {
        if (playerFolder.exists() && playerFolder.isDirectory()) {
            logger.log(Level.WARNING, "检测到使用文件储存的旧玩家数据, 请使用 /sf migrate 迁移旧数据至数据库!");
        }
    }

    /**
     * To check the existence of old player data stored as yml
     * and try to migrate them to database
     */
    public static MigrateStatus migrateOldData() {
        var result = MigrateStatus.SUCCESS;

        if (!playerFolder.exists() || !playerFolder.isDirectory()) {
            return MigrateStatus.MIGRATED;
        }

        var backupFolder = new File("data-storage/Slimefun/Players_Backup");
        backupFolder.mkdirs();

        for (File file : playerFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                try {
                    var uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    var p = Bukkit.getOfflinePlayer(uuid);

                    if (!p.hasPlayedBefore() || p == null) {
                        Slimefun.logger().log(Level.FINEST, "检测到从未加入服务器玩家的数据, 已自动跳过");
                        continue;
                    }

                    if (Slimefun.getDatabaseManager().getProfileDataController().getProfile(p) == null) {
                        migratePlayerProfile(uuid);
                    }

                    var backupFile = new File(backupFolder, file.getName());
                    backupFile.createNewFile();
                    Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    file.delete();
                } catch (IOException | IllegalArgumentException e) {
                    Slimefun.logger().log(Level.WARNING, "迁移玩家数据时出现问题", e);
                    result = MigrateStatus.FAILED;
                }
            }
        }

        Slimefun.logger().log(Level.INFO, "迁移玩家数据完成! 迁移前的数据已储存在 " + backupFolder.getAbsolutePath());
        playerFolder.delete();

        return result;
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
        var max = 0;
        for (String backpackID : configFile.getKeys("backpacks")) {
            var bpID = Integer.parseInt(backpackID);
            if (max < bpID) {
                max = bpID;
            }
            var size = configFile.getInt("backpacks." + bpID + ".size");

            var bp = controller.createBackpack(p, "",bpID, size);

            var changedSlot = new HashSet<Integer>();

            for (String key : configFile.getKeys("backpacks." + bpID + ".contents")) {
                var bpKey = Integer.parseInt(key);
                var item = configFile.getItem("backpacks." + bpID + ".contents." + bpKey);
                bp.getInventory().setItem(bpKey, item);
                changedSlot.add(bpKey);
            }

            controller.saveBackpackInventory(bp, changedSlot);
        }
        profile.setBackpackCount(max);
    }
}
