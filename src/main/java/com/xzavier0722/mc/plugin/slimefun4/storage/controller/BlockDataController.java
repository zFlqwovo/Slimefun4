package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockDataController extends ADataController {

    private final Set<String> loadedChunks;
    private final Map<String, String> blockCache;
    private final Map<String, Map<String, String>> blockDataCache;
    private final Map<String, Map<String, String>> chunkDataCache;

    BlockDataController() {
        super(DataType.BLOCK_STORAGE);
        loadedChunks = new HashSet<>();
        blockCache = new ConcurrentHashMap<>();
        blockDataCache = new ConcurrentHashMap<>();
        chunkDataCache = new ConcurrentHashMap<>();
    }

    public void setBlock(Location l, String sfId) {
        var lKey = LocationUtils.getLocKey(l);
        blockCache.put(lKey, sfId);

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, lKey);

        var data = new RecordSet();
        data.put(FieldKey.LOCATION, lKey);
        data.put(FieldKey.SLIMEFUN_ID, sfId);

        scheduleWriteTask(new LocationKey(DataScope.NONE, l), key, data, true);
    }

    public void removeBlock(Location l) {
        var lKey = LocationUtils.getLocKey(l);
        blockCache.put(lKey, null);

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, lKey);

        scheduleDeleteTask(new LocationKey(DataScope.NONE, l), key, true);
    }

    public String getBlock(Location l) {
        var lKey = LocationUtils.getLocKey(l);
        if (blockCache.containsKey(lKey)) {
            return blockCache.get(lKey);
        }

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, lKey);
        key.addField(FieldKey.SLIMEFUN_ID);

        var result = getData(key);
        var re = result.isEmpty() ? null : result.get(0).get(FieldKey.SLIMEFUN_ID);
        blockCache.put(lKey, re);
        return re;
    }

    public void getBlockAsync(Location l, IAsyncReadCallback<String> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getBlock(l)));
    }

    public String getLoadedBlock(Location l) {
        return blockCache.get(LocationUtils.getLocKey(l));
    }

    public void setBlockData(Location l, String key, String data) {

    }

    public void removeBlockData(Location l, String key) {

    }

    public String getBlockData(Location l) {
        return null;
    }

    public void getBlockDataAsync(Location l, IAsyncReadCallback<String> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getBlockData(l)));
    }

    public void loadChunk(Chunk chunk) {
        var cKey = LocationUtils.getChunkKey(chunk);
        if (loadedChunks.contains(cKey)) {
            return;
        }

        synchronized (this) {
            if (loadedChunks.contains(cKey)) {
                return;
            }
            loadedChunks.add(cKey);
        }

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addField(FieldKey.LOCATION);
        key.addField(FieldKey.SLIMEFUN_ID);
        key.addCondition(FieldKey.CHUNK, cKey);

        getData(key).forEach(block -> {
            var lKey = block.get(FieldKey.LOCATION);
            var sfId = block.get(FieldKey.SLIMEFUN_ID);
            blockCache.put(lKey, sfId);
            scheduleReadTask(() -> {
                if (Slimefun.getRegistry().getTickerBlocks().contains(sfId)) {
                    loadBlockData(lKey);
                    Slimefun.getTickerTask().enableTicker(LocationUtils.toLocation(lKey));
                }
            });
        });
    }

    private void loadBlockData(String lKey) {
        if (blockDataCache.containsKey(lKey)) {
            return;
        }

        var key = new RecordKey(DataScope.BLOCK_DATA);
        key.addCondition(FieldKey.LOCATION, lKey);
        key.addField(FieldKey.DATA_KEY);
        key.addField(FieldKey.DATA_VALUE);

        getData(key).forEach(
                recordSet -> putBlockDataCache(lKey, recordSet.get(FieldKey.DATA_KEY), recordSet.get(FieldKey.DATA_VALUE))
        );
    }

    public void setChunkData(Chunk chunk, String key, String data) {

    }

    public String getChunkData(Chunk chunk, String key) {
        return null;
    }

    public void getChunkDataAsync(Chunk chunk, String key, IAsyncReadCallback<String> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getChunkData(chunk, key)));
    }

    private void putBlockDataCache(String locKey, String key, String data) {
        var cache = blockDataCache.computeIfAbsent(locKey, k -> new ConcurrentHashMap<>());
        cache.put(key, data);
    }

    private String getBlockDataCache(String locKey, String key) {
        var cache = blockDataCache.get(locKey);
        return cache == null ? null : cache.get(key);
    }

    private void putChunkDataCache(String chunkKey, String key, String data) {
        var cache = chunkDataCache.computeIfAbsent(chunkKey, k -> new ConcurrentHashMap<>());
        cache.put(key, data);
    }

    private String getChunkDataCache(String chunkKey, String key) {
        var cache = chunkDataCache.get(chunkKey);
        return cache == null ? null : cache.get(key);
    }
}
