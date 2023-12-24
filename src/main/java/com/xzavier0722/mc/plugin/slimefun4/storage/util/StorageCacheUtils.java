package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import com.google.gson.JsonObject;
import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Utils to access the cached block data.
 * It is safe to use when the target block is in a loaded chunk (such as in block events).
 * By default, please use
 * {@link com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController#getBlockData}
 */
public class StorageCacheUtils {
    private static final Set<SlimefunBlockData> loadingData = new HashSet<>();

    @ParametersAreNonnullByDefault
    public static boolean hasBlock(Location l) {
        return getBlock(l) != null;
    }

    @ParametersAreNonnullByDefault
    public static boolean isBlock(Location l, String id) {
        var blockData = getBlock(l);
        return blockData != null && id.equals(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    public static boolean isBlock(Block block, String id) {
        return isBlock(block.getLocation(), id);
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunBlockData getBlock(Location l) {
        return Slimefun.getDatabaseManager().getBlockDataController().getBlockDataFromCache(l);
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunBlockData getBlock(Block block) {
        return getBlock(block.getLocation());
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunItem getSfItem(Location l) {
        var blockData = getBlock(l);
        return blockData == null ? null : SlimefunItem.getById(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunItem getSfItem(Block block) {
        var blockData = getBlock(block.getLocation());
        return blockData == null ? null : SlimefunItem.getById(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable public static String getData(Location loc, String key) {
        var blockData = getBlock(loc);
        return blockData == null ? null : blockData.getData(key);
    }

    @ParametersAreNonnullByDefault
    @Nullable public static String getData(Block block, String key) {
        var blockData = getBlock(block.getLocation());
        return blockData == null ? null : blockData.getData(key);
    }

    @ParametersAreNonnullByDefault
    public static void setData(Location loc, String key, String val) {
        var block = getBlock(loc);

        if (block == null) {
            Slimefun.logger()
                    .log(
                            Level.WARNING,
                      "The specifiy location {0} doesn't have block data!",
                            LocationUtils.locationToString(loc));
        } else {
            block.setData(key, val);
        }
    }

    @ParametersAreNonnullByDefault
    public static void setData(Block block, String key, String val) {
        setData(block.getLocation(), key, val);
    }

    @Nullable public static String getData(@Nonnull Block block, @Nonnull String key, String def) {
        return getData(block.getLocation(), key, def);
    }

    @Nullable public static String getData(@Nonnull Location loc, @Nonnull String key, String def) {
        var blockData = getData(loc, key);
        return blockData == null ? def : blockData;
    }

    @ParametersAreNonnullByDefault
    public static void removeData(Location loc, String key) {
        if (getBlock(loc) != null) {
            getBlock(loc).removeData(key);
        }
    }

    @ParametersAreNonnullByDefault
    public static void removeData(Block block, String key) {
        removeData(block.getLocation(), key);
    }

    @ParametersAreNonnullByDefault
    @Nullable public static BlockMenu getMenu(Location loc) {
        var blockData = getBlock(loc);
        if (blockData == null) {
            return null;
        }

        if (!blockData.isDataLoaded()) {
            requestLoad(blockData);
            return null;
        }

        return blockData.getBlockMenu();
    }

    @ParametersAreNonnullByDefault
    @Nullable public static BlockMenu getMenu(Block block) {
        return getMenu(block.getLocation());
    }

    @ParametersAreNonnullByDefault
    public static JsonObject getDataAsJSON(Block block) {
        return getDataAsJSON(block.getLocation());
    }

    @ParametersAreNonnullByDefault
    public static JsonObject getDataAsJSON(Location loc) {
        var blockData = getBlock(loc);
        var jsonObject = new JsonObject();
        if (blockData == null) {
            return null;
        }
        for (var key : blockData.getDataKeys()) {
            jsonObject.addProperty(key, blockData.getData(key));
        }
        return jsonObject;
    }

    public static void requestLoad(SlimefunBlockData blockData) {
        if (blockData.isDataLoaded()) {
            return;
        }

        if (loadingData.contains(blockData)) {
            return;
        }

        synchronized (loadingData) {
            if (loadingData.contains(blockData)) {
                return;
            }
            loadingData.add(blockData);
        }

        Slimefun.getDatabaseManager()
                .getBlockDataController()
                .loadBlockDataAsync(blockData, new IAsyncReadCallback<>() {
                    @Override
                    public void onResult(SlimefunBlockData result) {
                        loadingData.remove(blockData);
                    }
                });
    }

    public static void executeAfterLoad(SlimefunBlockData data, Runnable execute, boolean runOnMainThread) {
        if (data.isDataLoaded()) {
            execute.run();
            return;
        }

        Slimefun.getDatabaseManager().getBlockDataController().loadBlockDataAsync(data, new IAsyncReadCallback<>() {
            @Override
            public boolean runOnMainThread() {
                return runOnMainThread;
            }

            @Override
            public void onResult(SlimefunBlockData result) {
                execute.run();
            }
        });
    }
}
