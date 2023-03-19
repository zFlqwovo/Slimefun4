package com.xzavier0722.mc.plugin.slimefun4.storage.adapter;

import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;

import java.util.List;

public interface IDataSourceAdapter<T> {
    void prepare(T config);
    void shutdown();
    void setData(RecordKey key, RecordSet item);
    List<RecordSet> getData(RecordKey key);
    void deleteData(RecordKey key);
}
