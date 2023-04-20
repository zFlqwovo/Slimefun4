package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.logging.Level;

/**
 * Utils to access the cached block data.
 * It is safe to use when the target block is in a loaded chunk (such as in block events).
 * By default, please use
 * {@link com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController#getBlockData}
 */
public class StorageCacheUtils {
    @ParametersAreNonnullByDefault
    public static boolean hasBlock(Location l) {
        return getBlock(l) != null;
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static SlimefunBlockData getBlock(Location l) {
        return Slimefun.getDatabaseManager().getBlockDataController().getBlockDataFromCache(l);
    }

    @ParametersAreNonnullByDefault
    public static boolean isBlock(Location l, String id) {
        var blockData = getBlock(l);
        return blockData != null && id.equals(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static SlimefunItem getSfItem(Location l) {
        var blockData = getBlock(l);
        return blockData == null ? null : SlimefunItem.getById(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static String getData(Location loc, String key) {
        var blockData = getBlock(loc);
        return blockData == null ? null : blockData.getData(key);
    }

    @ParametersAreNonnullByDefault
    public static void setData(Location loc, String key, String val) {
        getBlock(loc).setData(key, val);
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static BlockMenu getMenu(Location loc) {
        var blockData = getBlock(loc);
        return blockData == null ? null : blockData.getBlockMenu();
    }
}