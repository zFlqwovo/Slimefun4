package com.xzavier0722.mc.plugin.slimefun4.database.adapter;

import com.xzavier0722.mc.plugin.slimefun4.database.data.RecordSet;
import com.xzavier0722.mc.plugin.slimefun4.database.data.RecordKey;

import java.util.Set;

public interface IDataSourceAdapter<T> {
    void prepare(T config);
    void shutdown();
    void setData(RecordKey key, RecordSet item);
    Set<RecordSet> getData(RecordKey key);
    void deleteData(RecordKey key);
}
