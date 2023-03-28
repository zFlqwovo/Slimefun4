package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.ScopeKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.task.DelayedSavingLooperTask;
import com.xzavier0722.mc.plugin.slimefun4.storage.task.DelayedTask;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BlockDataController extends ADataController {

    private final Map<LinkedKey, DelayedTask> delayedWriteTasks;
    private final Set<String> loadedChunks;
    private final Map<String, String> blockCache;
    private final Map<String, Map<String, String>> blockDataCache;
    private final Map<String, Map<String, String>> chunkDataCache;
    private boolean enableDelayedSaving = false;
    private int delayedSecond = 0;
    private BukkitTask looperTask;

    BlockDataController() {
        super(DataType.BLOCK_STORAGE);
        delayedWriteTasks = new HashMap<>();
        loadedChunks = new HashSet<>();
        blockCache = new ConcurrentHashMap<>();
        blockDataCache = new ConcurrentHashMap<>();
        chunkDataCache = new ConcurrentHashMap<>();
    }

    public void initDelayedSaving(Plugin p, int delayedSecond, int forceSavePeriod) {
        checkDestroy();
        if (delayedSecond < 1 || forceSavePeriod < 1) {
            throw new IllegalArgumentException("Second must be greater than 0!");
        }
        enableDelayedSaving = true;
        this.delayedSecond = delayedSecond;
        looperTask = Bukkit.getScheduler().runTaskTimerAsynchronously(p, new DelayedSavingLooperTask(
                forceSavePeriod,
                () -> new HashMap<>(delayedWriteTasks),
                key -> {
                    synchronized (delayedWriteTasks) {
                        delayedWriteTasks.remove(key);
                    }
                }
        ), 20, 20);
    }

    public void setBlock(Location l, String sfId) {
        checkDestroy();
        var lKey = LocationUtils.getLocKey(l);
        blockCache.put(lKey, sfId);

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, lKey);

        var data = new RecordSet();
        data.put(FieldKey.LOCATION, lKey);
        data.put(FieldKey.SLIMEFUN_ID, sfId);

        var scopeKey = new LocationKey(DataScope.NONE, l);
        removeDelayedBlockDataUpdates(scopeKey); // Shouldn't have.. But for safe..
        scheduleWriteTask(scopeKey, key, data, true);
    }

    public void removeBlock(Location l) {
        checkDestroy();
        var lKey = LocationUtils.getLocKey(l);
        blockCache.put(lKey, null);

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, lKey);

        var scopeKey = new LocationKey(DataScope.NONE, l);
        blockDataCache.put(lKey, null);
        removeDelayedBlockDataUpdates(scopeKey);
        scheduleDeleteTask(scopeKey, key, true);
    }

    public String getBlock(Location l) {
        checkDestroy();
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
        checkDestroy();
        return blockCache.get(LocationUtils.getLocKey(l));
    }

    public void setBlockData(Location l, String key, String data) {
        checkDestroy();
        putBlockDataCache(LocationUtils.getLocKey(l), key, data);
        scheduleDelayedBlockDataUpdate(l, key);
    }

    public void removeBlockData(Location l, String key) {
        checkDestroy();
        putBlockDataCache(LocationUtils.getLocKey(l), key, null);
        scheduleDelayedBlockDataUpdate(l, key);
    }

    public String getBlockData(Location l, String key) {
        checkDestroy();
        var lKey = LocationUtils.getLocKey(l);
        if (!blockDataCache.containsKey(lKey)) {
            loadBlockData(lKey);
        }
        return getBlockDataCache(lKey, key);
    }

    public void getBlockDataAsync(Location l, String key, IAsyncReadCallback<String> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getBlockData(l, key)));
    }

    public void loadChunk(Chunk chunk) {
        checkDestroy();
        var cKey = LocationUtils.getChunkKey(chunk);
        if (loadedChunks.contains(cKey)) {
            return;
        }

        synchronized (loadedChunks) {
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

        key = new RecordKey(DataScope.CHUNK_DATA);
        key.addField(FieldKey.DATA_KEY);
        key.addField(FieldKey.DATA_VALUE);
        key.addCondition(FieldKey.CHUNK, cKey);

        getData(key).forEach(data -> putChunkDataCache(cKey, data.get(FieldKey.DATA_KEY), data.get(FieldKey.DATA_VALUE)));
    }

    private void loadBlockData(String lKey) {
        if (blockDataCache.containsKey(lKey)) {
            return;
        }

        var key = new RecordKey(DataScope.BLOCK_DATA);
        key.addCondition(FieldKey.LOCATION, lKey);
        key.addField(FieldKey.DATA_KEY);
        key.addField(FieldKey.DATA_VALUE);

        blockDataCache.put(lKey, null);
        getData(key).forEach(
                recordSet -> putBlockDataCache(lKey, recordSet.get(FieldKey.DATA_KEY), recordSet.get(FieldKey.DATA_VALUE))
        );
    }

    public void setChunkData(Chunk chunk, String key, String data) {
        checkDestroy();
        // TODO
    }

    public String getChunkData(Chunk chunk, String key) {
        checkDestroy();
        // TODO
        return null;
    }

    public void getChunkDataAsync(Chunk chunk, String key, IAsyncReadCallback<String> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getChunkData(chunk, key)));
    }

    @Override
    public void shutdown() {
        if (enableDelayedSaving) {
            looperTask.cancel();
            executeAllDelayedTasks();
        }
        super.shutdown();
    }

    private void scheduleDelayedBlockDataUpdate(Location l, String key) {
        var scopeKey = new LocationKey(DataScope.NONE, l);
        var lKey = LocationUtils.getLocKey(l);
        var val = getBlockDataCache(lKey, key);
        var reqKey = new RecordKey(DataScope.BLOCK_DATA);
        reqKey.addCondition(FieldKey.LOCATION, lKey);
        reqKey.addCondition(FieldKey.DATA_KEY, key);
        if (!enableDelayedSaving) {
            scheduleBlockDataUpdate(scopeKey, reqKey, lKey, key);
            return;
        }

        synchronized (delayedWriteTasks) {
            var linkedKey = new LinkedKey(scopeKey, reqKey);
            var task = delayedWriteTasks.get(linkedKey);
            if (task != null && !task.isExecuted()) {
                task.setRunAfter(delayedSecond, TimeUnit.SECONDS);
                return;
            }

            task = new DelayedTask(delayedSecond, TimeUnit.SECONDS, () -> {
                var newVal = getBlockDataCache(lKey, key);
                if (Objects.equals(val, newVal)) {
                    return;
                }

                scheduleBlockDataUpdate(scopeKey, reqKey, lKey, key);
            });
            delayedWriteTasks.put(linkedKey, task);
        }
    }

    private void removeDelayedBlockDataUpdates(ScopeKey scopeKey) {
        synchronized (delayedWriteTasks) {
            delayedWriteTasks.entrySet().removeIf(each -> scopeKey.equals(each.getKey().getParent()));
        }
    }

    private void scheduleBlockDataUpdate(ScopeKey scopeKey, RecordKey reqKey, String lKey, String key) {
        var val = getBlockDataCache(lKey, key);
        if (val == null) {
            scheduleDeleteTask(scopeKey, reqKey, false);
        } else {
            var data = new RecordSet();
            reqKey.addField(FieldKey.DATA_VALUE);
            data.put(FieldKey.LOCATION, lKey);
            data.put(FieldKey.DATA_KEY, key);
            data.put(FieldKey.DATA_VALUE, val);
            scheduleWriteTask(scopeKey, reqKey, data, false);
        }
    }

    private void executeAllDelayedTasks() {
        delayedWriteTasks.values().forEach(DelayedTask::runUnsafely);
    }

    private void putBlockDataCache(String locKey, String key, String data) {
        var cache = blockDataCache.get(locKey);
        if (cache == null) {
            if (data == null) {
                return;
            }
            cache = new ConcurrentHashMap<>();
            blockDataCache.put(locKey, cache);
        }
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
