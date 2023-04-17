package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Chunk;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlimefunChunkData extends ASlimefunDataContainer {
    private final Chunk chunk;
    private final Map<String, SlimefunBlockData> sfBlocks;

    @ParametersAreNonnullByDefault
    SlimefunChunkData(Chunk chunk) {
        super(LocationUtils.getChunkKey(chunk));
        this.chunk = chunk;
        sfBlocks = new ConcurrentHashMap<>();
    }

    @Nonnull
    public Chunk getChunk() {
        return chunk;
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public SlimefunBlockData createBlockData(Location l, String sfId) {
        var lKey = LocationUtils.getLocKey(l);
        if (sfBlocks.get(lKey) != null) {
            throw new IllegalStateException("There already a block in this location: " + lKey);
        }
        var re = new SlimefunBlockData(l, sfId);
        sfBlocks.put(lKey, re);
        Slimefun.getDatabaseManager().getBlockDataController().saveNewBlock(l, sfId);
        return re;
    }

    @Nullable
    @ParametersAreNonnullByDefault
    public SlimefunBlockData getBlockData(Location l) {
        checkData();
        return sfBlocks.get(LocationUtils.getLocKey(l));
    }

    @Nullable
    @ParametersAreNonnullByDefault
    public SlimefunBlockData removeBlockData(Location l) {
        var lKey = LocationUtils.getLocKey(l);
        var re = sfBlocks.remove(lKey);
        if (re == null) {
            if (isDataLoaded()) {
                return null;
            }
            sfBlocks.put(lKey, null);
        }
        Slimefun.getDatabaseManager().getBlockDataController().removeBlockDirectly(l);
        return re;
    }

    void addBlockCacheInternal(String lKey, SlimefunBlockData data, boolean override) {
        if (override) {
            sfBlocks.put(lKey, data);
        } else {
            sfBlocks.putIfAbsent(lKey, data);
        }
    }

    SlimefunBlockData getBlockCacheInternal(String lKey) {
        return sfBlocks.get(lKey);
    }

    boolean hasBlockCache(String lKey) {
        return sfBlocks.containsKey(lKey);
    }

    SlimefunBlockData removeBlockDataCacheInternal(String lKey) {
        return sfBlocks.remove(lKey);
    }

    @ParametersAreNonnullByDefault
    public void setData(String key, String val) {
        checkData();
        setCacheInternal(key, val, true);
        Slimefun.getDatabaseManager().getBlockDataController().scheduleDelayedChunkDataUpdate(this, key);
    }

    @ParametersAreNonnullByDefault
    public void removeData(String key) {
        if (removeCacheInternal(key) != null) {
            Slimefun.getDatabaseManager().getBlockDataController().scheduleDelayedChunkDataUpdate(this, key);
        }
    }
}
