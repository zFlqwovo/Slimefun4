package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataType;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Collections;
import java.util.Set;

public class BlockDataController extends ADataController {

    BlockDataController() {
        super(DataType.BLOCK_STORAGE);
    }

    public void setBlock(Location l, String sfId) {

    }

    public void removeBlock(Location l) {

    }

    public void setData(Location l, String key, String data) {

    }

    public void removeData(Location l, String key) {

    }

    public String getData(Location l) {
        return null;
    }

    public void getDataAsync(Location l, IAsyncReadCallback<String> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getData(l)));
    }

    public Set<Location> getTickingLocations(Chunk chunk) {
        return Collections.emptySet();
    }

    public void getTickingLocationsAsync(Chunk chunk, IAsyncReadCallback<Set<Location>> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getTickingLocations(chunk)));
    }


    public void setChunkData(Chunk chunk, String key, String data) {

    }

    public String getChunkData(Chunk chunk, String key) {
        return null;
    }

    public void getChunkDataAsync(Chunk chunk, String key, IAsyncReadCallback<String> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getChunkData(chunk, key)));
    }
}
