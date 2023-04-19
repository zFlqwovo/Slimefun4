package com.xzavier0722.mc.plugin.slimefun4.storage.migrator;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.FileUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.NumUtils;
import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;
import java.io.File;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BlockStorageMigrator {
    private static final File invFolder = new File("data-storage/Slimefun/stored-inventories/");
    private static final File chunkFolder = new File("data-storage/Slimefun/stored-chunks/");
    private static final File blockFolder = new File("data-storage/Slimefun/stored-blocks/");
    private static volatile boolean migrateLock = false;
    // worldName;x;y;z.sfi
    private static final Pattern invFilePattern = Pattern.compile("^.+;\\d+;\\d+;\\d+.sfi$");

    public static boolean isOldDataExists() {
        return FileUtils.checkDirectoryExists(invFolder)
                && FileUtils.checkDirectoryExists(blockFolder)
                && FileUtils.checkDirectoryExists(chunkFolder);
    }

    public static void checkOldData() {
        if (isOldDataExists()) {
            Slimefun.logger().log(Level.WARNING, "检测到使用文件储存的旧机器数据, 请使用 /sf migrate 迁移旧数据至数据库!");
        }
    }

    public static MigrateStatus migrateData() {
        if (migrateLock) {
            return MigrateStatus.MIGRATING;
        }

        var status = MigrateStatus.SUCCESS;
        migrateLock = true;

        var bsController = Slimefun.getDatabaseManager().getBlockDataController();

        // Inventory migration
        for (File invData : invFolder.listFiles()) {
            try {
                if (!invFilePattern.matcher(invData.getName()).matches()) {
                    continue;
                }

                var name = invData.getName().split(";");
                var worldName = name[0];
                var x = NumUtils.parseDouble(name[1]);
                var y = NumUtils.parseDouble(name[2]);
                var z = NumUtils.parseDouble(name[3]);

                if (x == -1 || y == -1 || z == -1) {
                    continue;
                }

                var cfg = new Config(invData);
                var machineId = cfg.getString("preset");

                var location = new Location(Bukkit.getWorld(worldName), x, y, z);
                var blockData = bsController.createBlock(location, machineId);

                for (String key : cfg.getKeys()) {
                    if (key.equals("preset")) continue;
                    var slot = NumberUtils.getInt(key, -1);
                    if (slot == -1) {
                        continue;
                    }

                    var item = cfg.getItem(key);
                    // TODO: Set machine inventory item to database
                }

                bsController.setBlockDataLocation(blockData, location);
            } catch (Exception e) {
                Slimefun.logger().log(Level.WARNING, "在迁移方块物品栏数据时出现问题", e);
            }
        }

        migrateLock = false;
        return status;
    }
}
