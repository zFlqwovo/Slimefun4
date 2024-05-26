package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ASlimefunDataContainer;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunUniversalData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.UniversalMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Utils to access the cached block data.
 * It is safe to use when the target block is in a loaded chunk (such as in block events).
 * By default, please use
 * {@link com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController#getBlockData}
 */
public class StorageCacheUtils {
    private static final Set<ASlimefunDataContainer> loadingData = new HashSet<>();

    @ParametersAreNonnullByDefault
    public static boolean hasBlock(Location l) {
        return getBlock(l) != null;
    }

    @ParametersAreNonnullByDefault
    public static boolean hasUniversalBlock(Location l) {
        return Slimefun.getBlockDataService().getUniversalDataUUID(l.getBlock()).isPresent();
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunBlockData getBlock(Location l) {
        return Slimefun.getDatabaseManager().getBlockDataController().getBlockDataFromCache(l);
    }

    @ParametersAreNonnullByDefault
    public static boolean isBlock(Location l, String id) {
        var blockData = getBlock(l);
        return blockData != null && id.equals(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunItem getSfItem(Location l) {
        var blockData = getBlock(l);
        var universalData = getUniversalData(l.getBlock());
        if (blockData == null && universalData == null) return null;
        if (blockData != null) return SlimefunItem.getById(blockData.getSfId());
        return SlimefunItem.getById(universalData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable public static String getData(Location loc, String key) {
        var blockData = getBlock(loc);
        return blockData == null ? null : blockData.getData(key);
    }

    @ParametersAreNonnullByDefault
    public static void setData(Location loc, String key, String val) {
        var block = getBlock(loc);
        var universal = getUniversalData(loc.getBlock());
        if (block == null && universal == null) throw new NullPointerException();

        if (block != null) block.setData(key, val);
        if (universal != null) universal.setData(key, val);
    }

    @ParametersAreNonnullByDefault
    public static void removeData(Location loc, String key) {
        var block = getBlock(loc);
        var universal = getUniversalData(loc.getBlock());
        if (block == null && universal == null) throw new NullPointerException();

        if (block != null) block.removeData(key);
        if (universal != null) universal.removeData(key);
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
    @Nullable public static SlimefunUniversalData getUniversalData(Location location) {
        var uuid = Slimefun.getBlockDataService().getUniversalDataUUID(location.getBlock());

        return uuid.map(StorageCacheUtils::getUniversalData).orElse(null);
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunUniversalData getUniversalData(UUID uuid) {
        var uniData = Slimefun.getDatabaseManager().getBlockDataController().getUniversalDataFromCache(uuid);

        if (uniData == null) {
            return null;
        }

        if (!uniData.isDataLoaded()) {
            requestLoad(uniData);
            return null;
        }

        return uniData;
    }

    @ParametersAreNonnullByDefault
    @Nullable public static SlimefunUniversalData getUniversalData(Block block) {
        var uuid = Slimefun.getBlockDataService().getUniversalDataUUID(block);

        return uuid.map(StorageCacheUtils::getUniversalData).orElse(null);
    }

    @ParametersAreNonnullByDefault
    @Nullable public static UniversalMenu getUniversalMenu(Block block) {
        var uuid = Slimefun.getBlockDataService().getUniversalDataUUID(block);

        return uuid.map(uniId -> getUniversalMenu(uniId, block.getLocation())).orElse(null);
    }

    @ParametersAreNonnullByDefault
    @Nullable public static UniversalMenu getUniversalMenu(UUID uuid, Location l) {
        var uniData = Slimefun.getDatabaseManager().getBlockDataController().getUniversalDataFromCache(uuid);

        if (uniData == null) {
            return null;
        }

        if (!uniData.isDataLoaded()) {
            requestLoad(uniData);
            return null;
        }

        uniData.setLastPresent(l);

        return uniData.getUniversalMenu();
    }

    public static void requestLoad(ASlimefunDataContainer data) {
        if (data.isDataLoaded()) {
            return;
        }

        if (loadingData.contains(data)) {
            return;
        }

        synchronized (loadingData) {
            if (loadingData.contains(data)) {
                return;
            }
            loadingData.add(data);
        }

        if (data instanceof SlimefunBlockData blockData) {
            Slimefun.getDatabaseManager()
                    .getBlockDataController()
                    .loadBlockDataAsync(blockData, new IAsyncReadCallback<>() {
                        @Override
                        public void onResult(SlimefunBlockData result) {
                            loadingData.remove(data);
                        }
                    });
        } else if (data instanceof SlimefunUniversalData uniData) {
            Slimefun.getDatabaseManager()
                    .getBlockDataController()
                    .loadUniversalDataAsync(uniData, new IAsyncReadCallback<>() {
                        @Override
                        public void onResult(SlimefunUniversalData result) {
                            loadingData.remove(data);
                        }
                    });
        }
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

    public static void executeAfterLoad(SlimefunUniversalData data, Runnable execute, boolean runOnMainThread) {
        if (data.isDataLoaded()) {
            execute.run();
            return;
        }

        Slimefun.getDatabaseManager().getBlockDataController().loadUniversalDataAsync(data, new IAsyncReadCallback<>() {
            @Override
            public boolean runOnMainThread() {
                return runOnMainThread;
            }

            @Override
            public void onResult(SlimefunUniversalData result) {
                execute.run();
            }
        });
    }
}
